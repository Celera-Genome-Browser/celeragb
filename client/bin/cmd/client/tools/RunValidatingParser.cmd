@echo off
REM *********************************************************************************************
REM This program builds the classpath and passes through the validating parser's required params
REM *********************************************************************************************
call %cel_root%\bin\cmd\client\classpath.cmd
set classpath=%classpath%;%cel_root%\lib\shared\xml\xercbin.jar
echo %classpath%
REM ************************* Run the Validating Parser with three parameters
java api.facade.concrete_facade.xml.ValidatingParser %1 %2 %3
