@echo off
setlocal ENABLEDELAYEDEXPANSION

echo ============================================
echo Valorant Mod - Build and Deploy (Dev Only)
echo ============================================

set "MOD_JAR_NAME=%mod_id%-%mod_version%.jar"
if "%MOD_JAR_NAME%"=="-%mod_version%.jar" set "MOD_JAR_NAME=valorant-1.0.0.jar"

set "BUILD_DIR=build\libs"
set "SOURCE_JAR=%BUILD_DIR%\%MOD_JAR_NAME%"
set "DEV_RUN_DIR=run"
set "MOD_FOLDER=%DEV_RUN_DIR%\mods"
set "TARGET_JAR=%MOD_FOLDER%\%MOD_JAR_NAME%"

echo.
echo Step 1: Build
call gradlew.bat build || goto :fail

echo.
echo Step 2: Verify JAR
if not exist "%SOURCE_JAR%" (
  echo ERROR: Missing JAR: %SOURCE_JAR%
  goto :fail
)

echo.
echo Step 3: Prepare run/mods
if not exist "%MOD_FOLDER%" mkdir "%MOD_FOLDER%"

echo.
echo Step 4: Deploy JAR
if exist "%TARGET_JAR%" del /F /Q "%TARGET_JAR%"
copy /Y "%SOURCE_JAR%" "%TARGET_JAR%" >nul || goto :fail

echo.
echo Step 5: Launch Dev Client (Quick-Play via gradle run config)
call gradlew.bat runClient
goto :eof

:fail
echo.
echo BUILD/DEPLOY FAILED
exit /b 1
