/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import java.util.Properties;

import org.springframework.beans.TestBean;

/**
 * @author robh
 * 
 */
public class PropertiesCommonsLogProviderTests extends
		AbstractCommonsLogProviderTests {

	protected CommonsLogProvider getLogProvider() {
		PropertiesCommonsLogProvider provider = new PropertiesCommonsLogProvider();
		Properties properties = new Properties();
		properties.setProperty("testBean", "foo");
		provider.setProperties(properties);
		return provider;
	}

	protected Object getBean() {
		return new TestBean();
	}

	protected String getBeanName() {
		return "testBean";
	}

	protected String getLogName() {
		return "foo";
	}

	public void testFallbackProvider() {
		PropertiesCommonsLogProvider provider = new PropertiesCommonsLogProvider();
		provider.setFallbackProvider(new ClassNameCommonsLogProvider());
		TestLog log = (TestLog)provider.getLogForBean(getBean(), getBeanName());
		
		assertNotNull("Log should not be null", log);
		assertEquals("Log name is incorrect.", "org.springframework.beans.TestBean", log.getName());
	}
}
