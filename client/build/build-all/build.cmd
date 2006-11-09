@echo off
cls
rem *--------------------------------------------------------------------------
rem * Validate parameters
rem *--------------------------------------------------------------------------
rem *
rem * JAVA_HOME
rem *
if "%1" == "" goto usage
rem *
rem * BUILD_PROPS_FILE
rem *
if "%2" == "" goto usage
rem *
rem * BUILD_HOME
rem *
if "%3" == "" goto usage
rem *
rem * PRODUCT_VERSION
rem *
if "%4" == "" goto usage
rem *
rem * CVS_TAG
rem *
if "%5" == "" goto usage

rem *--------------------------------------------------------------------------
rem * Set local environment variables
rem *--------------------------------------------------------------------------
set JAVA_HOME=%1
set BUILD_PROPS_FILE=%2
set BUILD_HOME=%3
set PRODUCT_VERSION=%4
set CVS_TAG=%5

rem *--------------------------------------------------------------------------
rem * Checkout from CVS
rem *--------------------------------------------------------------------------
call ant -Dapi.home.dir=%BUILD_HOME% -Dproduct.version=%PRODUCT_VERSION% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.type=gbclient -Dapi.build.dir=gbclient -Dcvs.module=gbclient-build -Dcvs.branch.tag=%CVS_TAG% -v -buildfile ../xml/api_manage_code.xml checkout
call ant -Dapi.home.dir=%BUILD_HOME% -Dproduct.version=%PRODUCT_VERSION% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.type=gbapi -Dapi.build.dir=gbapi -Dcvs.module=gbapi-build -Dcvs.branch.tag=%CVS_TAG% -v -buildfile ../xml/api_manage_code.xml checkout


rem *--------------------------------------------------------------------------
rem * Build code
rem *--------------------------------------------------------------------------
call ant -Dapi.home.dir=%BUILD_HOME% -Dcvs.tag=%CVS_TAG% -Dproduct.version=%PRODUCT_VERSION% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbclient -buildfile %BUILD_HOME%\gbclient\gbclient\build\xml\build.xml
call ant -Dapi.home.dir=%BUILD_HOME% -Dcvs.tag=%CVS_TAG% -Dproduct.version=%PRODUCT_VERSION% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml


rem *--------------------------------------------------------------------------
rem * Create WAR, JAR, and EAR files
rem *--------------------------------------------------------------------------
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml war-all
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml ear-all


rem *--------------------------------------------------------------------------
rem * Make JRun deployment
rem *--------------------------------------------------------------------------
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml make-gb-deployment

echo *-----------------------------------------------------------------------------------
echo * Build complete...
echo *-----------------------------------------------------------------------------------
goto end

:usage
echo *-----------------------------------------------------------------------------------
echo * %0 "<java_home>" "<build_props_file>" "<build_home>" "<product_version>" "<cvs_tag>"
echo *
echo * where:
echo *
echo *    java_home        = Mandatory. Java JDK home directory
echo *    build_props_file = Mandatory. The Ant properties file that contains
echo *                       the build properties.
echo *    build_home       = Mandatory. The directory that will contain the
echo *                       build output.
echo *    product_version  = Mandatory. The product version of GB and the GBAPIs being
echo *                       built.
echo *    build_tag        = Mandatory. The CVS tag of the code to build.
echo *-----------------------------------------------------------------------------------

:end
