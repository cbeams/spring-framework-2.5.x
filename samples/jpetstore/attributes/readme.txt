
This directory tree shows how to use Commons Attributes to achieve
autoproxying. There's no need to use a TransactionFactoryProxyBean:
application classes that contain transaction attributes will be
automatically intercepted to perform declarative transaction management.

We need our own build file here to compile attributes as part of the build
process. 

applicationContext.xml also illustrates the use of the Apache Commons DBCP
connection pool: a good solution when running in a web container that
doesn't provide a JNDI datasource.

The build script and the applicationContext.xml file are very similar to
those in the root directory. Only trivial modification is required.
However, we've put them in this separate directory to avoid complicating
the main build script and XML bean definition files by providing too many alternatives.