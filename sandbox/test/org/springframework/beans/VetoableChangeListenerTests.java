package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import junit.framework.TestCase;

/**
 * Must not begin with Abstract or wildcard will exclude.
 * @author Rod Johnson
 */
public class VetoableChangeListenerTests extends TestCase {
	
	private static final int MAX_AGE = 65;

	public void testDirectValidation() throws Exception {
		MyListener l = new MyListener();
		TestBean tb = new TestBean();
		PropertyChangeEvent e = new PropertyChangeEvent(tb, "age", new Integer(tb.getAge()), new Integer(MAX_AGE - 1));
		// Ok
		l.vetoableChange(e);
		e = new PropertyChangeEvent(tb, "age", new Integer(tb.getAge()), new Integer(MAX_AGE + 1));
		try {
			l.vetoableChange(e);
			fail();
		}
		catch (PropertyVetoException ex) {
			// Ok
		}
	}


	private static class MyListener extends AbstractVetoableChangeListener {
		public void validateAge(int age, PropertyChangeEvent e) throws PropertyVetoException {
			assertTrue(e != null);
			assertTrue(e.getPropertyName().equals("age"));
			if (age > MAX_AGE)
				throw new PropertyVetoException("too old", e);
		}

		public void validateName(String name, PropertyChangeEvent e) throws PropertyVetoException {
			assertTrue(e.getPropertyName().equals("name"));
			if (name == null)
				throw new PropertyVetoException("must provide name", e);
		}

		// Wrong sig, not invoked
		public void validateName(String name) {
			throw new IllegalStateException();
		}
	}

}
