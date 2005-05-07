/*
 * webflow-samples
 *
 * birthdate - demonstrates Struts integration and the MultiAction
 * fileupload - demonstrates multipart file upload with webflow
 * flowlauncher - demonstrates the different ways to launch flows from web pages
 * itemlist - demonstrates application transaction tokens and expired flow cleanup
 * numberguess - demonstrates how to play a game with spring web flow
 * phonebook - central sample demonstrating most webflow features
 * sellitem - demonstrates a wizard with conditional transitions and continuations
 *
 * @author Keith Donald
 * @since Mar 2005
 * @version $Id: readme.txt,v 1.6 2005-05-07 09:00:13 kdonald Exp $
 */

HOW TO BUILD WEBFLOW SAMPLES - FROM RELEASED DISTRIBUTION
---------------------------------------------------------
1. copy in the template 'build.properties' file in the same directory as this file to the
root directory of the sample you wish to run.

2. cd to the root directory of the sample you wish to run.

3. tweak the copied 'build.properties' to your environment

4. tweak 'build.bat' to point your environemnt so the ant build system can execute.

5. run 'build dist' to build the application .war file, ready for deployment.

6. if tomcat is installed on your system, run 'build tomcat.server.start' to start it and
deploy the sample application in one step.

7. access the sample at the appropriate URL, e.g http://localhost:8080/phonebook

HOW TO BUILD WEBFLOW SAMPLES - FROM CVS
---------------------------------------------------------
From the spring root directory, execute from the command line:

1. build alljars

2. build webflow.jar

3. build webflow.support.jar

4. Proceed with the RELEASED DISTRIBUTION instructions above, customizing your local
build.properties for each sample as necessary.  Note: If all wish to do is build the
sample .war file for manual deployment, you shouldn't have to do any build.properties or
build.bat customization--the default properties and paths will suffice.  Property customization
is only necessary if you have custom paths to dependent jar files or wish to automate deployment
with a local tomcat installation.

SAMPLE DEPENDENCIES
------------------------------------------------------------
To see exactly what dependencies each sample depends on to build, review the
'build.webapp.libs' target in the build.xml file for each project.  At a minimum,
this includes:

commons-logging.jar
spring.jar
spring-webflow.jar
spring-webflow-support.jar