/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import org.springframework.beans.TestBean;

/**
 * @author robh
 *
 */
public class BeanNameCommonsLogProviderTests extends
		AbstractCommonsLogProviderTests {

	protected CommonsLogProvider getLogProvider() {
		return new BeanNameCommonsLogProvider();
	}

	protected Object getBean() {
		return new TestBean();
	}

	protected String getBeanName() {
		return "testBean";
	}

	protected String getLogName() {
		return "testBean";
	}
	
	public void testWithClassName() {
		BeanNameCommonsLogProvider provider = new BeanNameCommonsLogProvider();
		provider.setIncludeClassName(true);
		
		TestLog log = (TestLog)provider.getLogForBean(getBean(), getBeanName());
		
		assertNotNull("Log should not be null.", log);
		assertEquals("Log name is incorrect.", "org.springframework.beans.TestBean [testBean]", log.getName());
	}

}
