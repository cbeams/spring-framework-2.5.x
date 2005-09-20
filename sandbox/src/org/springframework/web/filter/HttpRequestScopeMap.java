/**
 * 
 */
package org.springframework.web.filter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.aop.target.scope.ScopeMap;
import org.springframework.util.Assert;

/**
 * HttpRequest-backed ScopeMap implementation. Relies
 * on a thread bound request.
 * @author Rod Johnson
 * @since 1.3
 * @see org.springframework.web.util.RequestHolder
 * @see org.springframework.web.filter.RequestBindingFilter
 */
public class HttpRequestScopeMap implements ScopeMap {
		
	public Object get(Object scopeIdentifier, String name) {
		Assert.isInstanceOf(HttpServletRequest.class, scopeIdentifier, "Scope identifier is not HTTP request object!");
		return ((HttpServletRequest)scopeIdentifier).getAttribute(name);
	}

	public void put(Object scopeIdentifier, String name, Object o) {
		Assert.isInstanceOf(HttpServletRequest.class, scopeIdentifier, "Scope identifier is not HTTP request object!");
		((HttpServletRequest)scopeIdentifier).setAttribute(name, o);
	}
	
	public boolean isPersistent(Object scopeIdentifier) {
		return false;
	}

	public void remove(Object scopeIdentifier, String name) {
		Assert.isInstanceOf(HttpServletRequest.class, scopeIdentifier, "Scope identifier is not HTTP request object!");
		((HttpServletRequest)scopeIdentifier).removeAttribute(name);
	}
}