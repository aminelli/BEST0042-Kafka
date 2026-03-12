# PowerShell startup script for Spark Normalizer with Java 21 compatibility

$javaOpts = @(
    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens", "java.base/java.nio=ALL-UNNAMED",
    "--add-opens", "java.base/java.util=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens", "java.base/java.io=ALL-UNNAMED",
    "-Dhadoop.home.dir=$PWD\tmp",
    "-Djava.io.tmpdir=$PWD\tmp",
    "-Djava.library.path=$PWD\tmp\bin",
    "-Dspark.hadoop.fs.file.impl=org.apache.hadoop.fs.RawLocalFileSystem"
)

# Add Hadoop bin to PATH
$env:PATH = "$PWD\tmp\bin;$env:PATH"

$jarFile = "spark-normalizer\target\spark-normalizer-1.0.0.jar"
$configFile = "config\spark-normalizer.properties"

Write-Host "Starting Spark Normalizer with Java 21 compatibility..." -ForegroundColor Green

& java $javaOpts -jar $jarFile $configFile
