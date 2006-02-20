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

package org.springframework.web.jsf;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 */
public class DelegatingNavigationHandlerTests extends TestCase {

	private MockFacesContext facesContext;
	private StaticListableBeanFactory beanFactory;
	private TestNavigationHandler origNavHandler;
	private DelegatingNavigationHandlerProxy delNavHandler;

	protected void setUp() {
		facesContext = new MockFacesContext();
		beanFactory = new StaticListableBeanFactory();
		origNavHandler = new TestNavigationHandler();

		delNavHandler = new DelegatingNavigationHandlerProxy(origNavHandler) {
			protected BeanFactory getBeanFactory(FacesContext facesContext) {
				return beanFactory;
			}
		};
	}

	public void testHandleNavigationWithoutDecoration() {
		TestNavigationHandler targetHandler = new TestNavigationHandler();
		beanFactory.addBean("jsfNavigationHandler", targetHandler);

		delNavHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals(targetHandler.lastFromAction, "fromAction");
		assertEquals(targetHandler.lastOutcome, "myViewId");
	}

	public void testHandleNavigationWithDecoration() {
		TestDecoratingNavigationHandler targetHandler = new TestDecoratingNavigationHandler();
		beanFactory.addBean("jsfNavigationHandler", targetHandler);

		delNavHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);

		// Original handler must have been invoked as well...
		assertEquals("fromAction", origNavHandler.lastFromAction);
		assertEquals("myViewId", origNavHandler.lastOutcome);
	}

	public void testHandleNavigationWithTwoLevelDecoration() {
		TestNavigationHandler lowerTargetHandler = new TestNavigationHandler();
		TestDecoratingNavigationHandler targetHandler = new TestDecoratingNavigationHandler(lowerTargetHandler);
		beanFactory.addBean("jsfNavigationHandler", targetHandler);

		delNavHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must NOT have been invoked,
		// since lower handler wasn't a DecoratingNavigationHandler.
		assertNull(origNavHandler.lastFromAction);
		assertNull(origNavHandler.lastOutcome);
	}

	public void testHandleNavigationWithTwoLevelDecorationStandardChaining() {
		TestDecoratingNavigationHandler lowerTargetHandler = new TestDecoratingNavigationHandler();
		TestNavigationHandler targetHandler = new TestNavigationHandler(lowerTargetHandler);
		beanFactory.addBean("jsfNavigationHandler", targetHandler);

		delNavHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must NOT have been invoked,
		// since lower handler wasn't a DecoratingNavigationHandler.
		assertNull(origNavHandler.lastFromAction);
		assertNull(origNavHandler.lastOutcome);
	}

	public void testHandleNavigationWithTwoLevelDecorationBackToOriginal() {
		TestDecoratingNavigationHandler lowerTargetHandler = new TestDecoratingNavigationHandler();
		TestDecoratingNavigationHandler targetHandler = new TestDecoratingNavigationHandler(lowerTargetHandler);
		beanFactory.addBean("jsfNavigationHandler", targetHandler);

		delNavHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must have been invoked as well,
		// since lower handler was a DecoratingNavigationHandler.
		assertEquals("fromAction", origNavHandler.lastFromAction);
		assertEquals("myViewId", origNavHandler.lastOutcome);
	}

	public void testStandardNavigationWithTwoLevelDecoration() {
		TestNavigationHandler lowerTargetHandler = new TestNavigationHandler();
		TestDecoratingNavigationHandler targetHandler = new TestDecoratingNavigationHandler(lowerTargetHandler);

		targetHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must NOT have been invoked,
		// since lower handler wasn't a DecoratingNavigationHandler.
		assertNull(origNavHandler.lastFromAction);
		assertNull(origNavHandler.lastOutcome);
	}

	public void testStandardNavigationWithTwoLevelDecorationStandardChaining() {
		TestDecoratingNavigationHandler lowerTargetHandler = new TestDecoratingNavigationHandler();
		TestNavigationHandler targetHandler = new TestNavigationHandler(lowerTargetHandler);

		targetHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must NOT have been invoked,
		// since lower handler wasn't a DecoratingNavigationHandler.
		assertNull(origNavHandler.lastFromAction);
		assertNull(origNavHandler.lastOutcome);
	}

	public void testStandardNavigationWithTwoLevelDecorationBackToOriginal() {
		TestDecoratingNavigationHandler lowerTargetHandler = new TestDecoratingNavigationHandler(origNavHandler);
		TestDecoratingNavigationHandler targetHandler = new TestDecoratingNavigationHandler(lowerTargetHandler);

		targetHandler.handleNavigation(facesContext, "fromAction", "myViewId");
		assertEquals("fromAction", targetHandler.lastFromAction);
		assertEquals("myViewId", targetHandler.lastOutcome);
		assertEquals("fromAction", lowerTargetHandler.lastFromAction);
		assertEquals("myViewId", lowerTargetHandler.lastOutcome);

		// Original handler must have been invoked as well,
		// since lower handler was a DecoratingNavigationHandler.
		assertEquals("fromAction", origNavHandler.lastFromAction);
		assertEquals("myViewId", origNavHandler.lastOutcome);
	}


	public static class TestNavigationHandler extends NavigationHandler {

		private NavigationHandler originalNavigationHandler;
		private String lastFromAction;
		private String lastOutcome;

		public TestNavigationHandler() {
		}

		public TestNavigationHandler(NavigationHandler originalNavigationHandler) {
			this.originalNavigationHandler = originalNavigationHandler;
		}

		public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
			lastFromAction = fromAction;
			lastOutcome = outcome;
			if (this.originalNavigationHandler != null) {
				this.originalNavigationHandler.handleNavigation(facesContext, fromAction, outcome);
			}
		}
	}


	public static class TestDecoratingNavigationHandler extends DecoratingNavigationHandler {

		private String lastFromAction;
		private String lastOutcome;

		public TestDecoratingNavigationHandler() {
		}

		public TestDecoratingNavigationHandler(NavigationHandler originalNavigationHandler) {
			super(originalNavigationHandler);
		}

		public void handleNavigation(
				FacesContext facesContext, String fromAction, String outcome, NavigationHandler originalNavigationHandler) {
			lastFromAction = fromAction;
			lastOutcome = outcome;
			callNextHandlerInChain(facesContext, fromAction, outcome, originalNavigationHandler);
		}
	}

}
