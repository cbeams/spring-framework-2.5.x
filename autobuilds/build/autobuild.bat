@echo off
::
:: autobuild.bat
::
:: run with params:
::    -u [optional]  - to force a CVS update of Spring source
::    -k [optional]  - to keepalive the server(s) after tests have run
::    application    - the sample app to build and test
::    server         - the target server to deploy upon
::
:: $Id: autobuild.bat,v 1.4 2004-01-19 20:19:17 davison Exp $
::
:: ---------------------------------------------------------------------------

set USAGE="Usage: %0 [-u] sample-app target-server"

:: ---------------------------------------------------------------------------

:chkprops
    if not exist build.properties echo "*** NO build.properties FILE!  DEFAULT VALUES WILL BE USED ***"
    goto getopts
    
:getopts
    if "%1" == "" goto begin
    set NEWARG=
    if %1 == -u set NEWARG=-Dcvs.update=yes
    if %1 == -k set NEWARG=-Dautobuilds.keepalive=yes
        
    if (%NEWARG%) == () goto getparams
    if not (%NEWARG%) == () set ALLPROPS=%ALLPROPS% %NEWARG%
    shift
    goto getopts
    
:getparams
    if (%APP%) == () set APP=%1
    if (%SERVER%) == () set SERVER=%1
    if %SERVER% == %APP% set SERVER=%1
    shift 
    goto getopts
    
:begin     
    if (%SERVER%) == () goto usage
    if (%APP%) == () goto usage
    if not exist %SERVER%-build.xml goto noserver
    if not exist ..\apps\%APP% goto noapp
    
::  ensure environment exists for 1st use
    ant -q setup
    echo Please see the file %BUILDLOG% for build logging and unit test results
    ant %ALLPROPS% main
    goto end
    
:noserver
    echo Don't know about server %SERVER%
    goto end
    
:noapp
    echo Can't find application %APP% or application is not configured for autobuilds
    goto end
    
:usage
    echo %USAGE%
    goto end
    
:end
    set USAGE=
    set NEWARG=
    set APP=
    set SERVER=
    set ALLPROPS=
    exit
