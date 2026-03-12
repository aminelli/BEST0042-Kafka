#!/bin/bash
# Startup script for Spark Normalizer with Java 21 compatibility

JAVA_OPTS="--add-opens java.base/sun.nio.ch=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.nio=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.util=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang.invoke=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.io=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS -Dhadoop.home.dir=$(pwd)/tmp"
JAVA_OPTS="$JAVA_OPTS -Djava.io.tmpdir=$(pwd)/tmp"

JAR_FILE="spark-normalizer/target/spark-normalizer-1.0.0.jar"
CONFIG_FILE="config/spark-normalizer.properties"

echo "Starting Spark Normalizer..."
java $JAVA_OPTS -jar $JAR_FILE $CONFIG_FILE
