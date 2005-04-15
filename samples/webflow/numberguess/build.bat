@ECHO OFF
set TOOLS_HOME=../../../
set ANT_LIB_HOME=%TOOLS_HOME%/lib/ant
set JUNIT_LIB_HOME=%TOOLS_HOME%/lib/junit
set CLOVER_LIB_HOME=%TOOLS_HOME%/lib/clover
%JAVA_HOME%/bin/java -cp %ANT_LIB_HOME%/ant.jar;%ANT_LIB_HOME%/ant-launcher.jar;%ANT_LIB_HOME%/ant-junit.jar;%JAVA_HOME%/lib/tools.jar;%JUNIT_LIB_HOME%/junit.jar;%CLOVER_LIB_HOME%/clover.jar org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
