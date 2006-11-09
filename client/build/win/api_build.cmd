@echo off
rem *==========================================================================
rem * FILE:
rem *    api_build.cmd
rem * DESCRIPTION:
rem *    Runs the Ant build for the specified API.
rem *==========================================================================

setlocal

rem *==========================================================================
rem * Loop through command line parameters.
rem *==========================================================================
:parse_cmd_line
if "%1" == "" goto end_parse_cmd_line
if "%1" == "-f" goto get_build_file
if "%1" == "-d" goto get_build_dir
if "%1" == "-h" goto get_home_dir
if "%1" == "-debug" goto set_debug_flag
if "%1" == "-emacs" goto set_emacs_flag
if "%1" == "-projecthelp" goto set_projecthelp_flag
goto get_build_target
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
rem * Retrieve the build file from the command line if the -f option
rem * was passed on the command line.
rem *==========================================================================
:get_build_file
shift
if not "%1" == "" set BUILD_FILE=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the build directory from the command line if the -d option
rem * was passed on the command line.
rem *==========================================================================
:get_build_dir
shift
if not "%1" == "" set BUILD_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the API home directory from the command line if the -h option
rem * was passed on the command line.
rem *==========================================================================
:get_home_dir
shift
if not "%1" == "" set API_HOME_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the Ant build target from the command line if the -t option
rem * was passed on the command line.
rem *==========================================================================
:get_build_target
if not "%1" == "" set BUILD_TARGET=%1
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
if "%BUILD_TARGET%"  == "" goto show_usage
if "%BUILD_FILE%"    == "" goto show_usage
if "%API_BRANCH%"    == "" set API_BRANCH=HEAD

rem *==========================================================================
rem * Build command line.
rem *==========================================================================
set ANT_CMD_LINE=ant %PROJECT_HELP_FLAG% %EMACS_FLAG% %DEBUG_FLAG% -buildfile ../xml/%BUILD_FILE%.xml %BUILD_TARGET%
if not "%BUILD_PROPS_FILE%"   == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.props.file=%BUILD_PROPS_FILE%
if not "%JAVA_HOME%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Djava.home=%JAVA_HOME%
if not "%WL_HOME%"            == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dwl.home=%WL_HOME%
if not "%CVS_PASSFILE%"       == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.passwd.file=%CVS_PASSFILE%
if not "%CVSROOT%"            == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.root=%CVSROOT%
if not "%API_BRANCH%"         == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.branch.tag=%API_BRANCH%
if not "%API_HOME_DIR%"       == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.home.dir=%API_HOME_DIR%
if not "%BUILD_DIR%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.build.dir=%BUILD_DIR%

rem *==========================================================================
rem * Run the build.
rem *==========================================================================
%ANT_CMD_LINE%
goto end

:show_usage
echo.
echo Usage: %0 -f [build_file] -hdb [option] [ant_target]
echo.
echo where:
echo.
echo   -f            = Required parameter specifying the build file to use, without
echo                   the .XML extension.
echo   -debug        = Optional parameter to generate debug build output.
echo   -projecthelp  = Optional parameter to display the valid targets.
echo   -emacs        = Optional parameter to generate emacs output usable by IDEs.
echo   -h            = Optional parameter specifying the home directory that
echo                   the build subdirectory is created in. For development,
echo                   the directory your CVS modules are checked out to
echo                   (ex. c:\genomics).
echo   -d            = Optional parameter specifying the build subdirectory
echo                   (ex. 20001106_1414_server-build_HEAD). For development,
echo                   the module or branch name that the code is checked out
echo                   to (ex. HEAD or BRA_CTr30b4).
echo   ant_target    = Mandatory parameter specifying the build target. The
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
