@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo        PROCESO DE CONSTRUCCION - MAVEN 
echo ====================================================

:: 1. Verificar si Java existe
echo [*] Verificando Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java no esta instalado o no esta en el PATH.
    exit /b 1
)

:: 2. Verificar si Maven existe
echo [*] Verificando Maven...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven (mvn) no se reconoce como comando. 
    echo Asegurate de que la carpeta bin de Maven este en el PATH.
    exit /b 1
)

:: 3. Ejecutar ciclo de vida de Maven
echo [*] Ejecutando: mvn clean install...
echo.
call mvn clean install
echo.

:: 4. Validar resultado de la compilacion
if %errorlevel% equ 0 (
    echo ====================================================
    echo [OK] PROYECTO CONSTRUIDO EXITOSAMENTE
    echo ====================================================
    exit /b 0
) else (
    echo ====================================================
    echo [FAIL] HUBO UN ERROR EN LA COMPILACION O TESTS
    echo ====================================================
    exit /b 1
)