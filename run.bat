@echo off
setlocal enabledelayedexpansion

:: =========================================
::   BUILD + DEPLOY PROJECT TO TOMCAT
:: =========================================

echo =========================================
echo       BUILD & DEPLOY TO TOMCAT
echo =========================================
echo.

:: --- CONFIGURATION ---------------------------------------
:: Repertoire Tomcat
set "TOMCAT_HOME=C:\Users\Mario\Documents\apache-tomcat-10.1.34"

:: Dossier webapps de Tomcat
set "TOMCAT_WEBAPPS=%TOMCAT_HOME%\webapps"



:: Nom du WAR (même que ton artifactId dans pom.xml)
set "WAR_NAME=sprint_test-1.0-SNAPSHOT"

:: ----------------------------------------------------------

:: Aller dans le dossier du projet
cd /d "%~dp0"

echo [1/3] Compilation du projet Maven...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERREUR] La compilation Maven a echoue.
    exit /b %ERRORLEVEL%
)
echo ✓ Compilation Maven reussie.
echo.

:: Trouver le WAR genere
for %%F in (target\*.war) do set "WAR_FILE=%%F"

if not defined WAR_FILE (
    echo [ERREUR] Aucun fichier WAR trouve dans target\
    exit /b 1
)

echo [2/3] Fichier WAR trouve: %WAR_FILE%
echo.

:: Suppression ancienne version deployee
if exist "%TOMCAT_WEBAPPS%\%WAR_NAME%.war" (
    echo Suppression de l'ancien WAR...
    del /f "%TOMCAT_WEBAPPS%\%WAR_NAME%.war" >nul
)
if exist "%TOMCAT_WEBAPPS%\%WAR_NAME%" (
    echo Suppression de l'ancien dossier deploye...
    rmdir /s /q "%TOMCAT_WEBAPPS%\%WAR_NAME%"
)

:: Copie du nouveau WAR
echo [3/3] Copie du WAR vers Tomcat...
copy /Y "%WAR_FILE%" "%TOMCAT_WEBAPPS%\%WAR_NAME%.war" >nul
if %ERRORLEVEL% neq 0 (
    echo [ERREUR] Impossible de copier le WAR vers %TOMCAT_WEBAPPS%.
    exit /b 1
)
echo ✓ WAR copie avec succès.
echo.

echo =========================================
echo   DEPLOIEMENT TERMINE AVEC SUCCES !
echo   (Tomcat doit deja etre en cours d execution)
echo =========================================
pause
