@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo       SISTEMA DE CONSTRUCCION + SONARQUBE
echo ====================================================

:: 1. VALIDAR ARCHIVOS DEL PROYECTO
echo [*] Validando estructura del proyecto...
if not exist "pom.xml" (
    echo [ERROR] No se encontro el archivo pom.xml en: "%cd%"
    exit /b 1
)
echo [OK] pom.xml detectado.

:: 2. VALIDAR ENTORNO
echo [*] Verificando herramientas...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java no es accesible.
    exit /b 1
)

call mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] El comando 'mvn' no funciona. Verifique el PATH.
    exit /b 1
)
echo [OK] Herramientas listas.

:: 3. LIMPIEZA SEGURA
echo [*] Limpiando procesos de Maven previos...
taskkill /f /im mvn.exe /t >nul 2>&1

:: 4. EJECUCION DE MAVEN + ANALISIS SONARQUBE
echo [*] Iniciando compilacion y analisis en SonarQube...
echo ----------------------------------------------------

:: Ejecutamos la compilacion y enviamos a Sonar en un solo paso
call mvn clean install sonar:sonar ^
  -Dsonar.projectKey=Mi-Primer-Proyecto-Java ^
  -Dsonar.projectName="Proyecto Java Jonathan" ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.login=118b78b350cf99def26ce78561858e5678 ^
  -DskipTests=false

set MAVEN_RESULT=!errorlevel!
echo ----------------------------------------------------

:: 5. RESULTADO FINAL
if !MAVEN_RESULT! equ 0 (
    echo.
    echo ====================================================
    echo [SUCCESS] CONSTRUCCION Y ANALISIS COMPLETADOS
    echo Revisa los resultados en: http://localhost:9000
    echo Finalizado el: %date% a las %time%
    echo ====================================================
    exit /b 0
) else (
    echo.
    echo ====================================================
    echo [FAILURE] ERROR EN LA COMPILACION O SONAR
    echo Codigo de salida: !MAVEN_RESULT!
    echo ====================================================
    exit /b 1
)