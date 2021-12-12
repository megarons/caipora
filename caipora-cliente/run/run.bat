@echo off
cls
:SetVars
echo.
echo ==================================================================================
echo ========== INICIADO SCRIPT DE RUN CONFIGURADO PARA HOT DEPLOY QUARKUS ============
echo ==================================================================================
echo.
echo ======= Path do Script de Carga do H2 no Contexto do Local =======
set PWD=%CD%
set HOME=%HOMEDRIVE%%HOMEPATH%
set WORKSPACE=%PWD%\src\main\resources
set myUID=%USERNAME%
set myGroup=%USERDOMAIN%
echo %WORKSPACE%
echo.
echo.
:CallMvn
echo ============== Construido o Projeto com Maven Local ==============
echo.
call mvn clean install -Ddbiq.skip=false -Ddbiq.unbreakable=true -Ddbiq.user=$DBIQ_USER -Ddbiq.password=$DBIQ_TOKEN -Dmaven.test.skip=true
if %ERRORLEVEL% neq 0 goto ProcessError
echo.
echo ============ Projeto Construido com Sucesso pelo Maven ===========
echo.
echo ======= Path do Script de Carga do H2 no Contexto do Docker ======
set WORKSPACE=\app\src\main\resources
echo %WORKSPACE%
echo.
echo.
echo == Executando o Docker Compose Com Build para subir a aplica��o ==
echo.
:Down
if "%~1"=="-FIXED_CONTROL_C" ( 
echo.
echo ==========================================================================
echo =========== EXECUTANDO O DOCKER COMPOSE DOWN APOS CTRL+C =================
echo ==========================================================================
echo.
:ProcessError
call docker-compose -f %CD%\\run\\docker-compose.yaml down
goto Exit1
) else (
goto Up
)

:Up
call docker-compose -f %CD%\\run\\docker-compose.yaml up --build
exit /b 0

:Exit1
exit /b 1