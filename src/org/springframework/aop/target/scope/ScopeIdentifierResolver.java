package org.springframework.aop.target.scope;

/**
 * Interface used to source the identifier of the current scope
 * for use with a ScopeMap.
 * @see org.springframework.aop.target.scope.ScopeMap
 * @author Rod Johnson
 * @since 1.3
 */
public interface ScopeIdentifierResolver {
	
	/**
	 * Canonical scope identifier value used when no scope
	 * identifier is required by a ScopeMap implementation,
	 * but the current scope can be dervied from the context.
	 */
	Object CONTEXT_BASED_SCOPE = new Object();

	/**
	 * Return the identifier of the current scope.
	 * How this is populated is dependent on the implementation.
	 * A ThreadLocal implementation may be used or the source may
	 * be a cache that is cleared every so often. Implementations must
	 * return the same value in successive method calls within
	 * the one scope.
	 * @throws IllegalStateException if there is no current scope
	 * @return the scope identifier.
	 */
	Object getScopeIdentifier() throws IllegalStateException;
	
	/**
	 * Canonical instance used when the given ScopeMap is parameterized
	 * by its context--typically, by a thread-bound resource--and does
	 * not care about the value of the scopeId parameter passed to its
	 * get() and put() methods.
	 */
	ScopeIdentifierResolver CONTEXT_BASED_SCOPE_IDENTIFIER_RESOLVER = new ScopeIdentifierResolver() {
		public Object getScopeIdentifier() {
			return CONTEXT_BASED_SCOPE;
		}
	};
	
	/**
	 * Convenient implementation that always returns the given scope id.
	 */
	class FixedScopeIdentifierResolver implements ScopeIdentifierResolver {
		private final Object id;
		
		public FixedScopeIdentifierResolver(Object id) {
			this.id = id;
		}
		
		public Object getScopeIdentifier() throws IllegalStateException {
			return id;
		}
	}
	
}
