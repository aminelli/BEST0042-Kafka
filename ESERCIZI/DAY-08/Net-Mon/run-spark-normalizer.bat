@echo off
REM Startup script for Spark Normalizer with Java 21 compatibility

set "JAVA_OPTS=--add-opens java.base/sun.nio.ch=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.nio=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.util=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.lang=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.lang.invoke=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.io=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% -Dhadoop.home.dir=%CD%\tmp"
set "JAVA_OPTS=%JAVA_OPTS% -Djava.io.tmpdir=%CD%\tmp"
set "JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%CD%\tmp\bin"
set "JAVA_OPTS=%JAVA_OPTS% -Dspark.hadoop.fs.file.impl=org.apache.hadoop.fs.RawLocalFileSystem"

REM Add Hadoop bin to PATH
set "PATH=%CD%\tmp\bin;%PATH%"

set "JAR_FILE=spark-normalizer\target\spark-normalizer-1.0.0.jar"
set "CONFIG_FILE=config\spark-normalizer.properties"

echo Starting Spark Normalizer...
java %JAVA_OPTS% -jar %JAR_FILE% %CONFIG_FILE%
