package org.springframework.beans.factory.config;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

/**
 * @author robh
 */
public class UtilNamespaceHandlerTests extends TestCase {

    public void testLoadProperties() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(new ClassPathResource("testUtilNamespace.xml", getClass()));
        Properties props = (Properties) bf.getBean("myProperties");
        assertEquals("Incorrect property value", "bar", props.get("foo"));
    }
}
