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
:: $Id: autobuild.bat,v 1.1 2003-12-19 01:44:36 davison Exp $
::
:: ---------------------------------------------------------------------------

for /f "tokens=2-4 skip=1 delims=(-)" %%g in ('echo.^|date') do (
  for /f "tokens=2 delims= " %%a in ('date /t') do (
     set v_first=%%g
     set v_second=%%h
     set v_third=%%i
     set v_all=%%a
  )
)
set %v_first%=%v_all:~0,2%
set %v_second%=%v_all:~3,2%
set %v_third%=%v_all:~6,4%

set USAGE="Usage: %0 [-u] sample-app target-server"
set LOGGER=org.apache.tools.ant.DefaultLogger
set LISTENER=org.apache.tools.ant.XmlLogger

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
    if not exist ..\..\samples\%APP% goto noapp
    set BUILDLOG=..\..\target\autobuilds\reports\%APP%_%SERVER%_build_%yy%-%mm%-%dd%.log

::  ensure environment exists for 1st use
    ant setup
    ant -logger %LOGGER% -logfile %BUILDLOG% %ALLPROPS% main
    goto end
    
:noserver
    echo Don't know about server %SERVER%
    goto end
    
:noapp
    echo Can't find application %APP%
    goto end
    
:usage
    echo %USAGE%
    goto end
    
:end
    set USAGE=
    set BUILDLOG=
    set LOGGER=
    set LISTENER=
    set NEWARG=
    set APP=
    set SERVER=
    set ALLPROPS=
    exit
