=========================================
== JCA CCI sample application          ==
=========================================

@author Thierry Templier


1. MOTIVATION

The aim of the sample is to show how to use the support
of JCA CCI 1.0 in Spring.
It uses the sample JCA connector of the j2sdkee version 1.3
modified to be used with Hypersonic and execute sql requests
directly on the CCI interaction spec. The connector of the
sample only supports local transactions and works in a
standalone mode.
To use it in a managed mode, you must package the jar
in a spring-cciblackbox-tx.rar and deploy on your application
server (jca 1.0 compliant).
The structure of your rar file must be as following:
- spring-cciblackbox-tx.jar
+ META-INF/
  - ra.xml
  - MANIFEST.MF

The MANIFEST.MF could be empty and here is the ra.xml file:
<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE connector PUBLIC '-//Sun Microsystems, Inc.//DTD Connector 1.0//EN' 'http://java.sun.com/dtd/connector_1_0.dtd'>

<connector>
    <display-name>CciBlackBoxLocalTx</display-name>
    <vendor-name>Java Software</vendor-name>
    <spec-version>1.0</spec-version>
    <eis-type>JDBC Database</eis-type>
    <version>1.0</version>
    <resourceadapter>
        <managedconnectionfactory-class>com.sun.connector.cciblackbox.CciLocalTxManagedConnectionFactory</managedconnectionfactory-class>
        <connectionfactory-interface>javax.resource.cci.ConnectionFactory</connectionfactory-interface>
        <connectionfactory-impl-class>com.sun.connector.cciblackbox.CciConnectionFactory</connectionfactory-impl-class>
        <connection-interface>javax.resource.cci.Connection</connection-interface>
        <connection-impl-class>com.sun.connector.cciblackbox.CciConnection</connection-impl-class>
        <transaction-support>LocalTransaction</transaction-support>
        <config-property>
            <config-property-name>ConnectionURL</config-property-name>
            <config-property-type>java.lang.String</config-property-type>
            <config-property-value>jdbc:cloudscape:rmi:CloudscapeDB;create=true</config-property-value>
        </config-property>
        <authentication-mechanism>
            <authentication-mechanism-type>BasicPassword</authentication-mechanism-type>
            <credential-interface>javax.resource.security.PasswordCredential</credential-interface>
        </authentication-mechanism>
        <reauthentication-support>false</reauthentication-support>
    </resourceadapter>
</connector>



2. BUILD AND DEPLOYMENT

This directory contains the standalone app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat launch"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).


3. START THE HYPERSONIC DATABASE

Before launching the sample application, the sample database must be started
in server mode on the port 9001. You can use ant to start it: Run "build.bat starthsqldb".
You can too launch the Hypersonic administration tool: Run "build.bat adminhsqldb".
The schema of the database is yet created and a sample row is inserted too.


4. START THE SAMPLE APPLICATION

To launch the sample application, run "build.bat launch".