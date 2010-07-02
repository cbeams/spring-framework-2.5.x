@ECHO OFF
set TOOLS_HOME=../tools
set ANT_HOME=%TOOLS_HOME%/lib/ant
set JUNIT_HOME=%TOOLS_HOME%/lib/junit
%JAVA_HOME%/bin/java -cp %ANT_HOME%/ant.jar;%ANT_HOME%/ant-launcher.jar;%ANT_HOME%/ant-junit.jar;%JAVA_HOME%/lib/tools.jar;%JUNIT_HOME%/junit.jar org.apache.tools.ant.Main %1
