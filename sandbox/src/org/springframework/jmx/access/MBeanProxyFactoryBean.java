/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jmx.access;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class MBeanProxyFactoryBean extends MBeanClientInterceptor implements FactoryBean, InitializingBean {

	private Class managementInterface;

	private Object managementProxy;

	public void setManagementInterface(Class managementInterface) {
		this.managementInterface = managementInterface;
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		if (this.managementInterface == null) {
			throw new IllegalArgumentException("managementInterface is required");
		}
		this.managementProxy = ProxyFactory.getProxy(this.managementInterface, this);
	}

	public Object getObject() throws Exception {
		return this.managementProxy;
	}

	public Class getObjectType() {
		return (this.managementProxy != null ? this.managementProxy.getClass() : null);
	}

	public boolean isSingleton() {
		return true;
	}

}
