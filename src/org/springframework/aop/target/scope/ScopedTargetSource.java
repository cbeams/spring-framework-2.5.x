package org.springframework.aop.target.scope;

import org.springframework.aop.target.AbstractPrototypeBasedTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

/**
 * TargetSource that adds per-scope capabilities to Spring IoC
 * container. The target will be sourced from a ScopeMap Strategy
 * interface, which can be backed by a variety of session stores.
 * Thus this TargetSource can be used for a variety of scopes such
 * as per HTTP request or session, or even a stateful model along the
 * lines of Stateful Session EJBs.
 * <p>
 * Targets will be created lazily in the scope. Scope map
 * implementations are expected to be threadsafe.
 * <p>
 * ScopeMap and targetBeanNames (inherited) are required properties. 
 * The default ScopeIdentifierResolver may be used when using a ScopeMap
 * that is parameterized by a ThreadLocal or other context information.
 * The sessionKey property may optionally be set to specify the key for
 * the scope object in the ScopeMap. 
 * @see org.springframework.aop.target.scope.ScopeMap
 * @author Rod Johnson
 * @since 1.3
 */
public class ScopedTargetSource extends AbstractPrototypeBasedTargetSource
	implements ScopingConfig {
	
	private String sessionKey;
	
	private ScopeMap scopeMap;
	
	private ScopeIdentifierResolver scopeIdentifierResolver = ScopeIdentifierResolver.CONTEXT_BASED_SCOPE_IDENTIFIER_RESOLVER;
	
	public void copyFrom(ScopedTargetSource other) {
		super.copyFrom(other);
		this.scopeIdentifierResolver = other.scopeIdentifierResolver;
		this.sessionKey = other.sessionKey;
		this.scopeMap = other.scopeMap;
	}
	
	/**
	 * Set the strategy interface used to resolve the identifier of the scope to resolve
	 * for this thread.
	 * The default value will work for any implementation that knows the scope identifier
	 * based on the context: for example, an HttpRequest bound to the current thread
	 * @param scopeIdentifierResolver strategy interface used to resolve the identifier
	 * of the scope to use for this invocation.
	 */
	public void setScopeIdentifierResolver(ScopeIdentifierResolver scopeIdentifierResolver) {
		this.scopeIdentifierResolver = scopeIdentifierResolver;
	}
	
	/**
	 * Set the object that will be used to resolve the target object in a given scope
	 * @param scopeMap strategy interface used to resolve the target object
	 * in the gien scope
	 */
	public void setScopeMap(ScopeMap scopeMap) {
		this.scopeMap = scopeMap;
	}
	
	/**
	 * Set the sessionKey (optional property). Will be automatically generated if not set.
	 * @param sessionEntryName
	 */
	public void setSessionKey(String sessionEntryName) {
		this.sessionKey = sessionEntryName;
	}
	
	/**
	 * Overridden to autogenerate sessionKey if the property was not set.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		if (this.sessionKey == null) {
			// Autogenerate session key
			this.sessionKey = getClass().getName() + "_" + 
				getTargetBeanName() + "_" + 
				System.identityHashCode(this);
		}
	}
	
	/**
	 * Return a target, lazily initializing in the current scope.
	 * The object will be stored in the scope map.
	 */
	public Object getTarget() throws Exception {
		Object o = scopeMap.get(scopeIdentifierResolver.getScopeIdentifier(), this.sessionKey);
		if (o == null) {
			// Lazily initialize in scope
			logger.info("Creating new scoped instance of object with name '" + sessionKey + "'");
			o = newPrototypeInstance();
			scopeMap.put(scopeIdentifierResolver.getScopeIdentifier(), this.sessionKey, o);
		}
		return o;
	}

	public String toString() {
		return "ScopedTargetSource: sessionKey='" + sessionKey + "'; scopeMap=[" + scopeMap + "]; " +
			"scopeIdentifierResolver=[" + scopeIdentifierResolver + "]";
	}

	public String getSessionKey() {
		return this.sessionKey;
	}

	public ScopeMap getScopeMap() {
		return this.scopeMap;
	}

	public ScopeIdentifierResolver getScopeIdentifierResolver() {
		return scopeIdentifierResolver;
	}
}
