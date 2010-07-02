@echo off

title Building the Spring Framework...

cls

@echo.
@echo Building the Spring Framework...
@echo.

"%JAVA_HOME%/bin/java" -cp lib/ant/ant.jar;lib/ant/ant-launcher.jar;lib/ant/ant-trax.jar;lib/ant/ant-junit.jar;lib/junit/junit-3.8.2.jar;lib/clover/clover.jar;"%JAVA_HOME%/lib/tools.jar" org.apache.tools.ant.Main %1

@echo.
