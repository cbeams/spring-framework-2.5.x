=========================================
== Spring Countries sample application ==
=========================================

@author Jean-Pierre Pawlak
@author Juergen Hoeller


1. MOTIVATION

This sample application demonstrates a couple of special Spring Web MVC features,
such as:

* paged list navigation
* locale and theme switching
* localized view definitions
* page composition through view definitions
* generation of PDF and Excel views


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.

Note on enabling Log4J:
- Log4J is disabled by default, due to JBoss issues
- drop a log4j.jar into the deployed war/WEB-INF/lib directory
- uncomment the root category in war/WEB-INF/classes/log4j.properties
- uncomment the Log4J listener in war/WEB-INF/web.xml

