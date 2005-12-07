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

package org.springframework.web.context.scope;

import org.springframework.aop.target.scope.ScopeMap;

/**
 * HttpServletRequest-backed ScopeMap implementation. Relies
 * on a thread-bound request, which can be exported through
 * RequestContextListener, RequestContextFilter or .
 *
 * @author Rod Johnson
 * @since 2.0
 * @see RequestContextHolder#currentRequest()
 */
public class RequestScopeMap implements ScopeMap {

	public boolean isPersistent() {
		return false;
	}

	public Object get(String name) {
		return RequestContextHolder.currentRequest().getAttribute(name);
	}

	public void put(String name, Object value) {
		RequestContextHolder.currentRequest().setAttribute(name, value);
	}

	public void remove(String name) {
		RequestContextHolder.currentRequest().removeAttribute(name);
	}

}
