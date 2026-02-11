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

:: 2. VALIDAR JAVA_HOME
echo [*] Verificando entorno Java...
if "%JAVA_HOME%"=="" (
    echo [WARN] JAVA_HOME no esta definida. Buscando 'java' en PATH...
) else (
    echo [OK] JAVA_HOME detectado en: "%JAVA_HOME%"
)

java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java no es accesible. Verifica la instalacion.
    exit /b 1
)

:: 3. VALIDAR MAVEN (Sin usar 'where' para evitar errores de sintaxis)
echo [*] Verificando Maven...
call mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] El comando 'mvn' no funciona. 
    echo Asegurate de que la carpeta 'bin' de Maven este en el PATH de Windows.
    exit /b 1
)
echo [OK] Maven esta listo para usar.

:: 4. LIMPIEZA PREVIA (Opcional)
echo [*] Limpiando procesos de Java previos para evitar bloqueos...
taskkill /f /im java.exe /t >nul 2>&1
:: No validamos errorlevel aqui porque si no hay procesos, dara error y es normal.

:: 5. EJECUCION DE MAVEN
echo [*] Iniciando compilacion: mvn clean install...
echo ----------------------------------------------------
call mvn clean install -DskipTests=false
set MAVEN_RESULT=!errorlevel!
echo ----------------------------------------------------

:: 6. RESULTADO FINAL
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
    echo Revisa los errores arriba. Codigo de salida: !MAVEN_RESULT!
    echo ====================================================
    exit /b 1
)