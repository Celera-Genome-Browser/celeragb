SET JAVA_HOME=C:\j2sdk1.4.1_01
REM SET JAVA_HOME=C:\jdk1.3.1_03\
SET BUILD_PROPS_FILE=..\build-all\myant.properties
SET BUILD_HOME=C:\build_capi

SET VER="R2.0CPW"
SET CVS_TAG="HEAD"

rem Checkout from CVS
call ant -Dapi.home.dir=%BUILD_HOME% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.type=capi -Dapi.build.dir=capi -Dcvs.module=capi-build -Dcvs.branch.tag=%CVS_TAG% -v -buildfile ../xml/api_manage_code.xml checkout

rem Build code
call ant -Dapi.home.dir=%BUILD_HOME% -Dcvs.tag=%CVS_TAG% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=capi -buildfile %BUILD_HOME%\capi\capi\build\xml\build.xml

rem create EAR and WAR Files
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=capi -Dclient.build.dir=capi -v -buildfile %BUILD_HOME%\capi\capi\build\xml\build.xml war-all
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=capi -Dclient.build.dir=capi -v -buildfile %BUILD_HOME%\capi\capi\build\xml\build.xml ear-all


rem make JRun Deployment
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=capi -Dclient.build.dir=capi -v -buildfile %BUILD_HOME%\capi\capi\build\xml\build.xml make-capi-deployment