@echo off
setlocal

set CONFIG_RESOURCE=resource.client.DeveloperClientConfig

call ../runBrowserSharedScript.cmd %CONFIG_RESOURCE% %1 %2 %3 %4 %5

endlocal
