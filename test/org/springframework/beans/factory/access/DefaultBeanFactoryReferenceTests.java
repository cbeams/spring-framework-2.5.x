package org.springframework.beans.factory.access;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Unit test for DefaultBeanFactoryReference
 * 
 * @author Colin Sampaleanu
 */
public class DefaultBeanFactoryReferenceTests extends TestCase {

	public void testRelease() {
		MockControl control = MockControl.createControl(ConfigurableBeanFactory.class);
		ConfigurableBeanFactory bf = (ConfigurableBeanFactory) control.getMock();

		bf.destroySingletons();
		control.replay();

		DefaultBeanFactoryReference bfr = new DefaultBeanFactoryReference(bf);

		assertNotNull(bfr.getFactory());
		bfr.release();

		try {
			bfr.getFactory();
		}
		catch (IllegalStateException e) {
			// expected
		}

		control.verify();
	}
}
