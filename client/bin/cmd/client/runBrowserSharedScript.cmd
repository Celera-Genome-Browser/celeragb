REM ** 
REM ** Expects params: 
REM ** 		CONFIG_RESOURCE	Required, the fully-qualified classpath name of the main resource file
REM **          %2 %3 %4 %5 %6 %7        Optional, passed to %USERNAME%.cmd

setlocal

set MAIN_CLASS=client.gui.application.genome_browser.GenomeBrowser
set TMP_RESOURCE=%1%

REM ** Look for a local config file for overrides
IF EXIST ..\%USERNAME%.cmd call ..\%USERNAME%.cmd %2 %3 %4 %5 %6 %7

REM ** Command line overrides local config file
IF "%TMP_RESOURCE%" NEQ "" set CONFIG_RESOURCE=%1%

REM ** Check required parameters
IF NOT DEFINED CONFIG_RESOURCE goto Usage_Params
IF NOT DEFINED cel_root goto Usage_Cel_Root

call %cel_root%\bin\cmd\client\classpath.cmd

IF DEFINED java_home set JAVA_RUN="%java_home%\bin\java" 
IF NOT DEFINED java_home set JAVA_RUN=java

%JAVA_RUN% -Xmx96m -Dx.genomebrowser.Config=%CONFIG_RESOURCE% %MAIN_CLASS%

goto end


:Usage_Cel_Root
echo "CEL_ROOT" environment varible MUST be defined.
pause
goto end

:Usage_Params
echo Usage: %0% MAIN_CLASS CONFIG_RESOURCE
pause
goto end

:end
endlocal
