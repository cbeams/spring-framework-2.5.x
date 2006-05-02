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

import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Convenient proxy factory bean for scoped objects.
 * Proxies creating using this factory bean are thread-safe singletons,
 * and may be injected, with transparent scoping behavior.
 *
 * <p>Proxies returned by this class implement the ScopedObject interface.
 * This presently allows to remove the corresponding object from the scope,
 * seamlessly creating a new instance in the scope on next access.
 *
 * <p>By default, this factory bean will create proxies that proxy the target class.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ScopedProxyFactoryBean extends AbstractSingletonProxyFactoryBean implements BeanFactoryAware {

	/** TargetSource that manages scoping */
	private final PrototypeTargetSource scopedTargetSource = new PrototypeTargetSource();

	/** The cached singleton proxy */
	private Object proxy;


	public ScopedProxyFactoryBean() {
		// Change default to proxy target class.
		setProxyTargetClass(true);
	}


	public void setTargetBeanName(String targetBeanName) {
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

		// Add an introduction that implements only the methods on ScopedObject.
		ScopedObject scopedObject = new DefaultScopedObject(cbf, this.scopedTargetSource.getTargetBeanName());
		pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject));

		this.proxy = pf.getProxy();
	}


	public Object getObject() {
		return this.proxy;
	}

	public Class getObjectType() {
		return this.scopedTargetSource.getTargetClass();
	}

}
