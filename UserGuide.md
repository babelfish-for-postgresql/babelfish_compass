**What Is Babelfish Compass?**

The Babelfish Compass (short for “**COMP**atibility **ASS**essment”) tool analyzes SQL/DDL code for one or more Microsoft SQL Server databases to identify the SQL features which are not currently compatible with Babelfish.

A new version of Babelfish Compass is released with every Babelfish release containing new or changed functionality.

**Downloading Babelfish Compass**

Babelfish Compass is available as open-source at <https://github.com/babelfish-for-postgresql/babelfishpg-compass-tool> 

A binary version can be downloaded from <https://github.com/babelfish-for-postgresql/babelfishpg-compass-tool/TBD> . The installation instructions below are based on this download.

**Installing Babelfish Compass (Windows)**

Requirements

The Java Runtime Environment (JRE) is required to run Babelfish Compass.  The Java JRE version must be 8 or higher (64 bit version).

Babelfish Compass produces compatibility assessment reports in HTML format.  For viewing the HTML output it is recommended to use a recent release of the Google Chrome or Mozilla Firefox browsers.

The reports can also generate a cross-reference with HTML links directly to the original SQL code that was analyzed. Note that for very large SQL source files, it may take some time before the browser displays the desired line. If this takes too long, it is also possible to edit the corresponding flat text file (same filename, but with a **.dat** suffix instead of **.html**). 

Installation

<this section to be written>

Babelfish Compass is distributed as an "executable JAR", which requires no CLASSPATH settings. The only environmental requirement is that the Java JRE is in the PATH.

The installation will provide the BabelfishCompass executable with text documentation and the BabelfishFeatures.cfg configuration file.  The BabelfishFeatures.cfg configuration file is not provided for user modification.  BabelfishFeatures.cfg contains a description of the Babelfish features supported by the current version of Babelfish and should not be changed.

**Running Babelfish Compass**

On Windows, Babelfish Compass is executed by opening a **cmd** command prompt (a.k.a. "DOS box") in the directory where Babelfish Compass is installed. By default, this is **C:\BabelfishCompass**.

At the command prompt, run **BabelfishCompass** (or **BabelfishCompass.bat**). To see online help information on the various command options, specify **-help**:

**C:\BabelfishCompass>  BabelfishCompass help**

Babelfish Compass usage typically starts with command-line execution to create an assessment report file.  The assessment report output file provides a detailed summary of the supported and unsupported SQL features in Babelfish for the analyzed SQL Server script(s).  
In its simplest form, an assessment report named **MyFirstReport**, containing the analysis for script **AnyCompany.sql**, is created as follows:

**C:\BabelfishCompass>  BabelfishCompass  MyFirstReport  AnyCompany.sql**

When the report is created, BabelfishCompass will automatically:

1. Open a explorer window in the directory where the report files are stored
1. Open the generated report in the default browser

Many additional options are possible, such as processing multiple input scripts (for one application or multiple applications), generating more detailed output reports. 

Assessment Report Options

<to be written>
