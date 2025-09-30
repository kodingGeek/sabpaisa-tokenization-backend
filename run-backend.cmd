@echo off
echo === Running SabPaisa Tokenization Backend with Enterprise Features ===
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_HOME=C:\Manish\WORK\Softwares\apache-maven-3.9.9-bin\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Checking Java...
java -version
echo.

echo Checking Maven...
call mvn -version
echo.

echo Setting environment variables...
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sabpaisa_tokenization
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=password
set SPRING_REDIS_HOST=localhost
set SPRING_REDIS_PORT=6379

echo.
echo Building application...
call mvn clean compile

echo.
echo Starting application on port 8082...
echo ==================================
call mvn spring-boot:run