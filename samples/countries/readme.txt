=========================================
== Spring Countries sample application ==
== focused on some Spring MVC features ==
-----------------------------------------
        This demo can be seen at
  http://spring.jppawlak.info/countries
=========================================
       @author Jean-Pierre Pawlak


1. BUILD AND DEPLOYMENT

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


2. TOPICS
 
a) Localizing an application.
	The two approaches are shown.
	- The most usual uses the same page with localized messages and/or images.
	- The second one uses a page par locale when almost the entire page has to be localized.
b) The use of themes. 
	This use is near to the "alternative" stylesheet feature. 
	But it works also with browsers that have not implemented the alternative
	stylesheets like IE/WIN.  

	In the real world, you will rarely have images with localized text embedded. 
	But if you are faced to this situation, this sample shows how to deal with.
c) Creating PDF and/or Excel documents on the fly.
	This sample shows also how number the pages in a PDF document.

d) The use of composed views as a light (and less powerfull but often sufficient) alternative to tiles.

e) The use of the "refreshedPagedList".


3. SCENARIOS

The scenarios's aim is only to demonstrate the change between a memory implementation
of the countries and a database one. This, just by changing the DAO implementation in 
the configuration files. The code, for its part, deals only with the common interface.

The most complicated scenario is the second one, when the two 
DAOs are used at the same time to be able to pick up the data from memory to the database.

By the way, this second scenario gives the opportunity to create a custom interceptor 
which detects at runtime the scenario in order to propose the database data loading.


Default scenario
----------------

The application is configured for the first scenario, generating data
in memory. This will normally run as-is in most application servers.

Moving to scenario 2
--------------------
This scenario will use the memoryDao like the first.
But, in addition, it will set a secondary databaseDao.
So, you will be able, using the application, to read the data from
the memoryDao and write into the databaseDao.
The main goal of this scenario is to put the countries data in your database.

Two databases have been tested: HSQLDB and MySQL.
Switching from one to the other is only a matter of mapping the right database
to the application known JNDI location.

Now that the server is set up, we will change the application configuration.
Just two little changes have to be made.
1) in war/WEB-INF/applicationContext.xml: comment the scenario 1 and uncomment the 2.
You are so just defining a "secondDaoCountry" bean and its "dataSource" bean.
2) in war/WEB-INF/countries-servlet.xml: uncomment the property "secondDaoCountry"
near the end of the file.

Now that all is set, rebuild the war file with the ant command and deploy the 
new generated war file.

Test the application, and go to the new choice "Copy". A message tell you about 
the work. If you see that the data were not copied, you will have to check the logs.

Moving to scenario 3
--------------------
This is the ultimate goal. You come back to using only one DAO, but you use the
database one instead the memory one as in the first scenario.
1) in war/WEB-INF/applicationContext.xml: comment the scenario 2 and uncomment the 3.
You just pick up the configuration of your previously "secondDaoCountry" to put 
them as primary DAO. You don't need anymore a second Dao.
2) in war/WEB-INF/countries-servlet.xml: re-comment the property "secondDaoCountry"
near the end of the file as you don't have any.

Now, once more, rebuild the war file with the ant command and deploy the 
new generated war file.

You don't have the 'copy' choice, because the application checks the configuration 
and allow this only when a secondDao of 'DATABASE' type is detected.


