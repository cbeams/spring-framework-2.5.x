/**
 * 
 */
package org.springframework.web.filter;

import org.springframework.aop.target.scope.ScopeMap;
import org.springframework.web.util.RequestHolder;

/**
 * HttpRequest-backed ScopeMap implementation. Relies
 * on a thread bound request.
 * @author Rod Johnson
 * @since 1.3
 * @see org.springframework.web.util.RequestHolder
 * @see org.springframework.web.filter.RequestBindingFilter
 */
public class HttpRequestScopeMap implements ScopeMap {
	
	public Object get(Object scopeId, String name) {
		return RequestHolder.currentRequest().getAttribute(name);
	}

	public void put(Object scopeId, String name, Object o) {
		RequestHolder.currentRequest().setAttribute(name, o);
	}
	
	public boolean isPersistent(Object scopeIdentifier) {
		return false;
	}

	public void remove(Object scopeIdentifier, String name) {
		RequestHolder.currentRequest().removeAttribute(name);
	}
}