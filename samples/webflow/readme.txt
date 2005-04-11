/*
 * webflow-samples
 *
 * phonebook - central sample demonstrating most webflow features
 * itemlist - demonstrates application transaction tokens and expired flow cleanup
 * fileupload - demonstrates multipart file upload with webflow
 * birthdate - demonstrates Struts integration and the MultiAction
 * sellitem - demonstrates a wizard with conditional transitions and continuations
 *
 * @author Keith Donald
 * @since Mar 2005
 * @version $Id: readme.txt,v 1.2 2005-04-11 06:19:53 kdonald Exp $
 */
 
HOW TO BUILD WEBFLOW SAMPLES

1. copy in the template 'build.properties' file in the same directory as this file to the root directory
of the sample you wish to run.

2. cd to the root directory of the sample you wish to run.

3. tweak the copied 'build.properties' to your environment

4. tweak 'build.bat' to point your environemnt so the ant build system can execute.

5. run 'build dist' to build the application .war file, ready for deployment.

6. if tomcat is installed on your system, run 'build tomcat.server.start' to start it and deploy the
sample application in one step.

7. access the sample at the appropriate URL, e.g http://localhost:8080/phonebook