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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @version $Id: JdoTemplateTests.java,v 1.5 2003-11-05 20:33:07 jhoeller Exp $
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
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(pmf);
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

	public void testTemplateExecuteWithThreadBoundAndFlushEager() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 1);
		dialect.flush(tx);
		dialectControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();
		dialectControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.setJdoDialect(dialect);
		jt.setFlushEager(true);
		jt.setAllowCreate(false);
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(pmf);
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
		dialectControl.verify();
	}

	public void testTemplateExceptions() {
		try {
			JdoTemplate template = createTemplate();
			template.setFlushEager(true);
			template.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOUserException();
				}
			});
			fail("Should have thrown JdoUsageException");
		}
		catch (JdoUsageException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOFatalUserException();
				}
			});
			fail("Should have thrown JdoUsageException");
		}
		catch (JdoUsageException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOException();
				}
			});
			fail("Should have thrown JdoSystemException");
		}
		catch (JdoSystemException ex) {
			// expected
		}

		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		final JDOException ex = new JDOException();
		dialect.translateException(ex);
		dialectControl.setReturnValue(new DataIntegrityViolationException("test", ex));
		dialectControl.replay();
		try {
			JdoTemplate template = createTemplate();
			template.setJdoDialect(dialect);
			template.execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw ex;
				}
			});
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException dive) {
			// expected
		}
		dialectControl.verify();
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
