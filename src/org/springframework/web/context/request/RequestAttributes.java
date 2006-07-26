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

package org.springframework.web.context.request;

/**
 * Abstraction for accessing attribute objects associated with a request.
 * Supports access to request-scoped attributes as well as to session-scoped
 * attributes, with the optional notion of a "global session".
 *
 * <p>Can be implemented for any kind of request/session mechanism,
 * in particular for servlet requests and portlet requests.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ServletRequestAttributes
 * @see org.springframework.web.portlet.context.PortletRequestAttributes
 */
public interface RequestAttributes {

	/**
	 * Constant that indicates request scope.
	 */
	int SCOPE_REQUEST = 0;

	/**
	 * Constant that indicates session scope.
	 * <p>This preferably refers to a locally isolated session, if such
	 * a distinction is available (for example, in a Portlet environment).
	 * Else, it simply refers to the common session.
	 */
	int SCOPE_SESSION = 1;

	/**
	 * Constant that indicates global session scope.
	 * <p>This explicitly refers to a globally shared session, if such
	 * a distinction is available (for example, in a Portlet environment).
	 * Else, it simply refers to the common session.
	 */
	int SCOPE_GLOBAL_SESSION = 2;


	/**
	 * Return the value for the scoped attribute of the given name, if any.
	 * @param name the name of the attribute
	 * @return the current attribute value, or <code>null</code> if not found
	 */
	Object getAttribute(String name, int scope);

	/**
	 * Set the value for the scoped attribute of the given name.
	 * Replaces an existing value, if any.
	 * @param name the name of the attribute
	 * @param value the value for the attribute
	 */
	void setAttribute(String name, Object value, int scope);

	/**
	 * Remove the scoped attribute of the given name, if it exists.
	 * @param name the name of the attribute
	 */
	void removeAttribute(String name, int scope);

	/**
	 * Expose the best available mutex for the underlying session:
	 * that is, an object to synchronize on for the underlying session.
	 */
	Object getSessionMutex();

}
