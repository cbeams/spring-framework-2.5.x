package org.springframework.aop.target.scope;

/**
 * Strategy interface used by the ScopedTargetSource class.
 * Provides the ability to get and put objects from whatever underlying
 * storage mechanism, such as HTTP Session or Request. The scope
 * identifier passed in to this class's get and put methods will
 * identifier the scope in which the map applies.
 * <p>
 * ScopeMaps are expected to be threadsafe. One ScopeMap
 * can be used with multiple ScopedTargetSources.
 * <p>
 *  A ThreadLocal
 * strategy may be used to populate this. Alternatively the implementation
 * may look at the current proxy. If the proxy config's exposeProxy
 * flag is set to true, the proxy will have been bound to the thread
 * before the TargetSource and ScopeMap are invoked.
 * <p>
 * Can be implemented over the top of a session API
 * such as the HttpSession interface. 
 * @see org.springframework.aop.target.scope.ScopedTargetSource
 * @author Rod Johnson
 * @since 1.3
 */
public interface ScopeMap {
	
	/**
	 * Return the object or null if not found in the session
	 * @param name name to bind with
	 * @return object value or null
	 */
	Object get(Object scopeIdentifier, String name);
	
	/**
	 * Bind into the underlying session
	 * @param name name to bind with
	 * @param o object to bind
	 */
	void put(Object scopeIdentifier, String name, Object o);
	
	/**
	 * Is this scope persistent? Can we reconnect to objects
	 * from it?
	 * @param scopeIdentifier scope identifier
	 * @return whether or not this scope is persistent, meaning
	 * that the handle will be usable to reconnect to the object.
	 */
	boolean isPersistent(Object scopeIdentifier);

	/**
	 * Remove the object with the given name in the specified scope
	 * @param scopeIdentifier identifier of the scope in which to perform the removal
	 * @param name name of the object to remove
	 */
	void remove(Object scopeIdentifier, String name);
	
}