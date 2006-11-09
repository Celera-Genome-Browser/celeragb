
set var1=%1
set cel_root=c:\Vizroot\Trunk\client-devel

if NOT DEFINED var1 goto end
if %var1%==1.2 set java_home=c:\program files\javasoft\jre\1.2
if %var1%==1.2.2 set java_home=c:\program files\javasoft\jre\1.2
if %var1%==1.3 set java_home=c:\program files\javasoft\jre\1.3.0_02
if %var1%==1.3.1 set java_home=c:\program files\javasoft\jre\1.3.1
:end