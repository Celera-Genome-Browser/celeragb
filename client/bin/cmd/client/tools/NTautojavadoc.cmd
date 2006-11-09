@echo off

REM *************************************************
REM  This cmd file creates a directory
REM  and checks out the latest from the 
REM  repository.  The source files will end up in
REM  local directory.
REM ************************************************* 

REM *************** Setup - must ensure proper environment ******************
REM Get the CVS build modules from the cvs server on the sun. 
REM /cm/cvs/CVSROOT/modules contains the valid module names.

set cel_root=D:\genomics_development\client-devel

call D:\genomics_development\client-devel\bin\cmd\client\classpath.cmd

set BUILD_DIR=D:\genomics_autojavadoc
set CVS_CHECKOUT_DIR=%BUILD_DIR%\cvs
set JAVADOC_SRC_DIR=%BUILD_DIR%\srcdocs
set JAVADOC_NGSDK_DIR=%BUILD_DIR%\ngsdkdocs

set CVS_LOCATION=D:\Program Files\GNU\WinCVS 1.1
set CVSROOT=tsaf@www.xxxxx.com:/cm/cvs
set CVS_BUILD_MODULE=src
set CVS_NGSDK_MODULE=ngsdk/src/com

set JAVA_DIR=D:\JBuilder35\jdk1.2.2\bin
set java_classes=D:\jbuilder35\java\jre\lib
set classpath=%java_classes%\rt.jar;%classpath%;%java_classes%\i18n.jar;%java_classes%\jaws.jar;D:\genomics_development\client-devel\bin\;

REM *************** Remove Last Javadoc Build **************
rd %CVS_CHECKOUT_DIR% /s/q
rd %JAVADOC_SRC_DIR% /s/q
rd %JAVADOC_NGSDK_DIR% /s/q
md %CVS_CHECKOUT_DIR%
md %JAVADOC_SRC_DIR%
md %JAVADOC_NGSDK_DIR%

REM *************** Checkout latest ***************
pushd %CVS_CHECKOUT_DIR%
"%CVS_LOCATION%\cvs" -w -d :pserver:%CVSROOT% checkout -AP %CVS_NGSDK_MODULE%
"%CVS_LOCATION%\cvs" -w -d :pserver:%CVSROOT% checkout -AP %CVS_BUILD_MODULE%
popd

REM *************** Run JavadocRecurse on Source ****************
pushd %BUILD_DIR%
%JAVA_DIR%\javac JavadocRecurse.java
%JAVA_DIR%\java JavadocRecurse %CVS_CHECKOUT_DIR%\packages.txt %CVS_CHECKOUT_DIR%\src
%JAVA_DIR%\java JavadocRecurse %CVS_CHECKOUT_DIR%\packages2.txt %CVS_CHECKOUT_DIR%\ngsdk\src
popd

REM *************** Create Javadocs for all packages ***************
pushd %CVS_CHECKOUT_DIR%
%JAVA_DIR%\javadoc -verbose -d %JAVADOC_SRC_DIR% -sourcepath %CVS_CHECKOUT_DIR%\src -J-Xmx100m -J-Xms100m @%CVS_CHECKOUT_DIR%\packages.txt
%JAVA_DIR%\javadoc -verbose -d %JAVADOC_NGSDK_DIR% -sourcepath %CVS_CHECKOUT_DIR%\ngsdk\src -J-Xmx100m -J-Xms100m @%CVS_CHECKOUT_DIR%\packages2.txt
popd

REM *************** Get rid of old jars, provided it gets this far *********
del S:\VISUALIZATION\Vizdocs\vizdocs.jar /Q
del S:\VISUALIZATION\Vizdocs\ngsdkdocs.jar /Q

REM *************** Create a JAR file and place the Javadocs into a folder on the S drive ***
pushd %JAVADOC_SRC_DIR%
%JAVA_DIR%\jar cfv vizdocs.jar *.*
copy vizdocs.jar S:\VISUALIZATION\Vizdocs\vizdocs.jar
popd
pushd %JAVADOC_NGSDK_DIR%
%JAVA_DIR%\jar cfv ngsdkdocs.jar *.*
copy ngsdkdocs.jar S:\VISUALIZATION\Vizdocs\ngsdkdocs.jar
popd

REM ************** Cleans up after itself, just in case. ******************
rd %CVS_CHECKOUT_DIR% /s/q
rd %JAVADOC_SRC_DIR% /s/q
rd %JAVADOC_NGSDK_DIR% /s/q
del %BUILD_DIR%\JavadocRecurse.class /Q

REM fin


