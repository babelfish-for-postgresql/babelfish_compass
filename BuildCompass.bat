rem basic build script for Babelfish Compass
rem this requires ANTLR to be installed

SET COMPASS=C:\BabelfishCompass
SET JAVA_HOME="C:\Program Files\Java\jdk1.8.0_301"
SET PATH=.;%PATH%;%JAVA_HOME%\bin;%COMPASS%\lib\antlr-4.9.2-complete.jar;
SET CLASSPATH=.;%JAVA_HOME%\bin;%COMPASS%\src\parser;%COMPASS%\src;%COMPASS%\lib\antlr-4.9.2-complete.jar;%CLASSPATH%

rem generate parser
java org.antlr.v4.Tool src/parser/TSQLLexer.g4  -o parser
java org.antlr.v4.Tool src/parser/TSQLParser.g4 -o parser -visitor

rem compile Java
javac parser\*java
javac src\compass\Compass*.java

rem run it:
java -Xmx8g -enableassertions compass.Compass -help

rem ensure checksum on .cfg file is updated:
SET COMPASS_CHECKSUM=1
java -Xmx8g -enableassertions compass.Compass -checksum

rem
rem end
rem
