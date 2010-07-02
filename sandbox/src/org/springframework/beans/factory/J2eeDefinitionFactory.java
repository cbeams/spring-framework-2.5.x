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

package org.springframework.beans.factory;

import org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean;
import org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * 
 * @author Rod Johnson
 */
public abstract class J2eeDefinitionFactory {
	
	public static JndiObjectFactoryBean jndiObject(Configurer cfg, String beanName, String jndiName) {
		JndiObjectFactoryBean jofb = (JndiObjectFactoryBean) cfg.add(beanName, JndiObjectFactoryBean.class);
		jofb.setJndiName(jndiName);
		return jofb;
	}
	
	public static LocalStatelessSessionProxyFactoryBean localSlsbProxy(Configurer cfg, String beanName, String jndiName, Class businessInterface) {
		LocalStatelessSessionProxyFactoryBean ejb = (LocalStatelessSessionProxyFactoryBean) cfg.add(beanName, LocalStatelessSessionProxyFactoryBean.class);
		ejb.setJndiName(jndiName);
		ejb.setBusinessInterface(businessInterface);
		return ejb;
	}
	
	public static SimpleRemoteStatelessSessionProxyFactoryBean remoteSlsbProxy(Configurer cfg, String beanName, String jndiName, Class businessInterface) {
		SimpleRemoteStatelessSessionProxyFactoryBean ejb = (SimpleRemoteStatelessSessionProxyFactoryBean) cfg.add(beanName, LocalStatelessSessionProxyFactoryBean.class);
		ejb.setJndiName(jndiName);
		ejb.setBusinessInterface(businessInterface);
		return ejb;
	}
	
	// TODO messaging?

}
