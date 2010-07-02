package org.springframework.web.servlet.handler;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * @author Arjen Poutsma
 */
public class HandlerMethodTests extends TestCase {

    private HandlerMethod handlerMethod;

    private boolean myMethodInvoked;

    private Method method;

    protected void setUp() throws Exception {
        myMethodInvoked = false;
        method = getClass().getMethod("myMethod", new Class[]{String.class});
        handlerMethod = new HandlerMethod(this, method);
    }

    public void testGetters() throws Exception {
        assertEquals("Invalid bean", this, handlerMethod.getBean());
        assertEquals("Invalid bean", method, handlerMethod.getMethod());
    }

    public void testInvoke() throws Exception {
        assertFalse("Method invoked before invocation", myMethodInvoked);
        handlerMethod.invoke(new Object[]{"arg"});
        assertTrue("Method invoked before invocation", myMethodInvoked);
    }

    public void testEquals() throws Exception {
        assertEquals("Not equal", handlerMethod, handlerMethod);
        assertEquals("Not equal", new HandlerMethod(this, method), handlerMethod);
        Method otherMethod = getClass().getMethod("testEquals", new Class[0]);
        assertFalse("Equal", new HandlerMethod(this, otherMethod).equals(handlerMethod));
    }

    public void testHashCode() throws Exception {
        assertEquals("Not equal", new HandlerMethod(this, method).hashCode(), handlerMethod.hashCode());
        Method otherMethod = getClass().getMethod("testEquals", new Class[0]);
        assertFalse("Equal", new HandlerMethod(this, otherMethod).hashCode() == handlerMethod.hashCode());
    }

    public void myMethod(String arg) {
        assertEquals("Invalid argument", "arg", arg);
        myMethodInvoked = true;
    }

    public void testToString() throws Exception {
        assertNotNull("Na valid toString", handlerMethod.toString());
    }
}
