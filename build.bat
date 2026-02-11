@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo       SISTEMA DE CONSTRUCCION AUTOMATIZADA
echo ====================================================

:: 1. VALIDAR ARCHIVOS DEL PROYECTO
echo [*] Validando estructura del proyecto...
if not exist "pom.xml" (
    echo [ERROR] No se encontro el archivo pom.xml en: %cd%
    echo Asegurate de que Jenkins este descargando el codigo en la carpeta correcta.
    exit /b 1
)
echo [OK] pom.xml detectado.

:: 2. VALIDAR JAVA_HOME
echo [*] Verificando JAVA_HOME...
if "%JAVA_HOME%"=="" (
    echo [WARN] La variable JAVA_HOME no esta definida. Intentando usar java por defecto...
    java -version >nul 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] Java no esta instalado o no se encuentra en el PATH.
        exit /b 1
    )
) else (
    echo [OK] JAVA_HOME esta definido en: %JAVA_HOME%
)

:: 3. VALIDAR MAVEN (mvn)
echo [*] Verificando ejecutable de Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] No se encuentra el comando 'mvn'. 
    echo Verifica que la carpeta 'bin' de Maven este en las Variables de Entorno (PATH).
    exit /b 1
)
echo [OK] Maven detectado correctamente.

:: 4. LIMPIEZA DE MEMORIA (Opcional pero util en Jenkins)
echo [*] Limpiando procesos antiguos...
:: Esto libera recursos si hubo una ejecucion trabada anteriormente
taskkill /f /im java.exe /t >nul 2>&1

:: 5. EJECUCION DE MAVEN
echo [*] Iniciando: mvn clean install...
echo ----------------------------------------------------
call mvn clean install -DskipTests=false
echo ----------------------------------------------------

:: 6. RESULTADO FINAL
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================
    echo [SUCCESS] CONSTRUCCION COMPLETADA SIN ERRORES
    echo Finalizado el: %date% a las %time%
    echo ====================================================
    exit /b 0
) else (
    echo.
    echo ====================================================
    echo [FAILURE] ERROR CRITICO EN LA COMPILACION
    echo Revisa el log de Maven arriba para mas detalles.
    echo ====================================================
    exit /b 1
)