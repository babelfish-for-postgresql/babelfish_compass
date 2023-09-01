<#
    .SYNOPSIS
    SMO_DDL.ps1

    .DESCRIPTION
    Connects to the specified SQL Server, and generates DDL for database(s) listed. Default = 'ALL' = all user databases.
    The generated DDL script(s) are placed in the specified output folder.
    The login specified ('sa' in the examples below) must have permissions to access the databases.
    The specified output folder must exist. Specifying '.' is allowed.
    
    .EXAMPLE (Windows)
	    powershell .\SMO_DDL.ps1 -Databases 'All'                 -OutputFolder 'C:\mydir' -ServerName 'IP.ADDRESS.GOES.HERE' -UserName 'sa' -Password 'mypassword' 
	    powershell .\SMO_DDL.ps1 -Databases 'mydb,yourdb,otherdb' -OutputFolder 'C:\mydir' -ServerName 'IP.ADDRESS.GOES.HERE' -UserName 'sa' -Password 'mypassword' 
	    
    .EXAMPLE (Linux/Mac)
	    pwsh ./SMO_DDL.ps1 -Databases 'mydb,yourdb,otherdb' -OutputFolder '/tmp' -ServerName 'IP.ADDRESS.GOES.HERE' -Username 'sa' -Password 'mypassword' 	    
    
    .NOTES
    This Powershell script requires SMO (SQL Server Management Objects) to be installed. On Windows, when SQL Server itself is installed, SMO is typically already 
    installed and the script should work. 
    If SQL Server is not installed, the script will try to install SMO, but this is not 100% guaranteed to work - YMMV. When the script installs SMO, it will take 
    longer than subsequent runs.
    When running this script without proper SMO installation, there will be multiple error messages, including something like 'Cannot find type [Microsoft.SqlServer.Management.Smo.Server]'.
    When SMO is installed correctly but the script fails to access the SQL Server, there will only be an error message like 'Cannot connect to SQL Server [...]'
    
    Execution speed very much depends on (i) available resources on the host where the script is invoked from and (ii) the network proximity to the SQL Server
    since SMO performs many client-server roundtrips. With a few 1000's objects, it could take from minutes to hours to run, depending on the above.
    
    This script can be invoked individually as shown above. It is also invoked by Babelfish Compass, so if you want to make any changes, take a copy first.
    Note that a new version of Babelfish Compass may overwrite the existing script in the Compass installation firectory.
    
    On Linux, powershell for Linux needs to be installed first (https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-linux)
    
	This script was successfully tested on Windows, Mac, and Amazon Linux.
#>

# parameter defaults
[CmdletBinding()]
Param (
    [Parameter(Mandatory = $false)][String[]]$Databases = 'ALL',
    [Parameter(Mandatory = $false)][String]$OutputFolder = '',
    [Parameter(Mandatory = $false)][String]$ServerName = 'localhost',
    [Parameter(Mandatory = $false)][String]$UserName = 'sa',
    [Parameter(Mandatory = $false)][String]$Password = 'nosuchpassword',    
    [Parameter(Mandatory = $false)][String]$SMOOutputDir = 'smo_ddl_output',
    [Parameter(Mandatory = $false)][String]$DDLTag = ''	
)

$ThisProgram = "SMO_DDL.ps1"

Function Get-Prereqs {
    $CurrentPSVersion = $PSVersionTable.PSVersion
    [version]$DesiredPSVersion = '5.1.0.0'

    If ($CurrentPSVersion -lt $DesiredPSVersion) {
        Write-Output 'ERROR: WMF5.1 Not installed, see here for the installation file: https://www.microsoft.com/en-us/download/details.aspx?id=54616'
        Exit 1
    }

    $PPPresent = Get-PackageProvider -Name 'Nuget' -Force -ErrorAction SilentlyContinue
    If (-not $PPPresent) {
        Write-Output 'INFO: INSTALL: Installing the NuGet package provider'
        Try {
            $Null = Install-PackageProvider -Name 'NuGet' -MinimumVersion '2.8.5' -Force -ErrorAction Stop
        } Catch [System.Exception] {
            Write-Output "ERROR: Failed to install NuGet package provider $_"
            Exit 1
        }
    }

    $PsRepPresent = Get-PSRepository -Name 'PSGallery' | Select-Object -ExpandProperty 'InstallationPolicy' -ErrorAction SilentlyContinue
    If ($PsRepPresent -ne 'Trusted') {
        Write-Output 'INFO: INSTALL: Setting PSGallery respository to trusted'
        Try {
            Set-PSRepository -Name 'PSGallery' -InstallationPolicy 'Trusted' -ErrorAction Stop
        } Catch [System.Exception] {
            Write-Output "ERROR: Failed to set PSGallery respository to trusted $_"
            Exit 1
        }
    }

    $ModPresent = Get-Module -Name 'SqlServer' -ListAvailable | Select-Object -ExpandProperty 'Version' | Select-Object -ExpandProperty 'Major'
    If (-not $ModPresent -or $ModPresent -lt 21) {
        Write-Output 'INFO: INSTALL: Downloading and installing the SQL Server PowerShell module'
        Try {
            Install-Module 'SqlServer' -AllowClobber -Force -ErrorAction Stop
        } Catch [System.Exception] {
            Write-Output "ERROR: Failed to download and install the SQL Server PowerShell module $_"
            Exit 1
        }
    }

    if ($OnWindows) {
	    # The registry test does not seem to work for all versions of SSMS, so also perform additional test for actual installed ssms.exe
	    $SSMSPresent = Get-ItemProperty "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*" -ErrorAction SilentlyContinue | Where-Object { $_.DisplayName -eq 'SQL Server Management Studio' }
	    If (-not $SSMSPresent) {
		    $ExecPresent = Get-ChildItem "C:\Program Files (x86)\Microsoft SQL Server Management Studio *\Common7\IDE\ssms.exe" 
		    if (-not $ExecPresent) {
				Write-Output 'INFO: INSTALL: Installing SQL Server Management Studio'
				$TmpFile = $Env:TEMP + '\SSMS-Setup.exe'
				(New-Object -TypeName 'System.Net.WebClient').DownloadFile('https://aka.ms/ssmsfullsetup', $TmpFile)
				$ArgumentList = '/Quiet'
				Try {
				    $Process = Start-Process -FilePath $TmpFile -ArgumentList $ArgumentList -NoNewWindow -PassThru -Wait -ErrorAction Stop
				} Catch [System.Exception] {
				    Write-Output "ERROR: Failed to install SQL Server Management Studio $_"
				    Exit 1
				}    
		    }
	    }
	}
}

Function GenerateDDL-DB { 
	[CmdletBinding()]
	Param (
	    [Parameter(Mandatory = $true)][Microsoft.SqlServer.Management.Smo.Database]$Database,
	    [Parameter(Mandatory = $true)][String]$DestinationFilePath
	)
	$DDLFilename="$($DestinationFilePath)$FilenameSeparator$($Database.Name)$($DDLTag).sql"
	$DDLFilenameTmp="$($DDLFilename).tmp"
	
	
	# create output files
 	Try {
		if (Test-Path $DDLFilename) {
		  Remove-Item $DDLFilename -ErrorAction Stop
		}	
    } Catch [System.Exception] {
        Write-Output "ERROR: Failed to delete file '$DDLFilename':  $_"
        Exit 1
    }  	
    Try {
        $Null = New-Item -Path $DDLFilename -ItemType 'File' -ErrorAction Stop
    } Catch [System.Exception] {
        Write-Output "ERROR: Failed to create file '$DDLFilename':  $_"
        Exit 1
    }     
 	Try {
		if (Test-Path $DDLFilenameTmp) {
		  Remove-Item $DDLFilenameTmp -ErrorAction Stop
		}	
    } Catch [System.Exception] {
        Write-Output "ERROR: Failed to delete file '$DDLFilenameTmp':  $_"
        Exit 1
    }     
    Try {
        $Null = New-Item -Path $DDLFilenameTmp -ItemType 'File' -ErrorAction Stop
    } Catch [System.Exception] {
        Write-Output "ERROR: Failed to create file '$DDLFilenameTmp':  $_"
        Exit 1
    }     
  
  	# do the real work
    Write-Output "INFO: Generating DDL for database $Database to $DDLFilename ..."
  		
	# Explicitly adding CREATE DATABASE since it's not clear how to force that to be generated
	# ALTER DATABASE statements are also not generated
	Write-Output "-- Generated by $ThisProgram at $((Get-Date).ToString('yyyy-MM-dd HH:mm:ss')) for server $ServerName`n`nUSE master`ngo`nCREATE DATABASE $Database`ngo`nUSE $Database`ngo`n" | Out-File -FilePath $DDLFilename
	$Transfer = New-Object -TypeName 'Microsoft.SqlServer.Management.Smo.Transfer' $Database		
	$Transfer.Options.ScriptBatchTerminator = $true
	$Transfer.Options.ContinueScriptingOnError = $true
	$Transfer.Options.Filename = $DDLFilenameTmp
	$Transfer.Options.ToFileOnly = $true    
	$Transfer.Options.AppendToFile = $true
	$Transfer.Options.Indexes = $true
	$Transfer.Options.ClusteredIndexes = $true	
	$Transfer.Options.NonClusteredIndexes = $true	
	$Transfer.Options.ColumnStoreIndexes = $true	
	$Transfer.Options.FullTextIndexes = $true		
	$Transfer.Options.FullTextCatalogs = $true		
	$Transfer.Options.FullTextStopLists = $true		
	$Transfer.Options.ExtendedProperties = $true			
	$Transfer.Options.Triggers = $true	
	$Transfer.Options.ScriptOwner = $true
	$Transfer.Options.Permissions = $true	
	$Transfer.Options.SchemaQualify  = $true
	$Transfer.Options.SchemaQualifyForeignKeysReferences = $true
	$Transfer.Options.Default  = $true
	$Transfer.Options.WithDependencies  = $true
	$Transfer.Options.ScriptSchema = $true	
	$Transfer.Options.IncludeDatabaseContext = $true
	$Transfer.Options.IncludeHeaders = $true	
	$Transfer.Options.DRIAll = $true	
	$Transfer.Options.DriPrimaryKey = $true
	$Transfer.Options.DriForeignKeys = $true
	$Transfer.Options.DriUniqueKeys = $true
	$Transfer.Options.DriClustered = $true
	$Transfer.Options.DriNonClustered = $true
	$Transfer.Options.DriChecks = $true
	$Transfer.Options.DriDefaults = $true
	$Transfer.Options.DriIndexes = $true
	$Transfer.Options.DriAllKeys = $true
	$Transfer.Options.DriAllConstraints = $true
	$Transfer.Options.DriAll = $true
	$Transfer.Options.Encoding = [System.Text.Encoding]::UTF8;	
	$Transfer.CreateTargetDatabase = $true
	$Transfer.CopyAllLogins = $true	
	$Transfer.PreserveLogins = $true		
	$Transfer.CopyAllRoles = $true	
	$Transfer.CopyAllUsers = $true
	$Transfer.CopyAllObjects = $true
	$Transfer.PreserveDbo = $true		
    Try {
        $Transfer.ScriptTransfer() | select-string "USE " -Notmatch
    } Catch [System.Exception] {
        Write-Output "ERROR: Error in \$Transfer.ScriptTransfer() to $DDLFilenameTmp : $_"
        Remove-Item $DDLFilename
        Remove-Item $DDLFilenameTmp
        Exit 1
    }  		
	Get-Content -Path $DDLFilenameTmp | Add-Content -Path $DDLFilename
	
	# Get some info on the SQL Server instance
	$SrvResourceTag = "SMO_DDL SQL Server info:" # do not change this string, or the variable name below!
	$SQLSrvName = $($MyServer.NetName)
	Write-Output "`n--SQL Server Information generated by SMO_DDL.ps1 (do not change these lines):`n"  | Add-Content -Path $DDLFilename	
	Write-Output "DECLARE @srvinfo VARCHAR(MAX)"  | Add-Content -Path $DDLFilename	
	Write-Output "SET @srvinfo = '$SrvResourceTag $($SQLSrvName):Edition=$($MyServer.Edition)'"  | Add-Content -Path $DDLFilename	
	Write-Output "SET @srvinfo = '$SrvResourceTag $($SQLSrvName):Processors=$($MyServer.Processors)'" | Add-Content -Path $DDLFilename	
	Write-Output "SET @srvinfo = '$SrvResourceTag $($SQLSrvName):VersionString=$($MyServer.VersionString)'" | Add-Content -Path $DDLFilename	
	Write-Output "SET @srvinfo = '$SrvResourceTag $($SQLSrvName):PhysicalMemory=$($MyServer.PhysicalMemory) MB'" | Add-Content -Path $DDLFilename	
	Write-Output "go" | Add-Content -Path $DDLFilename	
	Write-Output " "  | Add-Content -Path $DDLFilename	
				
	# Generate the DDL
	Write-Output "-- End of script generated by $ThisProgram `n`n" | Add-Content -Path $DDLFilename	
	Remove-Item $DDLFilenameTmp
	Write-Output "INFO: Database [$($Database.Name)] : scripted to $DDLFilename"
	
	
}

#==================================================
# Main
#==================================================

#Write-Output "INFO: BabelfishCompassAutoDDL start PS"

# Older PS versions don't have $IsWindows etc.
$OnWindows = $false
$OnLinux = $false
$platform = [System.Environment]::OSVersion.Platform
If ($platform.ToString().substring(0,3) -eq "Win") {
    $OnWindows = $true
    $FilenameSeparator = "\";
} Else {
    $OnLinux = $true
    $FilenameSeparator = "/";
}
#Write-Output "INFO: BabelfishCompassAutoDDL/SMO: OnWindows:$OnWindows  OnLinux:$OnLinux "

# validations
$Servername = $Servername.Trim()
if ($Servername -eq '') {
    Write-Output "ERROR: -ServerName must be provided"
    Exit 1
}
$Username = $Username.Trim()
if ($Username -eq '') {
    Write-Output "ERROR: -Username must be provided"
    Exit 1
}
$Password = $Password.Trim()
if ($Password -eq '') {
    Write-Output "ERROR: -Password must be provided"
    Exit 1
}
$OutputFolder = $OutputFolder.Trim()
if ($OutputFolder -eq '') {
    Write-Output "ERROR: -OutputFolder must be provided"
    Exit 1
}
if (-not (Test-Path $OutputFolder) ) {
    Write-Output "ERROR: specified output folder not found [$OutputFolder]"
    Exit 1
}
$Databases = $Databases.Trim()
if ([string]::Concat($Databases).Trim() -eq '') {
	$Databases = "ALL"
    Write-Output "INFO: no list of databases provided, using 'ALL'"
}
if ($DDLTag -eq '') {
	$DDLTag = "_DDL_$(get-date -format yyyy-MM-dd)";
}

# validations done, now proceed...
Get-Prereqs
Import-Module SqlServer

[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null
[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMOExtended") | Out-Null
[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.Management.Common") | Out-Null

# create output dir
$SMOOutputDir = $SMOOutputDir.trim()
if ($SMOOutputDir -ne '') {
	$DestinationFilePath = Join-Path -Path $OutputFolder -ChildPath $SMOOutputDir
}
else {
	$DestinationFilePath = $OutputFolder 
}
$TempDir = Test-Path -Path $DestinationFilePath -ErrorAction Stop
If (-not $TempDir) {
    Try {
        $Null = New-Item -Path $DestinationFilePath -ItemType 'Directory' -ErrorAction Stop
    } Catch [System.Exception] {
        Write-Output "ERROR: Failed to create directory '$DestinationFilePath' $_"
        Exit 1
    }
}

# connect to the SQL Server 
$MyServer = New-Object -TypeName 'Microsoft.SqlServer.Management.Smo.Server' $ServerName
$MyServer.ConnectionContext.LoginSecure = $false
$MyServer.ConnectionContext.Login = $Username
$MyServer.ConnectionContext.Password = $Password
If ($Null -eq $MyServer.Version) {
	Write-Output "ERROR: Cannot connect to SQL Server $ServerName"
	Exit 1
}
Write-Output "INFO: Connected to SQL Server $ServerName"


# use this to get indication of client-server roundtrip time
$StartTime = (Get-Date)
$DBchk = $Myserver.Databases | Where-Object { $_.name -eq 'master' }
$EndTime = (Get-Date)
$millisec=[Math]::Ceiling([int32](($EndTime - $StartTime).TotalMilliSeconds))
Write-Output "INFO: roundtrip millisec=$millisec"   # do not change this string, must match Compass Java program
If ($DDLTag -eq 'report-roundtrip') { # do not change this string, must match Compass Java program
	Exit 1
}

If ($Databases.ToUpper() -eq 'ALL') { 
	$DBs = $Myserver.Databases | Where-Object { $_.IsSystemObject -eq $False }
	if ($DBs.Count -eq 0) {
		Write-Output "ERROR: No user databases found in server '$ServerName'"		
		Exit 1
	}
	$DBList = [String]::Join(', ',$DBs)
	Write-Output "INFO: $($DBs.Count) user databases found in server '$ServerName': $DBList"	
	If ($DDLTag -eq 'report-all-dbs') { # do not change this string, must match Compass Java program
		# just report the databases that will be processed, and exit
		Exit 1
	}
    Foreach ($DB in $DBs) {
    	If ($Database.ToLower() -eq "rdsadmin") { Continue }
        GenerateDDL-DB -Database $DB -DestinationFilePath $DestinationFilePath
    }
} Else { 
	[bool] $AllDBsFound = $true
    $Databases = $Databases.Split(',')
    Foreach ($Database in $Databases) {
    	$Database = $Database.Trim()
    	$db = $myServer.Databases[$Database]
        If ($db.name -ne $Database) {
            Write-Output "ERROR: Specified database not found: '$Database' in server '$ServerName' does not exist"
            $AllDBsFound = $false
        }     	
    }
    if (-not $AllDBsFound) {
    	Exit 1
    }
    Foreach ($Database in $Databases) {
        $Database = $Database.Trim()
        $db = $myServer.Databases[$Database]
        GenerateDDL-DB -Database $db -DestinationFilePath $DestinationFilePath
    }
}
#Write-Output "INFO: BabelfishCompassAutoDDL ready PS"

#
# end
#