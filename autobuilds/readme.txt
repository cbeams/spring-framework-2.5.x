/*
 * autobuilds
 *
 * Automated server configuration and deployment & testing of
 * Spring sample applications
 *
 * @author Darren Davison
 * @since Dec 2003
 * @version $Id: readme.txt,v 1.3 2004-01-01 13:44:35 davison Exp $
 */


Overview
------------------------------------------------------------------------------
The autobuilds root is a collection of code, scripts and config files.
Together, they automate builds of the Spring codebase and of the sample Spring
applications, which can then be deployed onto application servers.  The
application servers (which can be downloaded from a network location via http
if needed) are correctly configured after extraction to the target directory.

Once the deployed applications are running on the application server,
automated tests are conducted on them before the servers are shutdown and
removed.  Results of the whole process are logged and date-stamped for future
reference.

Ad-hoc builds/tests can be run to verify locally uncommitted code in the main
tree doesn't break the existing sample applications before committing it, or
alternatively, a machine may be set to automatically run one or more
autobuilds each day after performing a CVS update beforehand.  This results in
additional confidence (hopefully) in the nightly CVS snapshots.

This document refers to all paths as relative to the top level directory of
the Spring project as checked out from CVS.  Thus a path shown as
'/autobuilds/conf' refers to the absolute path
'/projects/spring/autobuilds/conf' if you have checked the Spring code out to
'/projects/spring'.



Preparing autobuilds
------------------------------------------------------------------------------
Firstly you should create a local file called build.properties in the
/autobuilds/build directory.  This file will be ignored by CVS as it will
contain entries relevant to your local environment.  The main build file
'build.xml' contains defaults for the entries that should go into
build.properties, so take a look at this file for info on the local property
types.

To get running quickly, just ensure your build.properties contains at least
the property 'tarballs.dir', which is the absolute location that your
application server downloads are stored at (and will be downloaded to if
required).  If you already have gzipped tarballs of your preferred server you
can move or copy the file to this location and upate the version information
in build.properties accordingly.

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
./autobuild petclinic tomcat5

The autobuild script will check that the application and server specified seem
genuine before embarking on the build.  The application name must be the name
of a Spring sample application residing under the /samples directory.  Server
names are symbolic names and configuration scripts must be present for each
server type that autobuilds supports.  Currently configs are available for:

    tomcat-4.1.x (tomcat4)
    tomcat-5.0.x (tomcat5)
    resin-2.1.x (resin2)
    resin-3.x (resin3)
    jetty-4.2.x (jetty4)

Scripts for jboss3 and (hopefully) wls8 will be available very shortly.

It may not always be possible to deploy all sample apps on all servers of
course.  If the app were to use EJB's for example, it would obviously have to
be deployed on a server with an EJB container.

The build products are all stored under /target/autobuilds - including the
server installations and log files.  The script logs all of its output to a
file whose name is in the format [appname]_[servername]_build_yyyy-mm-dd.log -
for example, the autobuild above conducted on the 17th Dec 2003 would create a
log file called
/target/autobuilds/reports/petclinic_tomcat5_build_2003-12-17.log.  All of the
build process including the result of the unit tests on the running
application is recorded in here.

If you're running an autobuild manually, it may be useful to tail the build
log in another window so you can see the unit test results as they happen.  On
linux/unix this can be done with:

  tail -f /path/to/build.log

  (if you know a win32 equivalent, please let me know)

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
    missing, anonymous access will be used.  This option is more useful for
    machines that do daily build testing on the latest CVS snapshots than
    developers doing adhoc quick build/tests.

    -k 
    Specifying -k will prevent the server(s) from shutting down after the
    tests have been performed.  Only use this option for adhoc, interactive
    builds if you want to be able to fire the application up in a browser and
    look at it after tests have run.  This option will keep alive the
    application server and the HSQL server (if used) until you manually stop
    the build process with a 'Ctrl-C' at the prompt in the command window which
    will then shutdown the server(s).  Access the application at the URL
    http://localhost:13084/appname

Any options other than those documented will not be understood and may be
mis-interpreted as application or server names causing the build to fail
immediately.

Some examples:

    ./autobuild -u petclinic tomcat4 
    will update Spring code from CVS prior to building and deploying petclinic
    on Tomcat 4.1.x

    ./autobuild -k countries resin2 
    will build countries, deploy it to resin 2.1.x and keep it running so that
    you may access it with your web browser

    ./autobuild jpetstore -k -u tomcat5
    should also work fine (CVS update and keep Tomcat 5 running after deploying 
    jpetstore)

    ./autobuild -q petclinic tomcat5
    will give an error "don't know about server petclinic" because the -q is 
    treated as the application name and petclinic as the server name

    ./autobuild -x tomcat5 petclinic
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
the various Spring sample applications and be managed by CVS.  HTTP servers
should be configured to run on the same port as the existing apps, defined by
the build property autobuilds.server.port (currently this is 13084).



Adding new sample applications
------------------------------------------------------------------------------
New sample apps are less likely to be added than new servers, but if one comes
along it can be supported if the application itself satisfies some
dependencies..

1)  it must have a build.xml file in its root directory.

2)  the build.xml must have a 'dist' target which builds the deployment
    units and places them in a directory called 'dist' found in the root.

3)  the 'dist' build process uses spring.jar from the spring-root/dist 
    directory

4)  if the sample app uses an HSQL db, the following three files must
    be present in the db/hsqldb directory from the app root:

    [i]   appname.script
    [ii]  appname.properties
    [iii] appname.data

    see petclinic or jpetstore for examples of this.

Having satisfied these dependencies, application support can be added to the
autobuilds scripts.  If the sample app uses HSQL, it will need to be added as
an HSQL application to the relevant property in the main build.xml file,
otherwise the main file will not need amending.

The (http)unit tests for the new sample app should be created in the
org.springramework.apptests.[sampleapp].AllTests class.  This is a standard
class name that the autobuilds scripts rely on.  Any supporting classes that
are required for the application can also be put in the same package if it
makes sense to do so - the only requirement is the AllTests class with the
'testXXXX' method names.  The source directory for this code is /autobulds/src
with classes output to /target/autobuilds/classes Refer to one of the existing
AllTests classes for more information.



Miscellaneous notes
------------------------------------------------------------------------------
The autobuilds system defines a couple of custom ant tasks for convenience:

1)  AppTestLauncher is a trivial launch program for the junit/httpunit tests
    performed on the sample application.  It avoids all the tedious mucking
    about with ant's optional.jar file which must be built with correct
    dependencies and must match whatever version of ant is being used.

2)  HsqlShutdown will cleanly close a running HSQL server instance by 
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

Items near the top are likely to be imminent.

 - jboss3 / wls8 server builds added
 - httpunit tests for countries / tiles-example completed
 - make anonymous cvs work
 - http downloads implemented
 - skip tests if class not available
 - email options for build logs;
   - configurable address/server
   - one or all logs, or just a unit test summary
   - conditional mail based on build failure / test success/failure
 - allow d/load of .zip tarballs for 'doze
 - handle exceptions / build failures that leave the server(s) running
 - lookup hsql using apps from an external props file to avoid amending 
   build.xml for new apps
 - use gui testrunner as an option (build.props)?
 - create remote webapp / socket listener to accept standard-format reports 
   from autobuilds and aggregate stats as html pages with links to the raw 
   build files.


