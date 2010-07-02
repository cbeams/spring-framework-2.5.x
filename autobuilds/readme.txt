/*
 * autobuilds
 *
 * Automated server configuration and deployment & testing of
 * Spring web application(s)
 *
 * @author Darren Davison
 * @since Dec 2003
 * @version $Id: readme.txt,v 1.9 2005-08-14 00:26:31 aarendsen Exp $
 */


Overview
------------------------------------------------------------------------------
The autobuilds root is a collection of code, scripts and config files.
Together, they automate builds of the Spring codebase and of the specially
prepared Spring applications.  The applications are built in such a way that
they exercise the maximum amount of Spring functionality (for example using
Velocity, XSLT, JSP, Re-direct and PDF views all within the same app).  This
functionality is then tested via the extensive HttpUnit tests available which
effectively remote-control the running application and verify the HTTP
responses and HTML (where appropriate) returned from various URL's.  Although
the applications are contrived to use as much Spring functionality as
possible, they are nevertheless fully functioning applications that can be
deployed onto any suitably configured application server should you so wish.

The autobuilds module intends to add support for the largest number of servers
feasible so that the application(s) can be deployed and tested on all of them
via scheduled daily scripts.  Each day, an update of the Spring code can be
taken from CVS (automatically by the code) and thus each daily run will test
the latest Spring functionality in a real application, on real application
servers.  An alternative usage may be ad-hoc builds/tests run by developers to
verify that locally amended code in the main /src tree doesn't break the 
sample application before they commit.

Autobuilds can download application servers via http if needed, controlled by
build properties and will always extract and configure them from scratch in a
sandboxed environment.

Once the deployed applications are running on the application server, the
automated tests are conducted on them before the servers are shutdown and
removed.  Results of the whole process - build logs, application logs, server
logs and test reports - are date-stamped and held in the reports directory for
future reference.

This document refers to all paths as relative to the top level directory of
the Spring project as checked out from CVS.  Thus a path shown as
'/autobuilds/conf' refers to the absolute path
'/home/projects/spring/autobuilds/conf' if you have checked the Spring code
out to '/home/projects/spring'.



Preparing autobuilds
------------------------------------------------------------------------------
Firstly you should create a local file called build.properties in the
/autobuilds/build directory.  This file will be ignored by CVS as it will
contain entries relevant to your local environment.  The main build file
'build.xml' contains defaults for the entries that should go into
build.properties, so take a look at this file for info on the local property
types.

To get running quickly, just ensure your build.properties contains at least
the following properties (copy/paste/amend as necessary):


# ----------- start required build.properties -----------

# the absolute location that your server tarballs live in (or will
# be downloaded to as required).  If you already have gzipped tarballs 
# of your preferred server you can move or copy the file to this location 
# and upate the version information in build.properties accordingly.
tarballs.dir=c:/downloads

# your email address and host if you want to receive failure reports by mail.
# eventually, all reports will be forwarded to a tracking application
# (built with Spring of course!) on the web.
autobuilds.mail.sendto=you@your.domain
autobuilds.mail.host=your.smtp.server

# ------------ end required build.properties ------------


Note that when setting the compressed tarball names, only use the tar.gz /
.tgz versions of files since the build script expects gzip compression.  This
will work equally well on windows as well as Unix/linux and WinZip can also
handle this file format for manual use.



Running autobuilds
------------------------------------------------------------------------------
To run an adhoc autobuild manually from the command line, change to the
autobuilds build directory at /autobuilds/build and execute the autobuilds
script with;

(unix / linux..)
./autobuild sampleApp targetServer

(win32..)
autobuild sampleApp targetServer

For example;
./autobuild jpetstore tomcat5

The autobuild script will check that the application and server specified seem
genuine before embarking on the build.  The application name must be the name
of a Spring sample application residing under the autobuilds/apps directory.  
Server names are symbolic and configuration scripts must be present for each
server type that autobuilds supports.  Currently configs are available for:

    tomcat-4.1.x (tomcat4)
    tomcat-5.0.x (tomcat5)
    resin-2.1.x (resin2)
    resin-3.x (resin3)
    jetty-4.2.x (jetty4)
    jboss-3.2.x (jboss3)

Scripts for wls8 (and possibly websphere5) will hopefully be available shortly.

The normal sample apps are no longer used for autobuilds as they serve a
slightly different purpose.  Presently the only two available apps are:

  1) buildtest - the simplest possible web app which is used to verify that 
     the autobuilds scripts and server configs are working.

  2) jpetstore - a modified version of the JPetStore sample application 
     which acts as the real test app for Spring functionality.

The sandbox is under /target/autobuilds - here, all the server installations,
compiled java classes and the log files are found.  The script logs all of its
output to a file whose name is in the format;

[appname]_[servername]_build_yyyy-mm-dd.log

for example, the autobuild above conducted on the 17th Dec 2003 would create a
log file called

/target/autobuilds/reports/jpetstore_tomcat5_build_2003-12-17.log.

All of the build process output is recorded in here.  The HttpUnit test reports
would be written to jpetstore_tomcat5_unittests_2003-12-17.xml in the
same directory.

Additional logs are generated for the output from the application server
itself (i.e. tomcat5_server.log), the application running on the application
server (i.e. petclinic_tomcat5_app_2003-12-17.log) and HSQLDB if it is needed
for the particular sample app (hsqldb.log).

The autobuild script takes a couple of options which can be specified on the
command line:

    -u 
    Specifying -u will cause the autobuild to initially attempt to update
    the /src directory from CVS.  Ensure your local build.properties file
    contains an entry 'cvs.rsh=ssh' if you are a Spring developer since the
    autobuild will use this method to pull the update.  If the property is
    missing, CVS update will be ignored.  This option is more useful for
    machines that do daily build testing on the latest CVS snapshots than
    developers doing adhoc quick build/tests.

    -k 
    Specifying -k will prevent the server(s) from shutting down after the
    tests have been performed.  Only use this option for adhoc, interactive
    builds if you want to be able to fire the application up in a browser and
    look at it after tests have run.  This option will keep alive the
    application server and the HSQL server (if used) until you continue the
    build process by pressing 'Enter' at the prompt in the command window 
    which will then shutdown the server(s).  Access the application at the URL
    http://localhost:13084/appname

Any options other than those documented will not be understood and may be
mis-interpreted as application or server names causing the build to fail
immediately.

Some examples:

    ./autobuild -u buildtest tomcat4 
    will update Spring code from CVS prior to building and deploying buildtest
    on Tomcat 4.1.x

    ./autobuild -k jpetstore resin2 
    will build jpetstore, deploy it to resin 2.1.x and keep it running 
    so that you may access it afterwards with your web browser

    ./autobuild jpetstore -k -u tomcat5
    should also work fine (CVS update and keep Tomcat 5 running after 
    deploying jpetstore)

    ./autobuild -q jpetstore tomcat5
    will give an error "don't know about server jpetstore" because the 
    -q is treated as the application name and jpetstore as the server
    name

    ./autobuild -x tomcat5 jpetstore
    will error "Can't find application -x" because -x is treated as the 
    application name and tomcat5 as the server name



Adding new servers
------------------------------------------------------------------------------
To add autobuilds support for a new server application, several tasks must be
actioned.  For a fictional J2EE server called 'jApps', currently at version
6.1.5, you might choose a symbolic name of 'jApps6'.  The following file and
directory would then be needed:

1)  /autobuilds/build/jApps6-build.xml

This ant build file must contain the following targets (it's 'interface' if
you like):

    [i]    extract-server
    [ii]   deploy
    [iii]  start-server
    [iv]   stop-server
    
These targets contain tasks specific to the jApps server.  The script will
receive all of the properties in the local build.properties file, and any
others defined on the command line or within the main build.xml file.  In
particular, it will receive a ${target.app} property denoting the sample
application.  Follow the conventions of the existing server build files.


2)  /autobuilds/conf/servers/jApps6

Any files placed under the /autobuilds/conf/servers/jApps6 directory will be
copied over any existing files or directories in the actual server
installation after it has been unzipped and extracted from the tarball.  For
example, the file structure under the tomcat5 directory consists of
'conf/server.xml' which will overwrite the default conf/server.xml of the
target installation.  Configuration files should be created and tested with
the autobuilds test applications and should be managed by CVS.  HTTP servers
should be configured to run on the same port as the existing apps, defined by
the build property autobuilds.server.port (currently this is 13084 for no 
particular reason).



Adding new sample applications
------------------------------------------------------------------------------
New sample apps are less likely to be added than new servers, but if one comes
along it can be supported if the application itself satisfies some
dependencies..

1)  it must have a build.xml file in its root directory.

2)  the build.xml must have a 'dist' target which builds the deployment
    units and places them in a directory called 'dist' found in the root.

3)  the 'dist' build process copies spring.jar from the spring-root/dist 
    directory to wherever the sample app needs it (WEB-INF/lib for example)

4)  if the sample app uses an HSQL db, the following three files must
    be present in the db/hsqldb directory from the app root:

    [i]   appname.script
    [ii]  appname.properties
    [iii] appname.data

    see jpetstore for examples of this.


Having satisfied these dependencies, application support can be added to the
autobuilds scripts.  If the sample app uses HSQL, it will need to be added as
an HSQL application to the relevant property in the main build.xml file,
otherwise the main file will not need amending.

The (http)unit tests for the new sample app should be created in the
org.springramework.apptests.[sampleapp].AllTests class.  This is a standard
class name that the autobuilds scripts rely on.  Any supporting classes that
are required for the application can also be put in the same package if it
makes sense to do so - the only requirement is the AllTests class with the
'testXXXX' method names.  The source directory for this code is /autobuilds/src
with classes output to /target/autobuilds/classes.  Refer to one of the 
existing AllTests classes for more information.

Notes for WLS9.0
------------------------------------------------------------------------------
WebLogic 9.0 is supported as well through the autobuilds system, although
there are some special things you need to know and do.

Prerequisites to run the autobuilds applications on WebLogic 9
- Install WebLogic
- In build.properties (in autobuilds/build dir), enter the following properties
  and modify if needed
    bea.home=c:/bea
	bea.domaindir=c:/bea/user_projects/domains
	bea.domain=spring
	bea.user=weblogic
	bea.password=weblogic	
	autobuilds.server.http.port=7001
	
The WebLogic server is started using the wls9.xml build file. It starts up 
the server, deploys the appropriate application and stops the server after
the tests have been run.



Miscellaneous notes
------------------------------------------------------------------------------
The autobuilds system defines a custom ant task for convenience:

1)  HsqlShutdown will cleanly close a running HSQL server instance by 
    sending the SHUTDOWN command through a Statement.execute() method.  The 
    command requires a host and port to be specified as task attributes, and
    optionally can take a 'compact' (boolean) attribute to have this 
    specified on the SHUTDOWN command.



To-do list
------------------------------------------------------------------------------
The following list of items are intended for completion at some point in the
future.  Roughly prioritised, items at the top are more likely to appear
first.  Please drop a line to the dev list
(http://lists.sourceforge.net/lists/listinfo/springframework-developer) if you
think some functionality is missing from autobuilds and is not included here,
or if you'd like to see something moved up this list.

 - wls8 / websphere5 server builds added
 - support for plugging in different database implementations 
 - create remote webapp / socket listener to accept standard-format reports 
   from autobuilds and aggregate stats as html pages with links to the raw 
   build files.
 - allow d/load of .zip tarballs for 'doze
 - handle exceptions / build failures that leave the server(s) running
 - use gui testrunner as an option (build.props / based on -k option)?
 - make anonymous cvs work
