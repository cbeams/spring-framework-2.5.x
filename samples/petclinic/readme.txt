-- Spring Petclinic demo --
@author Ken Krebs
@author Juergen Hoeller

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "ant" in this directory for available targets
(e.g. "ant build", "ant warfile"). Note that to start
Ant this way, you'll need an XML parser in your classpath
(e.g. jre/lib/ext; included in JDK 1.4).

You can also invoke an existing installation of Ant, with this
directory as execution directory. Note that you need to do this
to execute the "test" target, as you need the JUnit task from
optional.jar (not included here.)

Note that to be able to execute the web app with its default
settings, you'll need to start the HSQLDB instance in the
db/hsqldb directory first, using server.bat.

Note 2: If you are using JBoss, the above note doesn't apply.
You must instead have copied the as/jboss/3.X.X xml files
in the deploy dir and JBoss must be running while you 
call the Ant with target all or test.

Note on enabling Log4J:
- Log4J is disabled by default, due to JBoss issues (see below)
- rename war/WEB-INF/lib/log4j-1.2.8.renametojar to log4j-1.2.8.jar
- uncomment the root category in war/WEB-INF/classes/log4j.properties
- uncomment the Log4J listener in war/WEB-INF/web.xml

Note for JBoss users:
- in war/WEB-INF/web.xml: leave the Log4J listener commented out
- in build.properties: uncomment the jboss.root property
- in build.properties: set the port number for HSQL to 1701
- if using JBoss 3.2.x, copy the files db/jboss/3.2.x/*.xml in your deploy directory
- for time being no database descriptor is provided for other JBoss versions, you have to write them
 