package org.springframework.beans.factory.xml;

import junit.framework.TestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXParseException;

/**
 * @author Rob Harrop
 */
public class SchemaValidationTests extends TestCase {

    public void testWithAutodetection() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        try {
            reader.loadBeanDefinitions(getResource("invalidPerSchema.xml"));
            fail("Should not be able to parse a file with errors");
        }
        catch (BeansException e) {
            assertEquals("Parse error not detected", SAXParseException.class, e.getCause().getClass());
        }
    }

    public void testWithExplicitValidationMode() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        try {
            reader.loadBeanDefinitions(getResource("invalidPerSchema.xml"));
            fail("Should not be able to parse a file with errors");
        }
        catch (BeansException e) {
            assertEquals("Parse error not detected", SAXParseException.class, e.getCause().getClass());
        }
    }

    public void testLoadDefinitions() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        reader.loadBeanDefinitions(getResource("schemaValidated.xml"));

        TestBean foo = (TestBean)bf.getBean("fooBean");
        assertNotNull("Spouse is null", foo.getSpouse());
        assertEquals("Incorrect number of friends", 2, foo.getFriends().size());
    }

    protected Resource getResource(String file) {
        return new ClassPathResource(file, getClass());
    }
}
