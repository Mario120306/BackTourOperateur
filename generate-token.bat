@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo    GENERATEUR DE TOKEN JWT - BackTourOperateur
echo ==============================================
echo.

REM Vérifier si Maven est installé
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Erreur: Maven n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Maven et reessayer.
    pause
    exit /b 1
)

REM Se déplacer dans le répertoire du projet
cd /d "%~dp0"

echo Compilation du projet...
call mvn compile -q

if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation du projet.
    pause
    exit /b 1
)

echo.
echo Execution du generateur de token...
echo.

REM Récupérer le classpath Maven
for /f "tokens=*" %%i in ('mvn dependency:build-classpath -Dmdep.outputFile=NUL -q -DincludeScope=runtime exec:exec -Dexec.executable="cmd" -Dexec.args="/c echo %%classpath" 2^>nul') do set MAVEN_CP=%%i

REM Construire le classpath complet
set CLASSPATH=target\classes;lib\*

REM Récupérer l'argument (durée en heures)
set VALIDITY_HOURS=%1
if "%VALIDITY_HOURS%"=="" set VALIDITY_HOURS=24

REM Exécuter le générateur de token
java -cp "%CLASSPATH%;%MAVEN_CP%" itu.back.util.TokenGenerator %VALIDITY_HOURS%

echo.
pause
