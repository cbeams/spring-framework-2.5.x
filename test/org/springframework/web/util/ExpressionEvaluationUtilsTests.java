/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import junit.framework.TestCase;

import org.springframework.mock.web.MockExpressionEvaluator;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Aled Arendsen
 * @author Juergen Hoeller
 * @since 16.09.2003
 */
public class ExpressionEvaluationUtilsTests extends TestCase {

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

	public void testEvaluate() throws Exception {
		PageContext ctx = new MockPageContext();
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";
		
		Object o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		
		assertEquals("test", ExpressionEvaluationUtils.evaluate("test", "test", String.class, ctx));

		try {
			ExpressionEvaluationUtils.evaluate("test", "test", Float.class, ctx);
			fail("Should have thrown JspException");
		}
		catch (JspException ex) {
			// expected
		}
	}

	public void testEvaluateString() throws Exception {
		PageContext ctx = new MockPageContext();
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";

		Object o = ExpressionEvaluationUtils.evaluateString("test", expr, ctx);
		assertEquals(o, "blie");
		
		assertEquals("blie", ExpressionEvaluationUtils.evaluateString("test", "blie", ctx));
	}

	public void testEvaluateInteger() throws Exception {
		PageContext ctx = new MockPageContext();
		ctx.setAttribute("bla", new Integer(1));
		String expr = "${bla}";
		
		int i = ExpressionEvaluationUtils.evaluateInteger("test", expr, ctx);
		assertEquals(i, 1);
		
		assertEquals(21, ExpressionEvaluationUtils.evaluateInteger("test", "21", ctx));
	}

	public void testEvaluateBoolean() throws Exception {
		PageContext ctx = new MockPageContext();
		ctx.setAttribute("bla", new Boolean(true));
		String expr = "${bla}";
		
		boolean b = ExpressionEvaluationUtils.evaluateBoolean("test", expr, ctx);
		assertEquals(b, true);
		
		assertEquals(true, ExpressionEvaluationUtils.evaluateBoolean("test", "true", ctx));
	}

	public void testEvaluateWithoutCaching() throws Exception {
		PageContext ctx = new CountingMockPageContext();
		CountingMockExpressionEvaluator eval = (CountingMockExpressionEvaluator) ctx.getExpressionEvaluator();
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";

		Object o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		assertEquals(1, eval.evaluateCount);

		o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		assertEquals(2, eval.evaluateCount);
	}

	public void testEvaluateWithCaching() throws Exception {
		PageContext ctx = new CountingMockPageContext();
		CountingMockExpressionEvaluator eval = (CountingMockExpressionEvaluator) ctx.getExpressionEvaluator();
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";

		MockServletContext sc = (MockServletContext) ctx.getServletContext();
		sc.addInitParameter(ExpressionEvaluationUtils.EXPRESSION_CACHE_CONTEXT_PARAM, "true");

		Object o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		assertEquals(1, eval.parseExpressionCount);

		o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		assertEquals(1, eval.parseExpressionCount);
	}


	private static class CountingMockPageContext extends MockPageContext {

		private ExpressionEvaluator eval = new CountingMockExpressionEvaluator(this);

		public ExpressionEvaluator getExpressionEvaluator() {
			return eval;
		}
	}


	private static class CountingMockExpressionEvaluator extends MockExpressionEvaluator {

		public int parseExpressionCount = 0;

		public int evaluateCount = 0;

		public CountingMockExpressionEvaluator(PageContext pageContext) {
			super(pageContext);
		}

		public Expression parseExpression(String expression, Class expectedType, FunctionMapper functionMapper) throws ELException {
			this.parseExpressionCount++;
			return super.parseExpression(expression, expectedType, functionMapper);
		}

		public Object evaluate(String expression, Class expectedType, VariableResolver variableResolver, FunctionMapper functionMapper) throws ELException {
			this.evaluateCount++;
			return super.evaluate(expression, expectedType, variableResolver, functionMapper);
		}
	}

}
