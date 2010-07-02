==  JPetStore sample application  ==

@author Juergen Hoeller
@author Darren Davison
Based on Clinton Begin's JPetStore (http://www.ibatis.com).


This is a modified build of the JPetStore application in the Spring 
/samples directory (the real demo app).  The purpose of this version
is to provide a platform for automated building / deploying and 
testing of an application using nightly CVS snapshots of the Spring
code.  The app is based on JPetStore as it looked at the time that
Spring-1.0-m4 was released (Jan 2004) but is expected to diverge
quite quickly.

The application is designed to use as much Spring functionality as possible,
even where it would not be realistic in the real world.

The /autobuilds module is what controls the automated testing and the 
configuration of the application on multiple J2EE servers.  See the 
docs in this directory for more detailed information.

Darren Davison
Jan 2004
