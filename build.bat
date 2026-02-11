@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo       SISTEMA DE CONSTRUCCION AUTOMATIZADA
echo ====================================================

:: 1. VALIDAR ARCHIVOS DEL PROYECTO
echo [*] Validando estructura del proyecto...
if not exist "pom.xml" (
    echo [ERROR] No se encontro el archivo pom.xml en: "%cd%"
    exit /b 1
)
echo [OK] pom.xml detectado.

:: 2. VALIDAR ENTORNO (JAVA y MAVEN)
echo [*] Verificando herramientas...

:: Verificamos Java sin bloquearnos en el IF
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java no es accesible.
    exit /b 1
)

:: Verificamos Maven sin usar 'where'
call mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] El comando 'mvn' no funciona. 
    echo Asegurate de que la carpeta 'bin' de Maven este en el PATH.
    exit /b 1
)
echo [OK] Herramientas listas.

:: 3. LIMPIEZA SEGURA
:: Solo matamos procesos de Maven colgados, NUNCA java.exe porque matamos a Jenkins
echo [*] Limpiando procesos de compilacion previos...
taskkill /f /im mvn.exe /t >nul 2>&1

:: 4. EJECUCION DE MAVEN
echo [*] Iniciando compilacion: mvn clean install...
echo ----------------------------------------------------
call mvn clean install -DskipTests=false
set MAVEN_RESULT=!errorlevel!
echo ----------------------------------------------------

:: 5. RESULTADO FINAL
if !MAVEN_RESULT! equ 0 (
    echo.
    echo ====================================================
    echo [SUCCESS] TODO SALIO BIEN - PROYECTO CONSTRUIDO
    echo Finalizado el: %date% a las %time%
    echo ====================================================
    exit /b 0
) else (
    echo.
    echo ====================================================
    echo [FAILURE] ERROR EN LA COMPILACION DE MAVEN
    echo Codigo de salida: !MAVEN_RESULT!
    echo ====================================================
    exit /b 1
)