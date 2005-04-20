/*
@license@
  */ 

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.orm.toplink.sessions.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new Toplink Session to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already was a pre-bound Session (e.g. from ToplinkTransactionManager,
 * or from a surrounding Toplink-intercepted method), the interceptor simply
 * takes part in it.
 *
 * <p>Application code must retrieve a Toplink Session via SessionFactoryUtils'
 * getSession method, to be able to detect a thread-bound Session. It is preferable
 * to use getSession with allowCreate=false, as the code relies on the interceptor
 * to provide proper Session handling. Typically the code will look as follows:
 *
 * <p><code>
 * public void doToplinkAction() {<br>
 * &nbsp;&nbsp;Session session = ToplinkUtils.getSession(this.session, false);<br>
 * &nbsp;&nbsp;try {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;...<br>
 * &nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;catch (ToplinkException ex) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;throw ToplinkUtils.convertToplinkAccessException(ex);<br>
 * &nbsp;&nbsp;}<br>
 * }
 * </code>
 *
 * <p>Note that the application must care about handling ToplinkExceptions itself,
 * preferably via delegating to ToplinkUtils' convertToplinkAccessException
 * that converts them to ones that are compatible with the org.springframework.dao
 * exception hierarchy (like ToplinkTemplate does).
 *
 * <p>This class can be considered a declarative alternative to ToplinkTemplate's
 * callback approach. The advantages are:
 * <ul>
 * <li>no anonymous classes necessary for callback implementations;
 * <li>the possibility to throw any application exceptions from within data access code.
 * </ul>
 *
 * <p>The drawbacks are:
 * <ul>
 * <li>the dependency on interceptor configuration;
 * <li>the delegating try/catch blocks.
 * </ul>
 *
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @since 15.04.2004
 */
public class TopLinkInterceptor extends TopLinkAccessor implements MethodInterceptor
{
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		boolean existingTransaction = false;
		Session s = SessionFactoryUtils.getSession(getSessionFactory(),true,true);
		
		if (TransactionSynchronizationManager.hasResource(getSessionFactory()))
		{
			logger.debug("Found thread-bound session for Toplink interceptor");
			existingTransaction = true;
		}
		else
		{
			logger.debug("Using new session for Toplink interceptor");
			TransactionSynchronizationManager.bindResource(
				getSessionFactory(), new SessionHolder(s));
		}
		try
		{
			Object retVal = methodInvocation.proceed();
			return retVal;
		}
		catch (TopLinkException ex)
		{
			throw convertToplinkAccessException(ex);
		}
		finally
		{
			if (existingTransaction)
			{
				logger.debug("Not closing pre-bound Toplink session after interceptor");
			}
			else
			{
				TransactionSynchronizationManager.unbindResource(getSessionFactory());
				SessionFactoryUtils.closeSessionIfNecessary(s, getSessionFactory());
			}
		}
	}

}
