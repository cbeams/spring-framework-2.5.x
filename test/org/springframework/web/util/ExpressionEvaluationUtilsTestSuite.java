package org.springframework.web.util;

import javax.servlet.jsp.PageContext;

import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockPageContext;
import org.springframework.web.mock.MockServlet;
import org.springframework.web.mock.MockServletConfig;
import org.springframework.web.mock.MockServletContext;

import junit.framework.TestCase;

/**
 * @author alef

 */
public class ExpressionEvaluationUtilsTestSuite extends TestCase {

	/**
	 * Constructor for ExpressionEvaluationUtilsTestSuite.
	 * @param arg0
	 */
	public ExpressionEvaluationUtilsTestSuite(String name) {
		super(name);
	}

	public void testIsExpressionLanguage() {
		// should be true
		String expr = "${bla}";		
		assertTrue(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be true
		expr = "bla${bla}";
		assertTrue(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be false
		expr = "bla{bla";
		assertFalse(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be false
		expr = "bla$b{";
		assertFalse(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// ok, tested enough ;-)
	}

	public void testEvaluate() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";
		
		Object o = 
			ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		
		assertEquals(new String("test"), ExpressionEvaluationUtils.evaluate(
			"test", "test", Float.class, ctx));
	}

	public void testEvaluateString() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", "blie", PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		Object o = 
			ExpressionEvaluationUtils.evaluateString("test", expr, ctx);
		assertEquals(o, "blie");
		
		assertEquals("blie", ExpressionEvaluationUtils.evaluateString("test", "blie", ctx));
	}

	public void testEvaluateInteger() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Integer(1), PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		
		int i = 
			ExpressionEvaluationUtils.evaluateInteger("test", expr, ctx);
		assertEquals(i, 1);
		
		assertEquals(21, ExpressionEvaluationUtils.evaluateInteger("test", "21", ctx));
	}

	public void testEvaluateBoolean() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Boolean(true), PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		
		boolean b = 
			ExpressionEvaluationUtils.evaluateBoolean("test", expr, ctx);
		assertEquals(b, true);
		
		assertEquals(true, ExpressionEvaluationUtils.evaluateBoolean("test", "true", ctx));
	}
	
	private MockPageContext getMockPageContext() 
	throws Exception {
		MockServletContext servletContext = new MockServletContext();				
		MockServletConfig servletConfig = new MockServletConfig(servletContext, "servlet");		
		MockServlet servlet = new MockServlet();
		servlet.init(servletConfig);
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext, 
			"POST", "http://www.springframework.org");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext();
		pageContext.initialize(servlet, request, response, "nope", false, 4096, true);
		return pageContext;		
	}
	

}
