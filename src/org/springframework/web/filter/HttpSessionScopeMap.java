/**
 * 
 */
package org.springframework.web.filter;

import javax.servlet.http.HttpSession;

import org.springframework.aop.target.scope.ScopeMap;
import org.springframework.web.util.RequestHolder;

/**
 * HttpSession-backed ScopeMap implementation. Relies
 * on a thread bound request.
 * @author Rod Johnson
 * @since 1.3
 * @see org.springframework.web.util.RequestHolder
 * @see org.springframework.web.filter.RequestBindingFilter
 */
public class HttpSessionScopeMap implements ScopeMap {

	private boolean synchronizeOnSession = false;

	/**
	 * Set if return should be synchronized on the session, to serialize
	 * parallel invocations from the same client.
	 */
	public final void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	public Object get(Object scopeIdentifier, String name) {
		HttpSession session = RequestHolder.currentSession();
		if (this.synchronizeOnSession) {
			synchronized (session) {
				return session.getAttribute(name);
			}
		}

		return session.getAttribute(name);
	}

	public void put(Object scopeIdentifier, String name, Object o) {
		HttpSession session = RequestHolder.currentSession();
		if (this.synchronizeOnSession) {
			synchronized (session) {
				session.setAttribute(name, o);
			}
		}
		session.setAttribute(name, o);
	}

	public boolean isPersistent(Object scopeIdentifier) {
		return false;
	}

	public void remove(Object scopeIdentifier, String name) {
		RequestHolder.currentSession().removeAttribute(name);
	}

}