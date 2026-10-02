@echo off
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pDhanush@6364 < init-db.sql
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo  Database 'usermanagement_db' initialized SUCCESS!
    echo ===================================================
) else (
    echo.
    echo ERROR: Failed to initialize database. Check credentials.
)
