@echo off
rem ------------------------------------------------------------------
rem  Babelfish Compass
rem  Compatibility assessment tool for Babelfish for T-SQL
rem ------------------------------------------------------------------
rem Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
rem SPDX-License-Identifier: Apache-2.0
rem ------------------------------------------------------------------

title Babelfish Compass

rem ----------------------------------------------------
set THISPROG=%~n0%~x0
rem ----------------------------------------------------
rem fixed installation dir for now:

SET COMPASS=C:\Compass

rem Check:
if exist %COMPASS% ( 
rem OK
) else (
echo %COMPASS% not found
goto end
)

rem ----------------------------------------------------
rem Edit this setting for the Java installation
rem Example: SET JAVA_HOME="C:\Program Files\Java\jdk1.8.0_301"

SET JAVA_HOME="change_this"
if %JAVA_HOME% == "change_this" (
  echo You must first edit %~nx0 and change the JAVA_HOME environment variable
  echo to the folder where Java is installed on your system.
  goto end
)

if exist %JAVA_HOME% ( 
rem OK
) else (
echo JAVA_HOME [%JAVA_HOME%] not found
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

%JAVA_HOME%\bin\java.exe -fullversion 2> %TMPFILE%
for /F "tokens=4" %%i IN (%TMPFILE%) DO SET JAVA_VERSION=%%i
for /F "tokens=1-3" %%i IN (%TMPFILE%) DO SET JAVA_VERSION_CHK=%%i%%j%%k

if "%JAVA_VERSION_CHK%" NEQ "javafullversion" (
	echo Java/JRE not found. Please install JRE 8 or later
	goto end
)

for /F "tokens=1 delims=." %%i IN (%JAVA_VERSION%) DO SET JAVA_VERSION_CHK2=%%i

if %JAVA_VERSION_CHK2% EQU 1 (
for /F "tokens=2 delims=." %%i IN (%JAVA_VERSION%) DO SET JAVA_VERSION_CHK2=%%i
)

if %JAVA_VERSION_CHK2% LSS 8 (
   echo Babelfish Compass requires Java/JRE 8 or later. Java version found: %JAVA_VERSION_CHK2%
   echo Run 'java -version' and verify the version ID starts with '1.8' or later
   goto end
)

goto invoke

rem ----------------------------------------------------

:invoke

SET PATH=.;%PATH%;%JAVA_HOME%\bin;%COMPASS%\lib\antlr-4.9.2-complete.jar;
SET CLASSPATH=.;%JAVA_HOME%\bin;%COMPASS%\src;%COMPASS%\lib\antlr-4.9.2-complete.jar;

rem 8GB should be enough for everything except very large cases
java -Xmx8g -enableassertions compass.Compass %*

rem ----------------------------------------------------

:end
rem pause Hit any key to continue....

rem ----------------------------------------------------







