package org.springframework.aop.target.scope;

/**
 * Adds management, such as passivation etc.
 * @author Rod Johnson
 * TODO for illustration only at this point
 */
public interface ManagedScopeMap extends ScopeMap {
	
	int size(Object scopeId);
	
	Object remove(Object scopeId, String name);
	
	/**
	 * Clear the scope with the given identifier
	 * @param scopeId scope identifier
	 */
	void clear(Object scopeId);
	
	/**
	 * Clear all scopes
	 *
	 */
	void clearAll();
	
	boolean isClusterable();

}
