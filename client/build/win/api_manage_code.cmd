@echo off
rem *==========================================================================
rem * FILE:
rem *    cvs_manage_code.cmd
rem * DESCRIPTION:
rem *    Runs the Ant code management target for the specified API.
rem *==========================================================================

setlocal

rem *==========================================================================
rem * Loop through command line parameters.
rem *==========================================================================
:parse_cmd_line
if "%1" == "" goto end_parse_cmd_line
if "%1" == "-f" goto get_build_file
if "%1" == "-t" goto get_api_type
if "%1" == "-h" goto get_home_dir
if "%1" == "-d" goto get_build_dir
if "%1" == "-a" goto get_archive_dir
if "%1" == "-b" goto get_api_branch
if "%1" == "-l" goto get_branch_filter
if "%1" == "-g" goto get_api_tag
if "%1" == "-m" goto get_cvs_module
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
rem * Retrieve the required API type from the command line (i.e. gbapi
rem * or capi) if the -t option was passed on the command line.
rem *==========================================================================
:get_api_type
shift
if not "%1" == "" set API_TYPE=%1
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
rem * Retrieve the build directory from the command line if the -d option
rem * was passed on the command line.
rem *==========================================================================
:get_build_dir
shift
if not "%1" == "" set BUILD_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the archive directory from the command line if the -a option
rem * was passed on the command line.
rem *==========================================================================
:get_archive_dir
shift
if not "%1" == "" set ARCHIVE_DIR=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the CVS code branch from the command line if the -b option
rem * was passed on the command line.
rem *==========================================================================
:get_api_branch
shift
if not "%1" == "" set API_BRANCH=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the CVS date/time filter from the command line if the -l option
rem * was passed on the command line.
rem *==========================================================================
:get_branch_filter
shift
if not "%1" == "" set API_BRANCH_FILTER=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the CVS code tag from the command line if the -g option
rem * was passed on the command line.
rem *==========================================================================
:get_api_tag
shift
if not "%1" == "" set API_TAG=%1
shift
goto parse_cmd_line

rem *==========================================================================
rem * Retrieve the API home directory from the command line if the -h option
rem * was passed on the command line.
rem *==========================================================================
:get_cvs_module
shift
if not "%1" == "" set CVS_MODULE=%1
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
if "%API_TYPE%"     == "" goto show_usage
if "%BUILD_FILE%"   == "" goto show_usage
if "%BUILD_TARGET%" == "" goto show_usage
if "%API_BRANCH%"   == "" set API_BRANCH=HEAD

rem *==========================================================================
rem * Build command line.
rem *==========================================================================
set ANT_CMD_LINE=ant %PROJECT_HELP_FLAG% %EMACS_FLAG% %DEBUG_FLAG% -buildfile ../xml/%BUILD_FILE%.xml -Dapi.type=%API_TYPE%
if not "%BUILD_PROPS_FILE%"   == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.props.file=%BUILD_PROPS_FILE%
if not "%CVS_PASSFILE%"       == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.passwd.file=%CVS_PASSFILE%
if not "%CVSROOT%"            == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.root=%CVSROOT%
if not "%API_BRANCH%"         == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.branch.tag=%API_BRANCH%
if not "%API_TAG%"            == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.tag=%API_TAG%
if not "%API_BRANCH_FILTER%"  == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.time.filter=%API_BRANCH_FILTER%
if not "%API_HOME_DIR%"       == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.home.dir=%API_HOME_DIR%
if not "%BUILD_DIR%"          == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.build.dir=%BUILD_DIR%
if not "%ARCHIVE_DIR%"        == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dapi.archive.dir=%ARCHIVE_DIR%
if not "%CVS_MODULE%"         == "" set ANT_CMD_LINE=%ANT_CMD_LINE% -Dcvs.module=%CVS_MODULE%
if not "%BUILD_TARGET%"       == "" set ANT_CMD_LINE=%ANT_CMD_LINE% %BUILD_TARGET%

rem *==========================================================================
rem * Run the build.
rem *==========================================================================
%ANT_CMD_LINE%
goto end

:show_usage
echo.
echo Usage: %0 -f build_file -t api_type [-debug] [-projecthelp] [-emacs] -hdabmlg [option] [ant_target]
echo.
echo where:
echo.
echo   -f            = Required parameter specifying the build file to use, without
echo                   the .XML extension.
echo   -t            = Required parameter specifying the API type to work on:
echo                     gbapi = Genome Browser API
echo                     capi  = Internal API
echo   -debug        = Optional parameter to generate debug output.
echo   -projecthelp  = Optional parameter to display the valid targets.
echo   -emacs        = Optional parameter to generate emacs output usable by IDEs.
echo   -h            = Optional parameter specifying the home directory in which
echo                   the build subdirectory is created (ex. c:\genomics).
echo   -d            = Optional parameter specifying the build subdirectory
echo                   (ex. 20001106_1414_server-build_HEAD).
echo   -a            = Optional parameter specifying the location used to archive
echo                   a build.
echo   -b            = Optional parameter specifying the code line to work on.
echo                   Defaults to HEAD.
echo   -m            = Optional parameter specifying the CVS module to checkout.
echo   -l            = Optional parameter specifying the date/time filter to use when
echo                   tagging files.
echo   -g            = Optional parameter specifying the code tag to create, delete,
echo                   or use to create or delete a branch.
echo.
echo   ant_target    = Mandatory parameter specifying the Ant target to execute. The
echo                   valid targets can be seen by running this script with the
echo                   -projecthelp parameter (see -projecthelp above).
echo.
echo NOTE: To override properties in the project XML file, create a standard Java 
echo       properties file and place the fully qualified name in an environment
echo       variable called BUILD_PROPS_FILE.
echo.
goto end

:end
endlocal
