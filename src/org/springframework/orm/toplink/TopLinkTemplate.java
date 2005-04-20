/*
@license@
  */ 

package org.springframework.orm.toplink;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.ObjectCopyingPolicy;
import oracle.toplink.sessions.Session;

import org.springframework.dao.DataAccessException;

/**
 * Helper class that simplifies Toplink data access code, and converts
 * ToplinkExceptions into unchecked DataAccessExceptions,
 * compatible to the org.springframework.dao exception hierarchy.
 * Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>Typically used to implement data access or business logic services that
 * use Toplink within their implementation but are Toplink-agnostic in
 * their interface. The latter resp. code calling the latter only have to deal
 * with business objects, query objects, and org.springframework.dao exceptions.
 *
 * <p>The central method is "execute", supporting Toplink code implementing
 * the ToplinkCallback interface. It provides Toplink Session handling
 * such that neither the ToplinkCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing Toplink Sessions,
 * or handling Session lifecycle exceptions. For typical single step actions,
 * there are various convenience methods (load, merge, delete, save, etc).
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a Session reference, or get prepared in an application context
 * and given to services as bean reference. Note: The Session should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * ToplinkInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Respective checks and the actual throwing of
 * such exceptions can often be deferred to after callback execution, though.
 *
 * <p>Note that even if ToplinkTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be Toplink-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies. For example, switching to JTA is just a matter of
 * Spring configuration (use JtaTransactionManager instead), without needing
 * to touch application code.
 *
 * <p>LocalSessionManagerFactoryBean is the preferred way of obtaining a reference
 * to a specific Toplink Session, at least in a non-EJB environment.
 *
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 15.04.2004
 */
public class TopLinkTemplate extends TopLinkAccessor
{
	private boolean allowCreate = true;

	/**
	 * Create a new ToplinkTemplate instance.
	 */
	public TopLinkTemplate()
	{
	}

	/**
	 * Create a new ToplinkTemplate instance.
	 * @param session Session to create Sessions
	 */      
	public TopLinkTemplate(SessionFactory sessionFactory)
	{
		setSessionFactory(sessionFactory);
		afterPropertiesSet();
	}

	/**
	 * Create a new ToplinkTemplate instance.
	 * @param session Session to create Sessions
	 * @param allowCreate if a new Session should be created
	 * if no thread-bound found
	 */
	public TopLinkTemplate(SessionFactory sessionFactory, boolean allowCreate)
	{
		setSessionFactory(sessionFactory);
		setAllowCreate(allowCreate);
		afterPropertiesSet();
	}

	/**
	 * Set if a new Session should be created if no thread-bound found.
	 * <p>ToplinkTemplate is aware of a respective Session bound to the
	 * current thread, for example when using ToplinkTransactionManager.
	 * If allowCreate is true, a new Session will be created if none found.
	 * If false, an IllegalStateException will get thrown in this case.
	 * @see SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	public void setAllowCreate(boolean allowCreate)
	{
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new Session should be created if no thread-bound found.
	 */
	public boolean isAllowCreate()
	{
		return allowCreate;
	}

	/**
	 * The main template method executing a given callback while providing
	 * exception translation and session handling
	 * @param action The action callback to execute
	 * @return Whatever the action returns
	 * @throws DataAccessException Unchecked exception in case of errors
	 */
	public Object execute(TopLinkCallback action) throws DataAccessException
	{
		Session session = SessionFactoryUtils.getSession(getSessionFactory(), this.allowCreate);

		try
		{
			Object result = action.doInToplink(session);
			return result;
		}
		catch (TopLinkException ex)
		{
			throw convertToplinkAccessException(ex);
		}
		catch (SQLException ex)
		{
			throw convertJdbcAccessException(ex);
		}
		catch (RuntimeException ex)
		{
			// callback code threw application exception
			throw ex;
		}
		finally
		{
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
		}
	}
	
	/**
	 * Helper method to execute finds that return a list
	 * @param action The action to execute
	 * @return The list of objects returned
	 * @throws DataAccessException
	 */
	public List executeFind(TopLinkCallback action) throws DataAccessException
	{
		return (List) execute(action);
	}

	/**
	 * Re-associate the given entity with the current UOW using simple merging 
	 * @param entity The clone to merge
	 * @throws DataAccessException
	 */
	public Object mergeClone(final Object entity) throws DataAccessException
	{
		return execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				session.readObject(entity);
				return session.getActiveUnitOfWork().mergeClone(entity);
			}
		});
	}

	/**
	 * Re-associate the given entity with the current UOW using deep merge of all
	 * contained entities 
	 * @param entity The clone to merge
	 * @throws DataAccessException
	 */
	public Object deepMergeClone(final Object entity) throws DataAccessException
	{
		return execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				session.readObject(entity);
				return session.getActiveUnitOfWork().deepMergeClone(entity);
			}
		});
	}

	/**
	 * Re-associate the given entity with the current UOW using shallow merging 
	 * @param entity The clone to merge
	 * @throws DataAccessException
	 */
	public Object shallowMergeClone(final Object entity) throws DataAccessException
	{
		return execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				session.readObject(entity);
				return session.getActiveUnitOfWork().shallowMergeClone(entity);
			}
		});
	}

	/**
	 * Re-associate the given entity with the current UOW using merging with all
	 * references from this clone 
	 * @param entity The clone to merge
	 * @throws DataAccessException
	 */
	public Object mergeCloneWithReferences(final Object entity) throws DataAccessException
	{
		return execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				session.readObject(entity);
				return session.getActiveUnitOfWork().mergeCloneWithReferences(entity);
			}
		});
	}

	/**
	 * Delete the given entity
	 * @param entity The entity to delete
	 * @throws DataAccessException
	 */
	public void delete(final Object entity) throws DataAccessException
	{
		execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				return session.getActiveUnitOfWork().deleteObject(entity);
			}
		});
	}

	/**
	 * Delete all the entities
	 * @param entities The entities to delete
	 * @throws DataAccessException
	 */
	public void deleteAll(final Collection entities) throws DataAccessException
	{
		execute(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
				session.getActiveUnitOfWork().deleteAllObjects(entities);
				return null;
			}
		});
	}

	/**
	 * Execute a given named query with the given parameters
	 * @param entityClass The class that has the named query descriptor
	 * @param queryName The name of the query
	 * @param params Parameters for the query
	 * @param readOnly Should we return clones or read directly through the session
	 * @return A list of objects matching the query
	 * @throws DataAccessException
	 */
	public List findByNamedQuery(final Class entityClass,
		final String queryName, final Vector params, final boolean readOnly)
	throws DataAccessException
	{
		return executeFind(new TopLinkCallback()
		{
			public Object doInToplink(Session session) throws TopLinkException, SQLException
			{
			    Session s = (readOnly?session:session.getActiveUnitOfWork());
				return s.executeQuery(queryName,entityClass,params);
			}
		});
	}
	
	/**
	 * execute a named query against the currently active UnitOfWork
	 * 
	 * @param entityClass
	 * @param queryName
	 * @param params
	 * @return
	 * @throws DataAccessException
	 */
	public List findByNamedQuery(final Class entityClass,
			final String queryName, final Vector params)
		throws DataAccessException
	{
	    return findByNamedQuery(entityClass,queryName,params,false);
	}

	public Object refresh(final Object object, final boolean readOnly) throws DataAccessException
    {
        return execute(new TopLinkCallback()
        {
            public Object doInToplink(Session session) throws TopLinkException,
                    SQLException
            {
                Session s = (readOnly?session:session.getActiveUnitOfWork());
                return s.refreshObject(object);
            }
        });
    }
	
	public Object load(final Class entityClass, final Vector keys, final boolean readOnly) throws DataAccessException
    {
        return execute(new TopLinkCallback() {
            public Object doInToplink(Session session) throws TopLinkException,
                    SQLException
            {
                Session s = (readOnly?session:session.getActiveUnitOfWork());
                ReadObjectQuery readObjectQuery = new ReadObjectQuery(entityClass);
                readObjectQuery.setSelectionKey(keys);
                return s.executeQuery(readObjectQuery);
            }
        });
    }
	 
	public Object load(final Class entityClass, final Object id, final boolean readOnly)
            throws DataAccessException
    {
        Vector v = new Vector();
        v.add(id);
        return load(entityClass, v, readOnly);
    }
	
	public Object loadAndCopy(final Class entityClass, final Vector keys) throws DataAccessException
    {
        return execute(new TopLinkCallback() {
            public Object doInToplink(Session session) throws TopLinkException,
                    SQLException
            {
                ReadObjectQuery readObjectQuery = new ReadObjectQuery(
                        entityClass);
                readObjectQuery.setSelectionKey(keys);
                Object object = session.executeQuery(readObjectQuery);

                ObjectCopyingPolicy copyPolicy = new ObjectCopyingPolicy();
        	    copyPolicy.cascadeAllParts();
        	    copyPolicy.setShouldResetPrimaryKey(false);

                return session.copyObject(object,copyPolicy);
            }
        });
    }

	public Object loadAndCopy(final Class entityClass, final Object id)
            throws DataAccessException
    {
        Vector v = new Vector();
        v.add(id);
        return loadAndCopy(entityClass, v);
    }

    public Object registerObject(final Object object)
    {
        return execute( new TopLinkCallback(){
            public Object doInToplink(Session session) throws TopLinkException,
                    SQLException
            {
                return session.getActiveUnitOfWork().registerObject(object);
            }
        });
    }
	
}
