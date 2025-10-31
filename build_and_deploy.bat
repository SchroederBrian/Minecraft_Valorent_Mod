@echo off
echo ============================================
echo Valorant Mod - Build and Deploy Script
echo ============================================
echo.

REM Set paths
set MOD_JAR_NAME=valorant-1.0.0.jar
set BUILD_DIR=build\libs
set MOD_FOLDER=C:\Users\bobby\AppData\Roaming\ModrinthApp\profiles\valorant\mods
set SOURCE_JAR=%BUILD_DIR%\%MOD_JAR_NAME%
set TARGET_JAR=%MOD_FOLDER%\%MOD_JAR_NAME%

echo Step 1: Building the mod...
call gradlew.bat build

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Checking if jar was created...
if not exist "%SOURCE_JAR%" (
    echo ERROR: Jar file not found at %SOURCE_JAR%
    pause
    exit /b 1
)

echo.
echo Step 3: Checking if mod folder exists...
if not exist "%MOD_FOLDER%" (
    echo WARNING: Mod folder does not exist, creating it...
    mkdir "%MOD_FOLDER%"
)

echo.
echo Step 4: Removing old mod file...
if exist "%TARGET_JAR%" (
    del /F /Q "%TARGET_JAR%"
    echo Old mod file deleted.
) else (
    echo No old mod file found.
)

echo.
echo ============================================
echo SUCCESS! Mod deployed successfully!
echo ============================================
echo.
echo JAR location: %TARGET_JAR%
echo.
echo Step 6: Launching Minecraft (NeoForge dev client)...
echo.
REM You can switch to launching Modrinth App by setting LAUNCH_MODE=modrinth
set LAUNCH_MODE=dev

if /I "%LAUNCH_MODE%"=="dev" (
    echo Starting dev client via Gradle runClient...
    call gradlew.bat runClient
    goto :eof
) else (
    REM Attempt to start Modrinth App (no direct CLI to auto-launch a profile)
    set MODRINTH_APP=%LOCALAPPDATA%\Programs\Modrinth App\Modrinth App.exe
    if exist "%MODRINTH_APP%" (
        echo Opening Modrinth App. Launch the 'valorant' profile from the app.
        start "" "%MODRINTH_APP%"
    ) else (
        echo Modrinth App not found at: %MODRINTH_APP%
        echo Skipping Modrinth launch. You can start it manually.
    )
)

goto :eof

