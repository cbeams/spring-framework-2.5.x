/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.orm.jdo;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @version $Id: JdoTemplateTests.java,v 1.2 2003-09-19 10:56:23 johnsonr Exp $
 */
public class JdoTemplateTests extends TestCase {

	public void testTemplateExecuteWithNotAllowCreate() {
		JdoTemplate jt = new JdoTemplate();
		jt.setAllowCreate(false);
		try {
			jt.execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					return null;
				}
			});
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testTemplateExecuteWithNotAllowCreateAndThreadBound() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.setAllowCreate(false);
		PersistenceManagerFactoryUtils.getThreadObjectManager().bindThreadObject(pmf, new PersistenceManagerHolder(pm));
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		PersistenceManagerFactoryUtils.getThreadObjectManager().removeThreadObject(pmf);
		pmfControl.verify();
		pmControl.verify();
	}

	public void testTemplateExecuteWithNewPersistenceManager() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		pmfControl.verify();
		pmControl.verify();
	}

	public void testTemplateExceptions() {
		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOUserException();
				}
			});
		}
		catch (JdoUsageException ex) {
		}
		catch (Exception ex) {
			fail("Should have thrown JdoUsageException");
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOFatalUserException();
				}
			});
		}
		catch (JdoUsageException ex) {
		}
		catch (Exception ex) {
			fail("Should have thrown JdoUsageException");
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOException();
				}
			});
		}
		catch (JdoSystemException ex) {
		}
		catch (Exception ex) {
			fail("Should have thrown JdoSystemException");
		}
	}

	private JdoTemplate createTemplate() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 1);
		tx.isActive();
		txControl.setReturnValue(false, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();
		return new JdoTemplate(pmf);
	}

}
