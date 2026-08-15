@echo off
setlocal

echo Building NetAssist...

if exist build rmdir /s /q build
mkdir build\classes

dir /s /b src\main\java\*.java > build\sources.txt

javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 (
    echo.
    echo Build failed.
    exit /b 1
)

jar --create --file build\NetAssist.jar --main-class com.yousef.netassist.Main -C build\classes .
if errorlevel 1 (
    echo.
    echo JAR creation failed.
    exit /b 1
)

echo.
echo Build complete:
echo build\NetAssist.jar
