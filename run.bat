@echo off
setlocal enabledelayedexpansion
title Blood Donation Management System

echo ===================================================
echo   Starting Blood Donation Management System
echo ===================================================
echo.

cd /d "%~dp0"

:: Locate a usable JDK first so Maven and Spring Boot run with a compatible Java version.
set "JAVA_HOME="
if exist "%USERPROFILE%\.jdks\*\bin\java.exe" (
    for /d %%D in ("%USERPROFILE%\.jdks\*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_HOME=%%D"
            goto found_java
        )
    )
)
:found_java
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-17\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-21\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-21"
if not defined JAVA_HOME if exist "C:\Users\naksh\.jdks\openjdk-26.0.2.1\bin\java.exe" set "JAVA_HOME=C:\Users\naksh\.jdks\openjdk-26.0.2.1"
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-25\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-25"
if not defined JAVA_HOME if exist "C:\Users\naksh\.jdk\jdk-25\jdk-25.0.2\bin\java.exe" set "JAVA_HOME=C:\Users\naksh\.jdk\jdk-25\jdk-25.0.2"

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo Using Java: "%JAVA_HOME%"
)

:: Check if mvn is available in PATH or IntelliJ's bundled Maven installation.
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
        set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
    ) else (
        for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
            if exist "%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
                set "MVN_CMD=%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
            )
        )
    )
)

if not defined JAVA_HOME (
    echo [ERROR] No Java installation was found on this machine.
    pause
    exit /b 1
)

if "%MVN_CMD%"=="mvn" (
    where mvn >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Maven was not found. Install Maven or open the project once in IntelliJ IDEA.
        pause
        exit /b 1
    )
)

echo Using Maven: "%MVN_CMD%"
echo Application URL: http://localhost:8089
echo.
echo Starting Spring Boot server...
echo Press Ctrl+C to stop the server.
echo ===================================================
echo.

call "%MVN_CMD%" spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed to start or exited with an error.
    pause
)
