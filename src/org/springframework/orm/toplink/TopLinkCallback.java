/*
@license@
  */ 

package org.springframework.orm.toplink;

import java.sql.SQLException;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;

/**
 * This interface is used to implement any TopLink functionality that will be executed
 * inside of a TopLinkTemplate.  Often, users will implement this interface as an
 * anonymous inner class.
 * 
 * The Session that is passed into the <code>doInTopLink</code> method is a Thread safe 
 * ClientSession.  Since this provides access to the TopLink shared cache, it is possible for implementations
 * of this interface to return references to read-only Objects from the shared cache.  These Objects must not be
 * modified by application code outside of the DAO layer.  
 * If Objects need to be edited, then they should be registered with a TopLink
 * UnitOfWork or they should be explicitly copied and merged back into a UnitOfWork at a later time.  
 * 
 * Users can access a UnitOfWork by using the  
 * <code>getActiveUnitOfWork</code> api on the <code>Session</code>.  Normally, this will only be done
 * when there is an active Transaction being managed by one of the Spring PlatformTransactionManagers, 
 * like <code>TopLinkTransactionManager</code>.
 *  
 * @author <a href="mailto:@james.x.clark@oracle.com">James Clark</a>
 * @see TopLinkTemplate
 * @see TopLinkTransactionManager
 */
public interface TopLinkCallback
{
	/**
	 *
	 * @return a result object, or null if none
	 * @throws ToplinkException in case of Toplink errors
	 * @throws SQLException in case of errors on direct JDBC access
	 * @see TopLinkTemplate#execute
	 * @see TopLinkTransactionManager
	 */
	Object doInToplink(Session session) throws TopLinkException, SQLException;
}
