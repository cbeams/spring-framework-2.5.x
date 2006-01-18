/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.jsf.navigation;

import javax.faces.application.NavigationHandler;

/**
 * Simple holder class that associates a LocaleContext instance
 * with the current thread. The LocaleContext will be inherited
 * by any child threads spawned by the current thread.
 *
 * <p>Used as a central holder for the current Locale in Spring,
 * wherever necessary: for example, in MessageSourceAccessor.
 * DispatcherServlet automatically exposes its current Locale here.
 * Other applications can expose theirs too, to make classes like
 * MessageSourceAccessor automatically use that Locale.
 *
 * @author Colin Sampaleanu
 * @since x.x
 */
abstract class NavigationHandlerContext {

	private static ThreadLocal navigationHandler = new InheritableThreadLocal();

	/**
	 * Return the NavigationHandler associated with the current thread,
	 * if any.
	 * @return the current NavigationHandler, or <code>null</code> if none
	 */
	public static NavigationHandler getNavigationHandler() {
		return (NavigationHandler) navigationHandler.get();
	}

	/**
	 * Associate the given NavigationHandler with the current thread.
	 * @param navigationHandler the current NavigationHandler, or <code>null</code> to reset
	 * the thread-bound NavigationHandler
	 */
	public static void setNavigationHandler(NavigationHandler handler) {
		navigationHandler.set(handler);
	}
}
