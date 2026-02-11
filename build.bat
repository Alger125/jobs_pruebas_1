@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo        SISTEMA DE CONSTRUCCION + SONARQUBE PRO
echo ====================================================

:: 1. CONFIGURACION DE RUTAS (JAVA 25)
:: Forzamos a que el sistema use tu version mas reciente
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
set "PATH=!JAVA_HOME!\bin;!PATH!"

:: 2. VALIDAR ARCHIVOS DEL PROYECTO
echo [*] Validando estructura del proyecto...
if not exist "pom.xml" (
    echo [ERROR] No se encontro el archivo pom.xml en: "%cd%"
    exit /b 1
)
echo [OK] pom.xml detectado.

:: 3. VALIDAR ENTORNO
echo [*] Verificando herramientas...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java 25 no es accesible en la ruta especificada.
    exit /b 1
)

call mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] El comando 'mvn' no funciona. Verifique que Maven este en el PATH.
    exit /b 1
)
echo [OK] Java 25 y Maven listos.

:: 4. LIMPIEZA SEGURA
echo [*] Limpiando procesos de Maven previos...
taskkill /f /im mvn.exe /t >nul 2>&1

:: 5. EJECUCION DE MAVEN + ANALISIS SONARQUBE
echo [*] Iniciando compilacion y analisis en SonarQube...
echo ----------------------------------------------------

:: Usamos el NUEVO token squ_ y el parametro recomendado -Dsonar.token
call mvn clean install sonar:sonar ^
  -Dsonar.projectKey=Mi-Primer-Proyecto-Java ^
  -Dsonar.projectName="Proyecto Java Jonathan" ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.token=squ_97714e005d00f6aee631b5df24c8624ac09d470c ^
  -DskipTests=false

set MAVEN_RESULT=!errorlevel!
echo ----------------------------------------------------

:: 6. RESULTADO FINAL
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
    echo [FAILURE] ERROR EN LA COMPILACION O EN SONAR
    echo Revisa que el servidor de SonarQube este ENCENDIDO.
    echo Codigo de salida: !MAVEN_RESULT!
    echo ====================================================
    exit /b 1
)