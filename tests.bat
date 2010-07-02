@echo off

title Running Spring Framework Tests...

cls

@echo.
@echo Running Spring Framework Tests...
@echo.

"%JAVA_HOME%/bin/java" -cp lib/ant/ant.jar;lib/ant/ant-launcher.jar;lib/ant/ant-trax.jar;lib/ant/ant-junit.jar;lib/junit/junit-4.4.jar;lib/clover/clover.jar;"%JAVA_HOME%/lib/tools.jar" org.apache.tools.ant.Main clean tests

@echo.

pause
