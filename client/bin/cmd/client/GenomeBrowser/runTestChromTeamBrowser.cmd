@echo off
setlocal

set CONFIG_RESOURCE=resource.client.TestChromTeamClientConfig

../runBrowserSharedScript.cmd %CONFIG_RESOURCE% %1 %2 %3 %4 %5 
endlocal