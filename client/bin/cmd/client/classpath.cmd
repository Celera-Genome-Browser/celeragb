IF NOT DEFINED cel_root goto Usage
set lib=%cel_root%\lib\client
set shared_lib=%cel_root%\lib\shared
set classpath=%cel_root%\bin;%lib%\graphics\ngsdk.jar;%lib%\weblogic\weblogicClient.jar;%lib%\weblogic\gbapi_stubs.jar;%shared_lib%\xml\xercbin.jar

goto end
:Usage
echo "cel_root environment varible MUST be defined.  cel_root should point to something like c:\genomics"
:end