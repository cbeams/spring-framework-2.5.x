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



