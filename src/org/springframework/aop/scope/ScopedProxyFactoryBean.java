/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.scope;

import java.lang.reflect.Modifier;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.ClassUtils;

/**
 * Convenient proxy factory bean for scoped objects.
 * 
 * <p>Proxies created using this factory bean are thread-safe singletons,
 * and may be injected, with transparent scoping behavior.
 *
 * <p>Proxies returned by this class implement the ScopedObject interface.
 * This presently allows to remove the corresponding object from the scope,
 * seamlessly creating a new instance in the scope on next access.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ScopedProxyFactoryBean extends ProxyConfig implements FactoryBean, BeanFactoryAware {

	/** TargetSource that manages scoping */
	private final PrototypeTargetSource scopedTargetSource = new PrototypeTargetSource();

	private String targetBeanName;

	/** The cached singleton proxy */
	private Object proxy;


	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
		this.scopedTargetSource.setTargetBeanName(targetBeanName);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory)) {
			throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
		}
		ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;

		this.scopedTargetSource.setBeanFactory(beanFactory);

		ProxyFactory pf = new ProxyFactory();
		pf.copyFrom(this);
		pf.setTargetSource(this.scopedTargetSource);

		Class beanType = beanFactory.getType(this.targetBeanName);
		boolean isPrivate = Modifier.isPrivate(beanType.getModifiers());
		if (isPrivate || beanType.isInterface()) {
			pf.setInterfaces(ClassUtils.getAllInterfacesForClass(beanType));
		} else {
			pf.setProxyTargetClass(true);
		}

		// Add an introduction that implements only the methods on ScopedObject.
		ScopedObject scopedObject = new DefaultScopedObject(cbf, this.scopedTargetSource.getTargetBeanName());
		pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject));

		this.proxy = pf.getProxy();
	}


	public Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	public Class getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		if (this.scopedTargetSource != null) {
			return this.scopedTargetSource.getTargetClass();
		}
		return null;
	}

	public boolean isSingleton() {
		return true;
	}

}
