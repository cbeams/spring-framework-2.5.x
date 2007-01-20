=========================================
== Spring PetClinic sample application ==
=========================================

@author Ken Krebs
@author Juergen Hoeller
@author Rob Harrop
@author Costin Leau


1. DATA ACCESS STRATEGIES

PetClinic features alternative DAO implementations and application configurations
for JDBC, Hibernate, Oracle TopLink and JPA, with HSQLDB and MySQL as target
databases. The default PetClinic configuration is JDBC on HSQLDB.
See "WEB-INF/web.xml" and "WEB-INF/applicationContext-*.xml" for details;
a simple comment change in web.xml switches between the data access strategies.

The JDBC version of PetClinic also demonstrates JMX support: it exposes the
CachingClinic management interface (implemented by its Clinic object) via JMX.
On JDK 1.5, you can start up the JDK's JConsole to see and use the exported bean.
On JDK < 1.5, your application server's JMX infrastructure needs to be used.
Note that special setup is necessary on WebLogic <= 8.1 and on JBoss:
see "jmxExporter" definition in "applicationContext-jdbc.xml" for details!

The Spring distribution comes with all required Hibernate and TopLink Essentials
(JPA RI) jar files to be able to build and run PetClinic on those two ORM tools. For
standard TopLink, only a minimal toplink-api.jar is included in the Spring distribution.
To run PetClinic with TopLink, download TopLink 10.1.3 or higher from the Oracle
website (http://www.oracle.com/technology/products/ias/toplink), install it and
copy "toplink.jar" and "xmlparserv2.jar" into Spring's "lib/toplink" directory.

All data access strategies can work with JTA for transaction management,
by activating the JtaTransactionManager and a JndiObjectFactoryBean that
refers to a transactional container DataSource. The default for JDBC is
DataSourceTransactionManager; for Hibernate, HibernateTransactionManager;
for TopLink TopLinkTransactionManager; for JPA, JpaTransactionManager.
Those local strategies allow for working with any locally defined DataSource.

Note that in the default case, the sample configurations specify Spring's
non-pooling DriverManagerDataSource as local DataSource. You can change
the DataSource definition to a Commons DBCP BasicDataSource, for example,
to get proper connection pooling.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant 1.5 or higher.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). You can use "warfile.bat" as a shortcut for WAR file
creation. The WAR file will be created in the "dist" directory.

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
- rename "WEB-INF/classes/log4j.properties.rename" to "log4j.properties"
- uncomment the Log4J listener in "WEB-INF/web.xml"


3. JPA ON TOMCAT

Notes on using the Java Persistence API (JPA) on Apache Tomcat 4.x or higher, with a
persistence provider that requires class instrumentation (such as TopLink Essentials):

To use JPA class instrumentation, Tomcat has to be instructed to use a custom class
loader which supports instrumentation. See the JPA section from Spring reference
documentation for complete details.

The basic steps are:
- copy "spring-tomcat-weaver.jar" from Spring distribution to "TOMCAT_HOME/server/lib"
- if you're running on Tomcat 4.x or 5.x, modify "TOMCAT_HOME/conf/server.xml"
and add a new "<Context>" element for petclinic (see below)
- if you're running on Tomcat 5.x, you can also deploy the WAR including
"META-INF/context.xml" from this application's "war" directory

<Context path="/petclinic" docBase="/petclinic/location" ...>
  <!-- please note that useSystemClassLoaderAsParent is available since Tomcat 5.5.20; remove it if previous versions are being used -->
  <Loader loaderClass="org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader" useSystemClassLoaderAsParent="false"/>
...
</Context>

