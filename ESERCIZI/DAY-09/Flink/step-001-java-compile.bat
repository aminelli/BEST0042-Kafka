@echo off

cd flink-job
mvn clean package -DskipTests
cd ..
