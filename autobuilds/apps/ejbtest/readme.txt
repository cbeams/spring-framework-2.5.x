Work-in-progress ejb integration test for Spring
------------------------------------------------

@author Colin Sampaleanu

------------------------------------------------

This is currently not fully integrated into the autobuilds infrastructure,
although it is intended that it will be relatively soon.

It is mean to be used for testing Spring ejb functionality which can not be
covered by unit tests (since EJBs need to run in a real container), and
thus needs an integration-level test to fully stress.

When deploying to JBoss, and using HSQLDB, you need to manually start an
instance of the HSQL database, with the db/hsqldb/runServerForTest script.
Before you actually run JBoss, you need to create a datasource pointing to
this DB instance, which is on port 19001. A sample datasource config you 
can just drop into JBoss's deploy dir is db/hsqldb/ejbtest-hsqldb-ds.xml.

This app has been tested with JBoss 4.0.2 and 4.0.3SP1. It used to also work
with JBoss 3.2.x but has not been tested with that version in some time.

JBoss appears to have some classloading issues preventing a hot-redeploy of
the app, with the following exception showing up:
   """org.hibernate.cache.CacheException: net.sf.ehcache.CacheException:
       Cannot configure CacheManager: null"""
While this seems to be a JBoss/Hibernate/Ehcache issue, it's been verified
that adding ehcache-1.1.jar in the server/lib dir seems to allow hot-redeploy
to work properly.



