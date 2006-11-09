SET JAVA_HOME=C:\j2sdk1.4.1_01
rem jdk1.3.1:  C:\jdk1.3.1_03\
SET BUILD_PROPS_FILE=..\build-all\myant.properties
SET BUILD_HOME=C:\build

SET VER="R4.3CPW"
SET CVS_TAG="HEAD"

rem Checkout from CVS
call ant -Dapi.home.dir=%BUILD_HOME% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.type=gbclient -Dapi.build.dir=gbclient -Dcvs.module=gbclient-build -Dcvs.branch.tag=%CVS_TAG% -v -buildfile ../xml/api_manage_code.xml checkout
call ant -Dapi.home.dir=%BUILD_HOME% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.type=gbapi -Dapi.build.dir=gbapi -Dcvs.module=gbapi-build -Dcvs.branch.tag=%CVS_TAG% -v -buildfile ../xml/api_manage_code.xml checkout


rem Build code
call ant -Dapi.home.dir=%BUILD_HOME% -Dcvs.tag=%CVS_TAG% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbclient -buildfile %BUILD_HOME%\gbclient\gbclient\build\xml\build.xml
call ant -Dapi.home.dir=%BUILD_HOME% -Dcvs.tag=%CVS_TAG% -Dproduct.version=%VER% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml


rem create EAR and WAR Files
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml war-all
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml ear-all


rem make JRun Deployment
call ant -Dapi.home.dir=%BUILD_HOME% -Dapi.props.file=%BUILD_PROPS_FILE% -Dapi.build.dir=gbapi -Dclient.build.dir=gbclient -v -buildfile %BUILD_HOME%\gbapi\gbapi\build\xml\build.xml make-gb-deployment