@echo off
rem *==========================================================================
rem * FILE:
rem *    api_deploy.cmd
rem * DESCRIPTION:
rem *    Runs the Ant deployment for the specified API.
rem *==========================================================================

setlocal

rem *==========================================================================
rem * Loop through command line parameters.
rem *==========================================================================
:parse_cmd_line
if "%1" == "" goto end_parse_cmd_line
if "%1" == "-s" goto get_host_name
if "%1" == "-p" goto get_host_port
if "%1" == "-r" goto get_host_dir
if "%1" == "-h" goto get_home_dir
if "%1" == "-d" goto get_build_dir
if "%1" == "-a" goto get_api_type
if "%1" == "-i" goto get_sid_name
if "%1" == "-l" goto get_login_id
if "%1" == "-debug" goto set_debug_flag
if "%1" == "-emacs" goto set_emacs_flag
if "%1" == "-projecthelp" goto set_projecthelp_flag
goto get_deploy_target
shift
goto parse_cmd_line

rem *==========================================================================
rem * Set the debug flag if the -debug option was passed on the command line.
rem *==========================================================================
:set_debug_flag
set DEBUG_FLAG=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Set the emacs flag if the -emacs option was passed on the command line.
rem *==========================================================================
:set_emacs_flag
set EMACS_FLAG=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Set the debug flag if the -debug option was passed on the command line.
rem *==========================================================================
:set_projecthelp_flag
set PROJECT_HELP_FLAG=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the host name from the command line if the -s option was passed
rem * on the command line.
rem *==========================================================================
:get_host_name
shift
if not "%1" == "" set HOST_NAME=%1
shift
goto parse_cmd_line


rem *==========================================================================
rem * Retrieve the host port from the command line if the -p option
rem * was passed on the command line.
rem *==========================================================================
:get_host_port
shift
if not "%1" == "" set HOST_PORT=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the base remote host directory from the command line if
rem * the -r option was passed on the command line.
rem *==========================================================================
:get_host_dir
shift
if not "%1" == "" set HOST_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the API home directory from the command line if the -d option
rem * was passed on the command line.
rem *==========================================================================
:get_home_dir
shift
if not "%1" == "" set API_HOME_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the build directory from the command line if the -b option
rem * was passed on the command line.
rem *==========================================================================
:get_build_dir
shift
if not "%1" == "" set API_BUILD_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the Ant build target from the command line if the -t option
rem * was passed on the command line.
rem *==========================================================================
:get_deploy_target
if not "%1" == "" set DEPLOY_TARGET=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the API type from the command line if the -a option was passed
rem * on the command line.
rem *==========================================================================
:get_api_type
shift
if not "%1" == "" set API_TYPE=%1
shift
goto parse_cmd_line


rem *==========================================================================
rem * Retrieve the Oracle SID name from the command line if the -i option was
rem * passed on the command line.
rem *==========================================================================
:get_sid_name
shift
if not "%1" == "" set ORA_SID=%1
shift
goto parse_cmd_line


rem *==========================================================================
rem * Retrieve the Oracle login ID from the command line if the -l option was
rem * passed on the command line.
rem *==========================================================================
:get_login_id
shift
if not "%1" == "" set ORA_LOGIN=%1
shift
goto parse_cmd_line


rem *==========================================================================
rem * The end_parse_cmd_line label is here just to provide a place for the
rem * argument list loop to break out to.
rem *==========================================================================
:end_parse_cmd_line

rem *==========================================================================
rem * Validate required parameters.
rem *==========================================================================
if "%DEPLOY_TARGET%" == "" goto show_usage
if "%HOST_NAME%"     == "" if "%ORA_SID%" == "" goto show_usage

rem *==========================================================================
rem * Build command line.
rem *==========================================================================
set ANT_CMD_LINE=ant %PROJECT_HELP_FLAG% %EMACS_FLAG% %DEBUG_FLAG% -buildfile ../xml/api_deploy.xml
if not "%BUILD_PROPS_FILE%" == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.props.file=%BUILD_PROPS_FILE%
if not "%JAVA_HOME%"        == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Djava.home=%JAVA_HOME%
if not "%WL_HOME%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dwl.home=%WL_HOME%
if not "%CVS_PASSFILE%"     == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.passwd.file=%CVS_PASSFILE%
if not "%CVSROOT%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.root=%CVSROOT%
if not "%HOST_NAME%"        == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.host.name=%HOST_NAME%
if not "%HOST_PORT%"        == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.host.port=%HOST_PORT%
if not "%HOST_DIR%"         == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.host.dir=%HOST_DIR%
if not "%API_HOME_DIR%"     == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.home.dir=%API_HOME_DIR%
if not "%API_BUILD_DIR%"    == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.build.dir=%API_BUILD_DIR%
if not "%API_TYPE%"         == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.type=%API_TYPE%
if not "%ORA_SID%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dora.sid.name=%ORA_SID%
if not "%ORA_LOGIN%"        == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dora.login.id=%ORA_LOGIN%
if not "%DEPLOY_TARGET%"    == "" set ANT_CMD_LINE=%ANT_CMD_LINE% %DEPLOY_TARGET%

rem *==========================================================================
rem * Run the build.
rem *==========================================================================
%ANT_CMD_LINE%
goto end

:show_usage
echo.
echo Usage: %0 -s [host_name] -p [host_port] -r [remote_dir] -h [build_home_dir] -d [build_sub_dir] -a [api_type] -i [ora_sid] -l [ora_login_id] target
echo.
echo where:
echo.
echo   -debug        = Optional parameter to generate debug build output.
echo   -projecthelp  = Optional parameter to display the valid targets.
echo   -emacs        = Optional parameter to generate emacs output usable by IDEs.
echo   -s            = Optional parameter specifying the name of the host being
echo                   deployed to.
echo   -p            = Optional parameter specifying the host port.
echo   -r            = Optional parameter specifying the remote base host
echo                   directory in which the build will be deployed.
echo   -h            = Optional parameter specifying the fully qualified 
echo                   base build directory.
echo   -d            = Optional parameter specifying the subdirectory of the
echo                   build being deployed.
echo   -a            = Optional parameter specifying the API type to build:
echo                      gbapi = Genome Browser API
echo                      capi  = Internal API
echo   -i            = Optional parameter specifying the Oracle instance the PL/SQL
echo                   is being deployed to.
echo   -l            = Optional parameter specifying the Oracle login ID.
echo.
echo   target        = Mandatory parameter specifying the build target. The
echo                   valid targets can be seen by running this script with
echo                   the -projecthelp parameter (see -projecthelp above).
echo.
echo NOTE: To override properties in the project XML file, create a standard Java 
echo       properties file and place the fully qualified name in an environment
echo       variable called BUILD_PROPS_FILE.
echo.
goto end

:end
endlocal
