/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
	}

	public void testEvaluateString() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", "blie", PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		
		Object o = 
			ExpressionEvaluationUtils.evaluateString("test", expr, ctx);
		assertEquals(o, "blie");
	}

	public void testEvaluateInteger() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Integer(1), PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		
		int i = 
			ExpressionEvaluationUtils.evaluateInteger("test", expr, ctx);
		assertEquals(i, 1);
	}

	public void testEvaluateBoolean() 
	throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Boolean(true), PageContext.REQUEST_SCOPE);
		String expr = "${bla}";
		
		boolean b = 
			ExpressionEvaluationUtils.evaluateBoolean("test", expr, ctx);
		assertEquals(b, true);
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
