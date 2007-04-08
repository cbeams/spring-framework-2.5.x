package org.springframework.web.servlet.handler.metadata;

import junit.framework.TestCase;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.annotation.Controller;
import org.springframework.web.annotation.Url;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.HandlerMethod;

/**
 * @author Arjen Poutsma
 */
public class UrlAnnotationHandlerMappingTests extends TestCase {

    private UrlAnnotationHandlerMapping mapping;

    private StaticApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", UrlAnnotationHandlerMapping.class);
        applicationContext.registerSingleton("endpoint", MyController.class);
        applicationContext.registerSingleton("other", OtherBean.class);
        applicationContext.refresh();
        mapping = (UrlAnnotationHandlerMapping) applicationContext.getBean("mapping");
    }

    public void testRegistration() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
        HandlerExecutionChain chain = mapping.getHandler(request);
        assertNotNull("method not registered", chain);

        HandlerMethod expected = new HandlerMethod(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, chain.getHandler());

        request = new MockHttpServletRequest("GET", "/home.html");
        chain = mapping.getHandler(request);
        assertNotNull("method not registered", chain);
        assertEquals("Invalid endpoint registered", expected, chain.getHandler());

        request = new MockHttpServletRequest("GET", "/other.html");
        chain = mapping.getHandler(request);
        assertNull("Invalid method registered", chain);
    }

    @Controller
    private static class MyController {

        @Url({"/index.html", "/home.html"})
        public void doIt() {

        }

    }

    private static class OtherBean {

        @Url("/other.html")
        public void doIt() {

        }

    }

}
