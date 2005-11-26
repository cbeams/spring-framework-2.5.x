package org.springframework.beans.factory.xml.support;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.UtilNamespaceHandler;

/**
 * @author Rob Harrop
 */
public class DefaultNamespaceHandlerResolverTests extends TestCase {

    public void testResolvedMappedHandler() {
        DefaultNamespaceHandlerResolver resolver = new DefaultNamespaceHandlerResolver();
        NamespaceHandler handler = resolver.resolve("http://www.springframework.org/schema/util");
        assertNotNull("Handler should not be null.", handler);
        assertEquals("Incorrect handler loaded", UtilNamespaceHandler.class, handler.getClass());
    }

    public void testNonExistentHandlerClass() throws Exception {
        String mappingPath = "org/springframework/beans/factory/xml/support/nonExistent.properties";
        try {
            new DefaultNamespaceHandlerResolver(mappingPath);
            fail("Should not be able to map a URI to a non-existent class");
        }
        catch (Throwable e) {
            assertEquals("Incorrect root cause.", ClassNotFoundException.class, e.getCause().getClass());
        }
    }

    public void testResolveInvalidHandler() throws Exception {
        String mappingPath = "org/springframework/beans/factory/xml/support/invalid.properties";
        try {
            new DefaultNamespaceHandlerResolver(mappingPath);
            fail("Should not be able to map a class that doesn't implement NamespaceHandler");
        }
        catch (Throwable e) {
            // success
        }
    }
}
