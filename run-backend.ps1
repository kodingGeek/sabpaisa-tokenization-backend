# PowerShell Script to run SabPaisa Backend with Enterprise Features

Write-Host "=== Running SabPaisa Tokenization Backend with Enterprise Features ===" -ForegroundColor Green
Write-Host ""

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:MAVEN_HOME = "C:\Manish\WORK\Softwares\apache-maven-3.9.9-bin\apache-maven-3.9.9"
$env:Path = "$env:MAVEN_HOME\bin;$env:Path"

# Spring Boot environment variables
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/sabpaisa_tokenization"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "password"
$env:SPRING_REDIS_HOST = "localhost"
$env:SPRING_REDIS_PORT = "6379"

Write-Host "Prerequisites Check:" -ForegroundColor Yellow
Write-Host "-------------------"

# Check Java
Write-Host "Checking Java..."
& java -version

# Check Maven
Write-Host "`nChecking Maven..."
& mvn -version

Write-Host "`nService Requirements:" -ForegroundColor Yellow
Write-Host "--------------------"
Write-Host "✓ PostgreSQL should be running on port 5432" -ForegroundColor Cyan
Write-Host "✓ Redis should be running on port 6379" -ForegroundColor Cyan
Write-Host "✓ Database 'sabpaisa_tokenization' should exist" -ForegroundColor Cyan

Write-Host "`nBuilding application..." -ForegroundColor Green
& mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nStarting application on port 8082..." -ForegroundColor Green
    Write-Host "====================================" -ForegroundColor Green
    & mvn spring-boot:run
} else {
    Write-Host "`nBuild failed! Please check the error messages above." -ForegroundColor Red
}