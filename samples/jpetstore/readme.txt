=========================================
== Spring JPetStore sample application ==
=========================================
@author Juergen Hoeller
Based on Clinton Begin's JPetStore (http://www.ibatis.com).

Features a Spring-managed middle tier with iBATIS Database Layer as data access
strategy, in combination with Spring's transaction and DAO abstractions.
Can work with local JDBC transactions or JTA, with the latter on two databases.
Uses the same data model and demo contents as the original JPetStore.
See "WEB-INF/applicationContext.xml" for details.

Offers two alternative web tier implementations with the same user interface:
one based on Spring's web MVC, and one based on Struts 1.1. The latter is close
to the original JPetStore but reworked for JSTL, to make the JSP implementations
as comparable as possible. See "WEB-INF/web.xml", "WEB-INF/petstore-servlet.xml",
and "WEB-INF/struts-config.xml" for details.

Compared to the original JPetStore, this implementation is significantly
improved in terms of internal structure and loose coupling: Leveraging Spring's
application context concept, there's a central place for wiring application
objects now. The most notable improvement is the former PetStoreLogic, now
called PetStoreFacade: It it not concerned with transaction details anymore.

Note that the Spring-based web tier implementation is deliberately similar to
the Struts-based one and does not aim to improve in terms of in-place error
messages or the like. The inclusion of two web tier alternatives outlines the
differences as well as the similarities in the respective programming model,
and also illustrates the different configuration styles.


This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "ant.bat" in this directory for available targets (e.g. "ant build",
"ant warfile"). Note that to start Ant this way, you'll need an XML
parser in your classpath (e.g. in jre/lib/ext; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for war file creation.
The war file will be created in the 'dist' directory.

To be able to execute the web application with its default settings,
you'll need to start the HSQLDB instance in the db/hsqldb directory
first, using server.bat.

