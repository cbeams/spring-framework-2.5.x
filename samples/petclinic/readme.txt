=========================================
== Spring Petclinic sample application ==
=========================================
@author Ken Krebs
@author Juergen Hoeller

Since release 1.0 M2, Petclinic features alternative DAO implementations and
application configurations for JDBC and Hibernate, on HSQL and MySQL.
The default Petclinic configuration is Hibernate on HSQL; to be able to build
and run it, the Spring distribution comes with Hibernate jar files now.
See "WEB-INF/web.xml", "WEB-INF/applicationContext-hibernate.xml", and
"WEB-INF/applicationContext-jdbc.xml" for details.

Both data access strategies can work with JTA for transaction management,
by activating the JtaTransactionManager and a JndiObjectFactoryBean that
refers to a transactional container DataSource. The default for Hibernate
is HibernateTransactionManager; for JDBC, DataSourceTransactionManager:
They allow to work with any locally defined DataSource; in the default case,
the sample configurations specify Spring's non-pooling DriverManagerDataSource.


This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "ant.bat" in this directory for available targets (e.g. "ant build",
"ant warfile"). Note that to start Ant this way, you'll need an XML
parser in your classpath (e.g. in jre/lib/ext; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for war file creation.
The war file will be created in the 'dist' directory.

You can also invoke an existing installation of Ant, with this
directory as execution directory. Note that you need to do this
to execute the "test" target, as you need the JUnit task from
optional.jar (not included here).

To be able to execute the web application with its default settings,
you'll need to start the HSQLDB instance in the db/hsqldb directory
first, using server.bat. If you are using JBoss, you must instead have
copied the as/jboss/3.X.X xml files in the deploy dir and JBoss must
be running while you call the Ant with target all or test.

Note on enabling Log4J:
- Log4J is disabled by default, due to JBoss issues
- drop a log4j.jar into the deployed war/WEB-INF/lib directory
- uncomment the root category in war/WEB-INF/classes/log4j.properties
- uncomment the Log4J listener in war/WEB-INF/web.xml

Note for JBoss users:
- in war/WEB-INF/web.xml: leave the Log4J listener commented out
- in build.properties: uncomment the jboss.root property
- in build.properties: set the port number for HSQL to 1701
- if using JBoss 3.2.x, copy db/jboss/3.2.x/*.xml in your deploy directory
- for the time being, no database descriptor is provided for other JBoss versions

 