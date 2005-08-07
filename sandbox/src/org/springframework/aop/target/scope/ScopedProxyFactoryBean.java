package org.springframework.aop.target.scope;

import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient proxy factory bean for scoped objets,
 * using ScopedTargetSource. 
 * <p>
 * By default, this factory bean will create proxies that
 * proxy target class. This will require CGLIB, as of Spring 1.3.
 * Most of the properties are shared with ScopedTargetSource--
 * refer to that class for extensive documentation.
 * <p>
 * Proxies returned by this class implement the ScopedObject interface.
 * This provides the ability to obtain a "handle" for the objects.
 * If the handle is persistent, which will depend on the backing ScopeMap
 * implementation, it will be possible to reconnect to the
 * particular instance using the reconnect(Handle) method in this class.
 * This is similar to reconnecting to a stateful session bean using its
 * handle.
 * <p>
 * Proxies creating using this factory bean are threadsafe singletons,
 * and may be injected, with transparent scoping behavior. The behavior
 * of the backing ScopeMap is driven by that of the ScopeIdentifierResolver,
 * which identifies which scope the object should be taken from. 
 * <br>If a caller calls the reconnect() method with a handle, 
 * the result will be a distinct proxy, with a fixed scope.
 * @author Rod Johnson
 * @since 1.3
 * @see org.springframework.aop.target.scope.ScopedTargetSource
 */
public class ScopedProxyFactoryBean extends AbstractSingletonProxyFactoryBean 
		implements InitializingBean, BeanFactoryAware, ScopingConfig {

	/**
	 * The cached singleton proxy
	 */
	private Object proxy;
	
	/**
	 * TargetSource that manages scoping.
	 */
	private ScopedTargetSource scopedTargetSource = new ScopedTargetSource();
	
	public ScopedProxyFactoryBean() {
		// Change default to proxy target class
		setProxyTargetClass(true);
	}

	public void setScopeMap(ScopeMap scopeMap) {
		scopedTargetSource.setScopeMap(scopeMap);
	}

	public void setScopeIdentifierResolver(ScopeIdentifierResolver scopeIdentifierResolver) {
		scopedTargetSource.setScopeIdentifierResolver(scopeIdentifierResolver);
	}
	
	public ScopeIdentifierResolver getScopeIdentifierResolver() {
		return scopedTargetSource.getScopeIdentifierResolver();
	}
	
	public void setSessionKey(String sessionKey) {
		scopedTargetSource.setSessionKey(sessionKey);
	}
	
	public void setTargetBeanName(String targetBeanName) {
		scopedTargetSource.setTargetBeanName(targetBeanName);
	}
	

	public void afterPropertiesSet() throws Exception {
		Handle handle = new DefaultHandle(getScopeIdentifierResolver(), getScopeMap(), getTargetBeanName());
		this.proxy = createProxyFactory(handle).getProxy(); 
	}

	private ProxyFactory createProxyFactory(Handle handle) {
		//	Object sampleInstance = scopedTargetSource.getBeanFactory().getBean(scopedTargetSource.getTargetBeanName());
		//System.out.println(sampleInstance);
		ProxyFactory pf = new ProxyFactory(); //new ProxyFactory(sampleInstance);
		pf.copyFrom(this);
		pf.setTargetSource(scopedTargetSource);
		
		// Add an introduction that implements only the
		// methods on ScopedObject
		pf.addAdvice(new DelegatingIntroductionInterceptor(new DefaultScopedObject(handle)));						
		return pf;
	}
	
	/**
	 * Callers can use this method to reobtain an object from a persistent handle.
	 * @param handle handle that must be persistent and compatible to this factory bean,
	 * from which a new proxy can be obtained
	 * @return a new proxy that will always reference the backing object obtained
	 * from this handle
	 */
	public Object reconnect(Handle handle) {
		if (handle == null) {
			throw new HandleNotPersistentException();
		}
		if (!handle.isPersistent()) {
			throw new HandleNotPersistentException(handle);
		}
		
		if (!handle.getTargetBeanName().equals(getTargetBeanName())) {
			throw new IncompatibleHandleException(handle, getTargetBeanName());
		}
		
		// Only ScopeIdentifierResolver differs, taking a fixed value
		ProxyFactory reconnectedProxyFactory = createProxyFactory(handle);
		ScopedTargetSource reconnectedTargetSource = new ScopedTargetSource();
		reconnectedTargetSource.copyFrom(this.scopedTargetSource);
		reconnectedTargetSource.setScopeIdentifierResolver(
				new ScopeIdentifierResolver.FixedScopeIdentifierResolver(handle.getScopeIdentifier()));
		reconnectedProxyFactory.setTargetSource(reconnectedTargetSource);
		return reconnectedProxyFactory.getProxy();
	}
	
	public Object getObject() throws Exception {
		return this.proxy;
	}

	public Class getObjectType() {
		if (isProxyTargetClass()) {
			return scopedTargetSource.getBeanFactory().getType(scopedTargetSource.getTargetBeanName());
		}
		else {
			Advised advised = (Advised) proxy;
			if (advised.getProxiedInterfaces().length == 1) {
				return advised.getProxiedInterfaces()[0];
			}
			else {
				// Can't tell
				return null;
			}
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		scopedTargetSource.setBeanFactory(beanFactory);
	}

	public String getSessionKey() {
		return scopedTargetSource.getSessionKey();
	}


	public String getTargetBeanName() {
		return scopedTargetSource.getTargetBeanName();
	}

	public ScopeMap getScopeMap() {
		return scopedTargetSource.getScopeMap();
	}
	
private class DefaultScopedObject implements ScopedObject {
		
		private Handle handle;
		
		public DefaultScopedObject(Handle handle) {
			this.handle = handle;
		}
		
		public ScopeMap getScopeMap() {
			return scopedTargetSource.getScopeMap();
		}
		
		public String getSessionKey() {
			return scopedTargetSource.getSessionKey();
		}
		
		public String getTargetBeanName() {
			return scopedTargetSource.getTargetBeanName();
		}
		
		public Handle getHandle() {
			return handle;
		}
		
		public void remove() {
			getScopeMap().remove(handle.getScopeIdentifier(), handle.getTargetBeanName());
		}
	}
	
	
	public static class DefaultHandle implements Handle {
		
		private final Object scopeIdentifier;
		private final String targetBeanName;
		private final boolean persistent;
		
		public DefaultHandle(ScopeIdentifierResolver sir, ScopeMap scopeMap, String targetBeanName) {
			this.scopeIdentifier = sir.getScopeIdentifier();
			this.targetBeanName = targetBeanName;
			this.persistent = scopeMap.isPersistent(scopeIdentifier);
		}

		public Object getScopeIdentifier() {
			return scopeIdentifier;
		}

		public String getTargetBeanName() {
			return targetBeanName;
		}

		public boolean isPersistent() {
			return persistent;
		}
	}

}
