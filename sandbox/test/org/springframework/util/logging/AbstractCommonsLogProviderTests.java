/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import junit.framework.TestCase;

import org.apache.commons.logging.LogFactory;

/**
 * @author robh
 *
 */
public abstract class AbstractCommonsLogProviderTests extends TestCase {

	private static final String LOG_PROPERTY = "org.apache.commons.logging.Log";

	public void setUp() {
		LogFactory.getFactory().setAttribute(LOG_PROPERTY, TestLog.class.getName());
	}
	
	public void tearDown() {
		LogFactory.getFactory().removeAttribute(LOG_PROPERTY);
	}
	
	public void testLogName() {
		CommonsLogProvider provider = getLogProvider();
		
		TestLog log = (TestLog)provider.getLogForBean(getBean(), getBeanName());
		
		assertNotNull("Log should not be null", log);
		assertEquals("Log name is incorrect", getLogName(), log.getName());
	}
	
	protected abstract CommonsLogProvider getLogProvider();
	protected abstract Object getBean();
	protected abstract String getBeanName();
	protected abstract String getLogName();
	
}
