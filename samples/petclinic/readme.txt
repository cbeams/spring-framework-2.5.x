=========================================
== Spring Petclinic sample application ==
=========================================

@author Ken Krebs
@author Juergen Hoeller


1. DATA ACCESS STRATEGIES

Since release 1.0 M2, Petclinic features alternative DAO implementations and
application configurations for JDBC and Hibernate, on HSQL and MySQL.
The default Petclinic configuration is Hibernate on HSQL; to be able to build
and run it, the Spring distribution comes with Hibernate jar files.
See "WEB-INF/web.xml", "WEB-INF/applicationContext-hibernate.xml", and
"WEB-INF/applicationContext-jdbc.xml" for details.

Both data access strategies can work with JTA for transaction management,
by activating the JtaTransactionManager and a JndiObjectFactoryBean that
refers to a transactional container DataSource. The default for Hibernate
is HibernateTransactionManager; for JDBC, DataSourceTransactionManager:
They allow to work with any locally defined DataSource. In the default case,
the sample configurations specify Spring's non-pooling DriverManagerDataSource.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.

You can also invoke an existing installation of Ant, with this directory
as execution directory. Note that you need to do this to execute the "test"
target, as you need the JUnit task from Ant's optional.jar (not included).

To execute the web application with its default settings, simply start the
HSQLDB instance in the "db/hsqldb" directory, for example using "server.bat".
For MySQL, you'll need to use the corresponding schema and load scripts
in the "db/mysql" subdirectory. In the local case, the JDBC settings can
be adapted in "WEB-INF/jdbc.properties". With JTA, you need to set up
corresponding DataSources in your J2EE container.

Note on enabling Log4J:
- Log4J is disabled by default, due to JBoss issues
- drop a log4j.jar into the deployed "WEB-INF/lib" directory
- uncomment the root category in "WEB-INF/classes/log4j.properties"
- uncomment the Log4J listener in "WEB-INF/web.xml"

