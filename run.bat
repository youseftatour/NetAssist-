@echo off
setlocal

if not exist build\NetAssist.jar (
    call build.bat
    if errorlevel 1 exit /b 1
)

java -jar build\NetAssist.jar
