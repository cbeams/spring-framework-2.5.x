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

package org.springframework.remoting.caucho;

import java.net.MalformedURLException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for Hessian proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface.
 *
 * <p>The service URL must be an HTTP URL exposing a Hessian service.
 * For details, see HessianClientInterceptor docs.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see HessianClientInterceptor
 * @see HessianServiceExporter
 */
public class HessianProxyFactoryBean extends HessianClientInterceptor implements FactoryBean {

	private Object serviceProxy;

	public void afterPropertiesSet() throws MalformedURLException {
		super.afterPropertiesSet();
		this.serviceProxy = ProxyFactory.getProxy(getServiceInterface(), this);
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public Class getObjectType() {
		return (this.serviceProxy != null) ? this.serviceProxy.getClass() : getServiceInterface();
	}
	
	public boolean isSingleton() {
		return true;
	}

}
