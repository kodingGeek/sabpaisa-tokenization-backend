@echo off
echo ========================================
echo SabPaisa Backend - Docker Local Mode
echo ========================================
echo.

echo [1/3] Building Docker image...
docker build -f Dockerfile.local -t sabpaisa-backend-local .
if errorlevel 1 (
    echo Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo [2/3] Starting container...
docker run -d --rm ^
    --name sabpaisa-backend-local ^
    -p 8082:8082 ^
    -e SPRING_PROFILES_ACTIVE=local ^
    sabpaisa-backend-local

echo.
echo [3/3] Waiting for application to start...
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo Application should be running at:
echo.
echo - Application: http://localhost:8082
echo - H2 Console: http://localhost:8082/h2-console
echo - Swagger UI: http://localhost:8082/swagger-ui.html
echo - Health: http://localhost:8082/actuator/health
echo.
echo H2 Database Connection:
echo - JDBC URL: jdbc:h2:mem:sabpaisa_tokenization_local
echo - Username: sa
echo - Password: (leave blank)
echo.
echo ========================================
echo.
echo To stop the container:
echo   docker stop sabpaisa-backend-local
echo.
echo To view logs:
echo   docker logs -f sabpaisa-backend-local
echo.
pause