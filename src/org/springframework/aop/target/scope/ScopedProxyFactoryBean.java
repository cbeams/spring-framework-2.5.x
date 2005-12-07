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
 * Convenient proxy factory bean for scoped objects, using ScopedTargetSource.
 *
 * <p>By default, this factory bean will create proxies that proxy target class.
 * This will require CGLIB, as of Spring 1.3. Most of the properties are shared
 * with ScopedTargetSource; refer to that class for extensive documentation.
 *
 * <p>Proxies returned by this class implement the ScopedObject interface.
 * This provides the ability to obtain a "handle" for the objects.
 * If the handle is persistent, which will depend on the backing ScopeMap
 * implementation, it will be possible to reconnect to the particular instance
 * using the <code>reconnect(Handle)</code> method in this class. This is
 * similar to reconnecting to a stateful session bean using its handle.
 *
 * <p>Proxies creating using this factory bean are thread-safe singletons,
 * and may be injected, with transparent scoping behavior.
 *
 * <p>If a caller calls the reconnect() method with a handle, the result
 * will be a distinct proxy, with a fixed scope.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.target.scope.ScopedTargetSource
 */
public class ScopedProxyFactoryBean extends AbstractSingletonProxyFactoryBean 
		implements InitializingBean, BeanFactoryAware, ScopingConfig {

	/** The cached singleton proxy */
	private Object proxy;
	
	/** TargetSource that manages scoping */
	private ScopedTargetSource scopedTargetSource = new ScopedTargetSource();


	public ScopedProxyFactoryBean() {
		// Change default to proxy target class
		setProxyTargetClass(true);
	}

	public void setScopeMap(ScopeMap scopeMap) {
		this.scopedTargetSource.setScopeMap(scopeMap);
	}

	public void setScopeKey(String scopeKey) {
		this.scopedTargetSource.setScopeKey(scopeKey);
	}
	
	public void setTargetBeanName(String targetBeanName) {
		this.scopedTargetSource.setTargetBeanName(targetBeanName);
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		scopedTargetSource.setBeanFactory(beanFactory);
	}


	public void afterPropertiesSet() throws Exception {
		DefaultHandle handle = new DefaultHandle(getTargetBeanName(), getScopeMap().isPersistent());
		this.proxy = createProxyFactory(handle).getProxy();
	}

	private ProxyFactory createProxyFactory(DefaultHandle handle) {
		ProxyFactory pf = new ProxyFactory();
		pf.copyFrom(this);
		pf.setTargetSource(this.scopedTargetSource);
		
		if (this.scopedTargetSource.getTargetClass().isInterface()) {
			pf.addInterface(this.scopedTargetSource.getTargetClass());
			pf.setProxyTargetClass(false);
		}
		
		// Add an introduction that implements only the methods on ScopedObject.
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
		if (!(handle instanceof DefaultHandle)) {
			throw new IncompatibleHandleException(handle, "Handle is not a ScopedProxyFactoryBean handle");
		}
		DefaultHandle defHandle = (DefaultHandle) handle;
		if (!defHandle.getTargetBeanName().equals(getTargetBeanName())) {
			throw new IncompatibleHandleException(
					handle, "Handle does not point to target bean '" + getTargetBeanName() + "'");
		}
		
		ProxyFactory reconnectedProxyFactory = createProxyFactory(defHandle);
		ScopedTargetSource reconnectedTargetSource = new ScopedTargetSource();
		reconnectedTargetSource.copyFrom(this.scopedTargetSource);
		reconnectedProxyFactory.setTargetSource(reconnectedTargetSource);
		return reconnectedProxyFactory.getProxy();
	}


	public ScopeMap getScopeMap() {
		return scopedTargetSource.getScopeMap();
	}

	public String getScopeKey() {
		return scopedTargetSource.getScopeKey();
	}

	public String getTargetBeanName() {
		return scopedTargetSource.getTargetBeanName();
	}


	public Object getObject() {
		return this.proxy;
	}

	public Class getObjectType() {
		if (isProxyTargetClass()) {
			return this.scopedTargetSource.getBeanFactory().getType(this.scopedTargetSource.getTargetBeanName());
		}
		Advised advised = (Advised) proxy;
		if (advised.getProxiedInterfaces().length == 1) {
			return advised.getProxiedInterfaces()[0];
		}
		return null;
	}


	private class DefaultScopedObject implements ScopedObject {
		
		private DefaultHandle handle;
		
		public DefaultScopedObject(DefaultHandle handle) {
			this.handle = handle;
		}
		
		public ScopeMap getScopeMap() {
			return scopedTargetSource.getScopeMap();
		}
		
		public String getScopeKey() {
			return scopedTargetSource.getScopeKey();
		}
		
		public String getTargetBeanName() {
			return scopedTargetSource.getTargetBeanName();
		}
		
		public Handle getHandle() {
			return handle;
		}
		
		public void remove() {
			getScopeMap().remove(handle.getTargetBeanName());
		}
	}
	
	
	private class DefaultHandle implements Handle {
		
		private final String targetBeanName;
		private final boolean persistent;

		public DefaultHandle(String targetBeanName, boolean persistent) {
			this.targetBeanName = targetBeanName;
			this.persistent = persistent;
		}

		public boolean isPersistent() {
			return persistent;
		}

		public String getTargetBeanName() {
			return targetBeanName;
		}
	}

}
