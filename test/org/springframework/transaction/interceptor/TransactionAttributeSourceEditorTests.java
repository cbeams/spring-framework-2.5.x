/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.transaction.TransactionDefinition;

/**
 * Format is 
 * FQN.Method=tx attribute representation
 * @author Rod Johnson
 * @since 26-Apr-2003
 * @version $Revision: 1.6 $
 */
public class TransactionAttributeSourceEditorTests extends TestCase {

	public TransactionAttributeSourceEditorTests(String arg0) {
		super(arg0);
	}

	public void testNull() throws Exception {
		TransactionAttributeSourceEditor pe = new TransactionAttributeSourceEditor();
		pe.setAsText(null);
		TransactionAttributeSource tas = (TransactionAttributeSource) pe.getValue();

		Method m = Object.class.getMethod("hashCode", null);
		assertTrue(tas.getTransactionAttribute(m, null) == null);
	}

	public void testInvalid() throws Exception {
		TransactionAttributeSourceEditor pe = new TransactionAttributeSourceEditor();
		try {
			pe.setAsText("foo=bar");
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testMatchesSpecific() throws Exception {
		TransactionAttributeSourceEditor pe = new TransactionAttributeSourceEditor();
		// TODO need FQN?
		pe.setAsText("java.lang.Object.hashCode=PROPAGATION_REQUIRED\n" +
		             "java.lang.Object.equals=PROPAGATION_MANDATORY\n" +
		             "java.lang.Object.*it=PROPAGATION_SUPPORTS\n" +
		             "java.lang.Object.notify=PROPAGATION_SUPPORTS\n" +
		             "java.lang.Object.not*=PROPAGATION_REQUIRED");
		TransactionAttributeSource tas = (TransactionAttributeSource) pe.getValue();

		checkTransactionProperties(tas, Object.class.getMethod("hashCode", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("equals", new Class[] { Object.class }),
		                     TransactionDefinition.PROPAGATION_MANDATORY);
		checkTransactionProperties(tas, Object.class.getMethod("wait", null),
		                     TransactionDefinition.PROPAGATION_SUPPORTS);
		checkTransactionProperties(tas, Object.class.getMethod("wait", new Class[] { long.class }),
		                     TransactionDefinition.PROPAGATION_SUPPORTS);
		checkTransactionProperties(tas, Object.class.getMethod("wait", new Class[] { long.class, int.class }),
		                     TransactionDefinition.PROPAGATION_SUPPORTS);
		checkTransactionProperties(tas, Object.class.getMethod("notify", null),
		                     TransactionDefinition.PROPAGATION_SUPPORTS);
		checkTransactionProperties(tas, Object.class.getMethod("notifyAll", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("toString", null),
		                     -1);
	}

	public void testMatchesAll() throws Exception {
		TransactionAttributeSourceEditor pe = new TransactionAttributeSourceEditor();
		pe.setAsText("java.lang.Object.*=PROPAGATION_REQUIRED");
		TransactionAttributeSource tas = (TransactionAttributeSource) pe.getValue();

		checkTransactionProperties(tas, Object.class.getMethod("hashCode", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("equals", new Class[] { Object.class }),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("wait", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("wait", new Class[] { long.class }),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("wait", new Class[] { long.class, int.class }),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("notify", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("notifyAll", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
		checkTransactionProperties(tas, Object.class.getMethod("toString", null),
		                     TransactionDefinition.PROPAGATION_REQUIRED);
	}

	private void checkTransactionProperties(TransactionAttributeSource tas, Method method, int propagationBehavior) {
		TransactionAttribute ta = tas.getTransactionAttribute(method, null);
		if (propagationBehavior >= 0) {
			assertTrue(ta != null);
			assertTrue(ta.getIsolationLevel() == TransactionDefinition.ISOLATION_DEFAULT);
			assertTrue(ta.getPropagationBehavior() == propagationBehavior);
		}
		else {
			assertTrue(ta == null);
		}
	}

}
