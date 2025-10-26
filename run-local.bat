@echo off
echo Starting SabPaisa Backend in LOCAL mode...
echo =========================================
echo.
echo Configuration:
echo   - Profile: local
echo   - Database: H2 (in-memory)
echo   - Port: 8082
echo   - H2 Console: http://localhost:8082/h2-console
echo   - Swagger UI: http://localhost:8082/swagger-ui.html
echo.

:: Clean and compile
echo Cleaning project...
call mvn clean

echo.
echo Compiling project...
call mvn compile

echo.
echo Starting application...
call mvn spring-boot:run -Dspring.profiles.active=local

pause