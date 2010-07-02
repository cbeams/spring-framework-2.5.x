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
package org.springframework.web.servlet.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.core.OrderComparator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Provides support for propagating session events to an
 * <code>WebApplicationContext</code>. Calls to <code>sessionCreated()</code>
 * and <code>sessionDestroyed()</code> are subsequently routed to all beans
 * implementing the <code>HttpSessionListener</code> interface registering in
 * the application context, after they're sorted using the
 * <code>OrderComparator</code>
 * <p>
 * 
 * Note: error handling is not sorted out yet in this class. The Servlet 2.4
 * specification is unclear about what happens to multiple listeners registered
 * in a web application where one of the listeners throws an unchecked exception
 * (see the Servlet specification section SRV.10.6). Currently, if an unchecked
 * exception is thrown by one of the listeners registered in the application
 * context (retrieved through the <code>ServletContext</code> available from
 * the <code>HttpSession</code> in the <code>HttpSessionEvent</code>), it
 * won't be caught by this class hence the other listener registered in the
 * context won't be notified. This might need to change later on.
 * <p>
 * 
 * Motivation: to allow for easy registration of session listeners in an
 * application context thus letting them profit from the Spring DI features.
 * <p>
 * 
 * The <code>WebApplicationContextSessionListener</code> should be wired up as
 * any other listener in the <code>web.xml</code> file:
 * <p>
 * 
 * <pre>
 * 
 *  &lt;listener&gt;
 *    &lt;listener-class&gt;org.springframework.web.servlet.support.WebApplicationContextSessionListener&lt;listener-class&gt;
 *  &lt;/listener&gt;
 *  
 * </pre>
 * 
 * Note that a WebApplicationContext <i>is </i> required by this listener. The
 * context is retrieved by calling
 * {@link WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)},
 * causing an unchecked exception to be thrown if no context could be found.
 * <p>
 * 
 * <b>WARNING UNTESTED!!!</b>
 * 
 * @see javax.servlet.http.HttpSessionListener
 * @see org.springframework.core.Ordered
 * 
 * @author Alef Arendsen
 */
public final class WebApplicationContextSessionListener implements
		HttpSessionListener {

	/** the application context to inspect */
	private WebApplicationContext webApplicationContext = null;

	/**
	 * Inspects the WebApplicationContext for beans implementing the
	 * <code>HttpSessionListener</code> interface, sorts them and propagates
	 * all calls to the listeners.
	 */
	public void sessionCreated(HttpSessionEvent evt) {
		List l = retrieveAllListeners(evt);
		Iterator it = l.iterator();
		while (it.hasNext()) {
			HttpSessionListener listener = (HttpSessionListener) it.next();
			listener.sessionCreated(evt);
		}
	}

	/**
	 * Inspects the WebApplicationContext for beans implementing the
	 * <code>HttpSessionListener</code> interface, sorts them and propagates
	 * all calls to the listeners.
	 */
	public void sessionDestroyed(HttpSessionEvent evt) {
		List l = retrieveAllListeners(evt);
		Iterator it = l.iterator();
		while (it.hasNext()) {
			HttpSessionListener listener = (HttpSessionListener) it.next();
			listener.sessionDestroyed(evt);
		}
	}

	private List retrieveAllListeners(HttpSessionEvent evt) {
		if (this.webApplicationContext == null) {
			this.webApplicationContext = retrieveWebApplicationContext(evt);
		}

		// retrieve all session listeners and sort
		Map m = webApplicationContext.getBeansOfType(HttpSessionListener.class,
				true, true);
		List l = new ArrayList(m.values());
		Collections.sort(l, new OrderComparator());
		return l;
	}

	private WebApplicationContext retrieveWebApplicationContext(
			HttpSessionEvent evt) {
		HttpSession session = evt.getSession();
		ServletContext sContext = session.getServletContext();
		// requires a web application context, otherwise there's no sense in
		// registering the listener anyway
		return WebApplicationContextUtils
				.getRequiredWebApplicationContext(sContext);
	}

}