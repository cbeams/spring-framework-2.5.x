package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 * @since 01.11.2003
 */
public class PropertiesFactoryBeanTests extends TestCase {

	public void testWithPropertiesFile() throws IOException {
		PropertiesFactoryBean pfb = new PropertiesFactoryBean();
		pfb.setLocation(new ClassPathResource("/org/springframework/beans/factory/config/test.properties"));
		pfb.afterPropertiesSet();
		Properties props = (Properties) pfb.getObject();
		assertEquals("value1", props.getProperty("key1"));
	}

	public void testWithLocalProperties() throws IOException {
		PropertiesFactoryBean pfb = new PropertiesFactoryBean();
		Properties localProps = new Properties();
		localProps.setProperty("key2", "value2");
		pfb.setProperties(localProps);
		pfb.afterPropertiesSet();
		Properties props = (Properties) pfb.getObject();
		assertEquals("value2", props.getProperty("key2"));
	}

	public void testWithPropertiesFileAndLocalProperties() throws IOException {
		PropertiesFactoryBean pfb = new PropertiesFactoryBean();
		pfb.setLocation(new ClassPathResource("/org/springframework/beans/factory/config/test.properties"));
		Properties localProps = new Properties();
		localProps.setProperty("key2", "value2");
		pfb.setProperties(localProps);
		pfb.afterPropertiesSet();
		Properties props = (Properties) pfb.getObject();
		assertEquals("value1", props.getProperty("key1"));
		assertEquals("value2", props.getProperty("key2"));
	}

	public void testWithPrototype() throws IOException {
		PropertiesFactoryBean pfb = new PropertiesFactoryBean();
		pfb.setSingleton(false);
		pfb.setLocation(new ClassPathResource("/org/springframework/beans/factory/config/test.properties"));
		Properties localProps = new Properties();
		localProps.setProperty("key2", "value2");
		pfb.setProperties(localProps);
		pfb.afterPropertiesSet();
		Properties props = (Properties) pfb.getObject();
		assertEquals("value1", props.getProperty("key1"));
		assertEquals("value2", props.getProperty("key2"));
		Properties newProps = (Properties) pfb.getObject();
		assertTrue(props != newProps);
		assertEquals("value1", newProps.getProperty("key1"));
		assertEquals("value2", newProps.getProperty("key2"));
	}

}
