package org.springframework.jndi;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 */
public class JndiNamespaceHandlerTests extends TestCase {

	private XmlBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("jndiNamespaceHandlerTests.xml", getClass()));
	}

	public void testSimpleDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("simple");
		assertEquals(JndiObjectFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "jndiName", "jdbc/MyDataSource");
		assertNull("Property resourceRef should not have been set", beanDefinition.getPropertyValues().getPropertyValue("resourceRef"));
	}

	public void testComplexDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("complex");
		assertEquals(JndiObjectFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "jndiName", "jdbc/MyDataSource");
		assertPropertyValue(beanDefinition, "resourceRef", "true");
		assertPropertyValue(beanDefinition, "cache", "true");
		assertPropertyValue(beanDefinition, "lookupOnStartup", "true");
		assertPropertyValue(beanDefinition, "expectedType", "com.myapp.DefaultFoo");
		assertPropertyValue(beanDefinition, "proxyInterface", "com.myapp.Foo");
	}

	public void testWithEnvironment() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("withEnvironment");
		assertPropertyValue(beanDefinition, "jndiEnvironment", "foo=bar");
	}

	private void assertPropertyValue(RootBeanDefinition beanDefinition, String propertyName, Object expectedValue) {
		assertEquals("Property [" + propertyName + "] incorrect.", expectedValue, beanDefinition.getPropertyValues().getPropertyValue(propertyName).getValue());
	}
}
