@echo off
title User Management System - Server Launcher
echo ===================================================================
echo     Enterprise User Management System - Starting Application
echo ===================================================================
echo.
echo Step 1: Compiling application and resources...
call mvn clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven compilation failed. Please ensure JDK 17+ and Maven are installed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Step 2: Launching embedded Tomcat server on port 8080...
echo.
echo The application will be accessible at:
echo   http://localhost:8080/
echo   http://localhost:8080/login.jsp
echo.
echo Demo Accounts:
echo   - Administrator: admin / Admin@123
echo   - Manager:       john_doe / Manager@123
echo   - User:          jane_smith / User@123
echo.
echo Starting server (Press Ctrl+C to stop)...
echo ===================================================================
call mvn exec:java
pause
