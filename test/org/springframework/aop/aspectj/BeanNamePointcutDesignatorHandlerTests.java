/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.aspectj;

import junit.framework.TestCase;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;

/**
 * Unit tests for the <code>BeanNamePointcutDesignatorHandler</code> class.
 *
 * @author Rick Evans
 */
public final class BeanNamePointcutDesignatorHandlerTests extends TestCase {

	private static final String BEAN_NAME = "service";


	private PointcutDesignatorHandler handler;


	protected void setUp() throws Exception {
		this.handler = new BeanNamePointcutDesignatorHandler();
	}


	public void testStaticMatchWhenNoProxyingIsGoingOn() {
		FuzzyBoolean isMatch = match("bean(" + BEAN_NAME + ")");
		assertEquals(FuzzyBoolean.YES, isMatch);
	}

	public void testStaticMatchWhenProxyingIsGoingOnValidMatch() {
		try {
			ProxyCreationContext.notifyProxyCreationStart(BEAN_NAME, false);
			FuzzyBoolean isMatch = match(BEAN_NAME);
			assertEquals(FuzzyBoolean.YES, isMatch);
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
	}

	public void testStaticMatchWhenProxyingIsGoingOn_ForInnerBeanValidMatch() {
		try {
			ProxyCreationContext.notifyProxyCreationStart(BEAN_NAME, true);
			FuzzyBoolean isMatch = match(BEAN_NAME);
			assertEquals("Inner beans must never match.", FuzzyBoolean.NO, isMatch);
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
	}

	public void testStaticMatchWhenProxyingIsGoingOnNoMatch() {
		try {
			ProxyCreationContext.notifyProxyCreationStart(BEAN_NAME, false);
			FuzzyBoolean isMatch = match("whatever");
			assertEquals(FuzzyBoolean.NO, isMatch);
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
	}

	public void testStaticMatchWhenProxyingIsGoingOn_ForInnerBeanNoMatch() {
		try {
			ProxyCreationContext.notifyProxyCreationStart(BEAN_NAME, true);
			FuzzyBoolean isMatch = match("whatever");
			assertEquals("Inner beans must never match.", FuzzyBoolean.NO, isMatch);
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
	}


	private FuzzyBoolean match(String expression) {
		ContextBasedMatcher matcher = this.handler.parse(expression);
		return matcher.matchesStatically(new StubMatchingContext());
	}


	private static final class StubMatchingContext implements MatchingContext {

		public boolean hasContextBinding(String string) {
			throw new UnsupportedOperationException();
		}

		public Object getBinding(String string) {
			throw new UnsupportedOperationException();
		}
	}

}
