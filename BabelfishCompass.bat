@echo off
rem ------------------------------------------------------------------
rem  Babelfish Compass
rem  Compatibility assessment tool for Babelfish for PostgreSQL
rem ------------------------------------------------------------------
rem Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
rem SPDX-License-Identifier: Apache-2.0
rem ------------------------------------------------------------------

title Babelfish Compass

rem ----------------------------------------------------
set THISPROG=%~n0%~x0
rem ----------------------------------------------------
rem fixed installation dir for now:

SET COMPASS=C:\XX

rem Check:
if exist %COMPASS% ( 
rem OK
) else (
echo %COMPASS% not found
goto end
)

rem ----------------------------------------------------
goto start

:usage
  echo.
  echo For usage info, run:
  echo    %THISPROG% -help 
  echo. 
  goto end

rem ----------------------------------------------------

:start
if "%1" == "" (
  goto usage
)

rem ---------------------------------------------------

rem Check for Java 8 or later
set TMPFILE=%TEMP%\compass.tmp.txt
java.exe -d64 -fullversion 2> %TMPFILE%
IF %ERRORLEVEL% NEQ 0 (
	echo 64-bit Java/JRE not found. Please install 64-bit JRE 8 or later
	goto end
)

for /F "tokens=4" %%i IN (%TMPFILE%) DO SET JAVA_VERSION=%%i
for /F "tokens=1-3" %%i IN (%TMPFILE%) DO SET JAVA_VERSION_CHK=%%i%%j%%k

if "%JAVA_VERSION_CHK%" NEQ "javafullversion" (
	echo 64-bit Java/JRE not found. Please install 64-bit JRE 8 or later
	goto end
)

for /F "tokens=1 delims=." %%i IN (%JAVA_VERSION%) DO SET JAVA_VERSION_CHK2=%%i

if %JAVA_VERSION_CHK2% EQU 1 (
for /F "tokens=2 delims=." %%i IN (%JAVA_VERSION%) DO SET JAVA_VERSION_CHK2=%%i
)

if %JAVA_VERSION_CHK2% LSS 8 (
   echo Babelfish Compass requires 64-bit Java/JRE 8 or later. Java version found: %JAVA_VERSION_CHK2%
   echo Run 'java -version' and verify the version ID starts with '1.8' or later
   goto end
)

goto invoke

rem ----------------------------------------------------

:invoke

rem assume Java is in the PATH, this was tested above
rem assuming 12GB is enough
java -Xmx12g -jar compass-1.0.jar %*

rem ----------------------------------------------------

:end
rem pause Hit any key to continue....

rem ----------------------------------------------------







