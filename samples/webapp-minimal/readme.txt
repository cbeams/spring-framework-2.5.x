This is a skeleton for the simplest possible Spring web application.

It shows the necessary web.xml and Spring-specific configuration (in the WEB-INF directory)
for a complete application. It also provides a build script and simple structure for your
application.

The directories have the following purpose:
* "src": Java source code. The binaries will eventually end up in the WAR's WEB-INF/classes directory.
* "war": documents such as JSP. Also contains WEB-INF directory containing deployment descriptors.
* "dist": directory created by build script containing WAR deployment unit
* "lib": binaries required in the WAR's WEB-INF/lib directory. You'll need to copy spring.jar,
  commons-logging.jar and servlet.jar here for compilation to succeed (see lib/readme.txt).

Type "ant warfile" to build a WAR deployment unit.

