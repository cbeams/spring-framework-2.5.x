THE SPRING FRAMEWORK, release 1.0 M3 (November 2003)
----------------------------------------------------
http://www.springframework.org


1. INTRODUCTION

Spring is a J2EE application framework based on code published in "Expert One-on-One J2EE Design and Development"
by Rod Johnson (Wrox, 2002).

Spring includes:

* Powerful JavaBeans-based configuration management, applying Inversion-of-Control principles. This makes
wiring up applications quick and easy. No more singletons littered throughout your codebase; no more arbitrary
properties files. One consistent and elegant approach everywhere.

* JDBC abstraction layer that offers a meaningful exception hierarchy (no more pulling vendor codes out of
SQLException), simplifies error handling, and greatly reduces the amount of code you'll need to write.
You'll never need to write another finally block to use JDBC again.

* Generic abstraction layer for transaction management, allowing for pluggable transaction managers, and making
it easy to demarcate transactions without dealing with low-level issues. Strategies for JTA and a single JDBC
DataSource are included.

* Integration with Hibernate and JDO, in terms of resource holders, DAO implementation support, and transaction
strategies. First-class Hibernate support with lots of IoC convenience features, solving many typical Hibernate
integration issues.

* AOP functionality, fully integrated into Spring configuration management. You can AOP-enable any object managed
by Spring, adding aspects such as declarative transaction management. With Spring, you can have declarative
transaction management without EJB... even without JTA, if you're using a single database in Tomcat or another
web container without JTA support.

* Flexible MVC web application framework, built on core Spring functionality. This framework is highly configurable
via strategy interfaces, and accommodates multiple view technologies. Note that a Spring middle tier can easily
be combined with a web tier based on any other web MVC framework, like Struts, WebWork, or Tapestry.

You can use all of Spring's functionality in any J2EE server, and most of it in non-managed environments too.
A central focus of Spring is to allow for reusable business and data access objects that are not tied to specific
J2EE services. Such objects can be used in J2EE environments with or without EJB, standalone applications, test
environments, etc without any hassle.

Spring has a layered architecture. All its functionality builds on lower levels. So you can e.g. use the JavaBeans
configuration management without using the MVC framework or AOP support. But if you use the MVC framework or AOP
support, you'll find they build on the configuration framework, so you can apply your knowledge about it immediately.


2. RELEASE INFO

The Spring Framework is released under the terms of the Apache Software License (see license.txt). This is the
fifth public release towards 1.0 final. Since 1.0 M1, the framework uses the package name "org.springframework",
superseding the former "com.interface21" that dated back to the Wrox version. The next release will be 1.0 RC1;
we do not plan any further milestone releases.

The Spring Framework requires J2SE 1.3 and J2EE 1.3 (Servlet 2.3, JSP 1.2, JTA 1.0, EJB 2.0).
Note that J2EE 1.2 (Servlet 2.2, JSP 1.1) is good enough if not using Spring's web MVC or EJB support.
Integration is provided with Log4J 1.2, CGLIB 1.0, Hibernate 2.0, JDO 1.0, Caucho's Hessian/Burlap 2.1/3.0,
JSTL 1.0, Velocity 1.3, Tiles 1.1, Commons FileUpload, Jason Hunter's COS, and more.

Release contents:
* "src" contains the Java source files
* "dist" contains various Spring Jar files
* "lib" contains the most important third-party libraries
* "docs" contains general and API documentation
* "samples" contains demo application and skeletons

Latest info is available at the public website: http://www.springframework.org
Project info at the SourceForge site: http://sourceforge.net/projects/springframework

This product includes software developed by the Apache Software Foundation (http://www.apache.org).


3. DISTRIBUTION JAR FILES

The "dist" directory contains the following overlapping jar files for use in applications. Each addresses a
typical usage of the Spring Framework, specifying the respective contents and third-party dependencies.
Libraries in brackets are optional, i.e. just necessary for certain functionality.

* "spring-beans" (~115 KB)
- Target: minimal bean container for wiring up within restricted environments like applets
- Contents: bean container, core utilities
- Dependencies: Commons Logging

* "spring-context" (~265 KB)
- Target: basic application context for use outside a J2EE container
- Contents: bean container, utilities, AOP framework, application context, validation framework
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB)

* "spring-jdbc" (~420 KB)
- Target: application context with transaction framework and JDBC support
- Contents: bean container, utilities, AOP framework, application context, validation framework,
transaction framework, JDBC support
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB; JTA)

* "spring" (~680 KB)
- Target: full application framework for use within a J2EE container (and suitable where jar size does not matter)
- Contents: bean container, utilities, AOP framework, EJB support, transaction framework, JDBC support, O/R Mapping
support, application context, web application context, validation framework, web MVC framework, remoting support
- Dependencies: Commons Logging, (Log4J, AOP Alliance, CGLIB; JTA; Hibernate, JDO; EJB 2.0, Servlet 2.3, JSP 1.2,
JSTL; Velocity, iText, POI; Hessian, Burlap)

Note: The above lists of third-party libraries assume J2SE 1.4 as foundation. For J2SE 1.3, an XML parser like
Xerces, the JDBC 2.0 standard extension interfaces, and JNDI have to be added when using XML bean definitions,
JDBC DataSource setup, and JNDI lookups, respectively.

Note: To use the JSP expression language for arguments of Spring's web MVC tags, the Jakarta implementation of
the JSTL (standard.jar) has to be available in the class path. Else, any JSTL implementation will do.


4. WHERE TO START?

Documentation can be found in the "docs" directory:
* "The Spring Framework - A Lightweight Container"
* "Data Access and Transaction Abstraction with the Spring Framework"
* "Container Resources vs Local Resources"
* "Web MVC with the Spring Framework"
* "Developing a Spring Framework MVC application step-by-step"
* various configuration and integration tutorials

Documented sample applications and skeletons can be found in "samples":
* "countries"
* "petclinic"
* "tiles-example"
* "webapp-minimal"
* "webapp-typical"

Release 1.0 M2 introduced a reworked version of Petclinic, now featuring alternative DAO implementations and
application configurations for JDBC and Hibernate, on HSQL and MySQL. The default Petclinic configuration is
Hibernate on HSQL; to be able to build and run it, the Spring distribution comes with Hibernate jar files now.

"Expert One-on-One J2EE Design and Development" discusses many of Spring's design ideas in detail.
Note: The code examples in the book refer to the original framework version that came with the book.
Thus, they need to be adapted to work with the current Spring release.
