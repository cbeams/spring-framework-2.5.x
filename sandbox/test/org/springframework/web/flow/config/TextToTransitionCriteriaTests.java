/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.web.flow.config;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionException;
import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.config.TextToTransitionCriteria;

/**
 * Test case for OgnlTransitionCriteriaCreator.
 * 
 * @author Rob Harrop
 */
public class TextToTransitionCriteriaTests extends TestCase {

	public void testTrueEvaluation() throws Exception {
		String expression = "${flowScope.foo == 'bar'}";
		TransitionCriteria criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
		RequestContext ctx = getRequestContext();
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));
	}

	public void testFalseEvaluation() throws Exception {
		String expression = "${flowScope.foo != 'bar'}";
		TransitionCriteria criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
		RequestContext ctx = getRequestContext();
		assertFalse("Criterion should evaluate to false", criterion.test(ctx));
	}

	public void testNonBooleanEvaluation() throws Exception {
		String expression = "${flowScope.foo}";
		TransitionCriteria criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
		RequestContext ctx = getRequestContext();
		try {
			criterion.test(ctx);
			fail("Non-boolean evaluations are not allowed");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	public void testInvalidSyntax() throws Exception {
		try {
			String expression = "${&foo<<m}";
			TransitionCriteria criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
			fail("Syntax error should throw ExpressionSyntaxException");
		}
		catch (ConversionException ex) {
			// success
		}
	}

	public void testEventId() throws Exception {
		String expression = "${lastEvent.id == 'sample'}";
		TransitionCriteria criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
		RequestContext ctx = getRequestContext();
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));
		expression = "${#result == 'sample'}";
		criterion = (TransitionCriteria)new TextToTransitionCriteria().convert(expression);
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));
	}

	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.getFlowScope().setAttribute("foo", "bar");
		ctx.setLastEvent(new Event(this, "sample"));
		return ctx;
	}
}