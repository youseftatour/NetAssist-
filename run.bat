@echo off
setlocal

if not exist target\NetAssist.jar (
    call build.bat
    if errorlevel 1 exit /b 1
)

java -jar target\NetAssist.jar
