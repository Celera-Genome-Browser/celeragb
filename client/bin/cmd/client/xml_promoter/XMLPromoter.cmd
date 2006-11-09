@echo off
setlocal
set filename=
set server=

set classpath=xml_promoter.jar;weblogicClient.jar
set base=xml_promoter
java -D%base%.FileName=%filename% -D%base%.ServerURL=%server% client.xml_promoter.Promoter
endlocal
