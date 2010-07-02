package org.springframework.web.servlet.mvc;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.annotation.WebParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerMethod;

/**
 * @author Arjen Poutsma
 */
public class WebParamHandlerMethodAdapterTests extends TestCase {

    private WebParamHandlerMethodAdapter adapter;

    private boolean supportedMavInvoked;

    private boolean supportedVoidInvoked;

    private boolean supportedTypesInvoked;

    @Override
    protected void setUp() throws Exception {
        adapter = new WebParamHandlerMethodAdapter();
    }

    public void testUnsupportedInvalidParam() throws NoSuchMethodException {
        HandlerMethod endpoint = new HandlerMethod(this, "unsupportedInvalidParamType", new Class[]{Byte.TYPE});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testUnsupportedInvalidReturnType() throws NoSuchMethodException {
        HandlerMethod endpoint = new HandlerMethod(this, "unsupportedInvalidReturnType", new Class[]{String.class});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testUnsupportedInvalidParams() throws NoSuchMethodException {
        HandlerMethod endpoint =
                new HandlerMethod(this, "unsupportedInvalidParams", new Class[]{String.class, String.class});
        assertFalse("Method supported", adapter.supports(endpoint));
    }

    public void testSupportedTypes() throws NoSuchMethodException {
        HandlerMethod endpoint = new HandlerMethod(this, "supportedTypes",
                new Class[]{Boolean.TYPE, Double.TYPE, Float.TYPE, Integer.TYPE, Long.TYPE, String.class});
        assertTrue("Not all types supported", adapter.supports(endpoint));
    }

    public void testSupportsMav() throws NoSuchMethodException {
        HandlerMethod endpoint = new HandlerMethod(this, "supportedMav", new Class[]{String.class});
        assertTrue("Source method not supported", adapter.supports(endpoint));
    }

    public void testSupportsVoid() throws NoSuchMethodException {
        HandlerMethod endpoint = new HandlerMethod(this, "supportedVoid", new Class[]{Integer.TYPE});
        assertTrue("void method not supported", adapter.supports(endpoint));
    }

    public void testInvokeTypes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("boolean", "true");
        request.addParameter("double", "42");
        request.addParameter("float", "42");
        request.addParameter("integer", "42");
        request.addParameter("long", "42");
        request.addParameter("string", "text");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod endpoint = new HandlerMethod(this, "supportedTypes",
                new Class[]{Boolean.TYPE, Double.TYPE, Float.TYPE, Integer.TYPE, Long.TYPE, String.class});
        ModelAndView result = adapter.handle(request, response, endpoint);
        assertNull("ModelAndView returned", result);
        assertTrue("Method not invoked", supportedTypesInvoked);
    }

    public void testInvokeVoid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("id", "42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod endpoint = new HandlerMethod(this, "supportedVoid", new Class[]{Integer.TYPE});
        ModelAndView result = adapter.handle(request, response, endpoint);
        assertNull("No ModelAndView returned", result);
        assertTrue("Method not invoked", supportedVoidInvoked);
    }

    public void testInvokeMav() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("id", "42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod endpoint = new HandlerMethod(this, "supportedMav", new Class[]{String.class});
        ModelAndView result = adapter.handle(request, response, endpoint);
        assertNotNull("No ModelAndView returned", result);
        assertTrue("Method not invoked", supportedMavInvoked);
    }

    public void supportedVoid(@WebParam(value = "id", required = true)int id) {
        assertEquals("Invalid parameter", 42, id);
        supportedVoidInvoked = true;
    }

    public ModelAndView supportedMav(@WebParam("id")String id) {
        assertEquals("Invalid parameter", "42", id);
        supportedMavInvoked = true;
        return new ModelAndView();
    }

    public void supportedTypes(@WebParam("boolean")boolean b,
                               @WebParam("double")double d,
                               @WebParam("float")float f,
                               @WebParam("integer")int i,
                               @WebParam("long")long l,
                               @WebParam("string")String s) {
        supportedTypesInvoked = true;
        assertTrue("Invalid boolean value", b);
        assertEquals("Invalid double value", 42D, d, 0.00001D);
        assertEquals("Invalid float value", 42F, f, 0.00001F);
        assertEquals("Invalid integer value", 42, i);
        assertEquals("Invalid long value", 42L, l);
        assertEquals("Invalid String value", "text", s);
    }

    public void unsupportedInvalidParams(@WebParam("param")String param1, String param2) {

    }

    public String unsupportedInvalidReturnType(@WebParam("param")String param1) {
        return null;
    }

    public void unsupportedInvalidParamType(@WebParam("param")byte param1) {
    }
}
