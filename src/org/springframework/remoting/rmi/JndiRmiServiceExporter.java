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

package org.springframework.remoting.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiAccessor;

/**
 * Service exporter which binds RMI services to JNDI.
 * Typically used for RMI-IIOP (CORBA).
 *
 * <p>Exports services via the <code>PortableRemoteObject</code> class.
 * You need to run "rmic" with the "-iiop" option to generate corresponding
 * stubs and skeletons for each exported service.
 *
 * <p>The JNDI environment can be specified as jndiEnvironment property,
 * or be configured in a jndi.properties file or as system properties.
 * For example:
 *
 * <pre>
 * &lt;property name="jndiEnvironment"&gt;
 * 	 &lt;props>
 *		 &lt;prop key="java.naming.factory.initial"&gt;com.sun.jndi.cosnaming.CNCtxFactory&lt;/prop&gt;
 *		 &lt;prop key="java.naming.provider.url"&gt;iiop://localhost:1050&lt;/prop&gt;
 *	 &lt;/props&gt;
 * &lt;/property&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setService
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setJndiName
 * @see JndiRmiClientInterceptor
 * @see JndiRmiProxyFactoryBean
 * @see javax.rmi.PortableRemoteObject#exportObject
 */
public class JndiRmiServiceExporter extends JndiAccessor implements InitializingBean, DisposableBean {

	private Remote service;

	private String jndiName;

	/**
	 * Set the RMI service to export.
	 * Typically populated via a bean reference.
	 */
	public void setService(Remote service) {
		this.service = service;
	}

	/**
	 * Set the JNDI name of the exported RMI service.
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * Register the service as RMI object.
	 * Creates an RMI registry on the specified port if none exists.
	 */
	public void afterPropertiesSet() throws NamingException, RemoteException {
		if (this.jndiName == null) {
			throw new IllegalArgumentException("jndiName is required");
		}
		if (logger.isInfoEnabled()) {
			logger.info("Binding RMI service to JNDI location [" + this.jndiName + "]");
		}
		PortableRemoteObject.exportObject(this.service);
		getJndiTemplate().rebind(this.jndiName, this.service);
	}

	/**
	 * Unbind the RMI service from JNDI at bean factory shutdown.
	 */
	public void destroy() throws NamingException, NoSuchObjectException {
		if (logger.isInfoEnabled()) {
			logger.info("Unbinding RMI service from JNDI location [" + this.jndiName + "]");
		}
		getJndiTemplate().unbind(this.jndiName);
		PortableRemoteObject.unexportObject(this.service);
	}

}
