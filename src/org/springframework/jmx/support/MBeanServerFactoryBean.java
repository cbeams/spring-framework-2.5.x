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

package org.springframework.jmx.support;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.MBeanServerNotFoundException;

/**
 * FactoryBean that obtains an <code>MBeanServer</code> instance
 * through the standard JMX 1.2 <code>MBeanServerFactory</code> API
 * (which is available on JDK 1.5 or as part of a JMX 1.2 provider).
 *
 * <p>Exposes the <code>MBeanServer</code> for bean references.
 *
 * <p>By default, <code>MBeanServerFactoryBean</code> will always create
 * a new <code>MBeanServer</code> even if one is already running. To have
 * the <code>MBeanServerFactoryBean</code> attempt to locate a running
 * <code>MBeanServer</code> first, set <code>locateExistingServerIfPossible</code>
 * to <code>true</code>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setLocateExistingServerIfPossible(boolean)
 * @see javax.management.MBeanServerFactory#createMBeanServer
 * @see javax.management.MBeanServerFactory#newMBeanServer
 * @see MBeanServerConnectionFactoryBean
 * @see ConnectorServerFactoryBean
 */
public class MBeanServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	/**
	 * Should the <code>MBeanServerFactoryBean</code> instruct the <code>MBeanServerFactory</code>
	 * to maintain an internal reference to the <code>MBeanServer</code>.
	 */
	private boolean registerWithFactory = true;

	/**
	 * The default domain used by the <code>MBeanServer</code>.
	 */
	private String defaultDomain;

	/**
	 * The <code>MBeanServer</code> to expose.
	 */
	private MBeanServer server;

	/**
	 * Flag indicating whether or not the <code>MBeanServerFactoryBean</code> should attempt to locate
	 * an existing <code>MBeanServer</code> before creating one.
	 */
	private boolean locateExistingServerIfPossible;


	/**
	 * Setting this value to true will cause the <code>MBeanServer</code> to be created with a call
	 * to <code>MBeanServerFactory.createMBeanServer()</code>, and thus it will be possible to
	 * retrieve a reference to the MBeanServer using <code>MBeanServerFactory.findMBeanServer()<code>.
	 * @see javax.management.MBeanServerFactory#createMBeanServer
	 * @see javax.management.MBeanServerFactory#findMBeanServer
	 */
	public void setRegisterWithFactory(boolean registerWithFactory) {
		this.registerWithFactory = registerWithFactory;
	}

	/**
	 * Set the default domain to be used by the <code>MBeanServer</code>,
	 * to be passed to <code>MBeanServerFactory.createMBeanServer()</code>
	 * or <code>MBeanServerFactory.findMBeanServer()<code>.
	 * <p>Default is none.
	 * @see javax.management.MBeanServerFactory#createMBeanServer(String)
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	/**
	 * Sets the value of the <code>locateExistingServerIfPossible</code> flag, indicating whether or
	 * not the <code>MBeanServerFactoryBean</code> should attempt to locate a running <code>MBeanServer</code>
	 * before creating one.
	 */
	public void setLocateExistingServerIfPossible(boolean locateExistingServerIfPossible) {
		this.locateExistingServerIfPossible = locateExistingServerIfPossible;
	}

	/**
	 * Creates the <code>MBeanServer</code> instance.
	 */
	public void afterPropertiesSet() {

		if(this.locateExistingServerIfPossible) {
			this.server = locateMBeanServer();
			if(this.server != null) return;
		}

		if (this.registerWithFactory) {
			// Create an MBeanServer instance that is accessible
			// using MBeanServerFactory.findMBeanServer().
			if (this.defaultDomain != null) {
				this.server = MBeanServerFactory.createMBeanServer(this.defaultDomain);
			}
			else {
				this.server = MBeanServerFactory.createMBeanServer();
			}
		}
		else {
			// Create an MBeanServer instance that is not accessible
			// using MBeanServerFactory.findMBeanServer().
			if (this.defaultDomain != null) {
				this.server = MBeanServerFactory.newMBeanServer(this.defaultDomain);
			}
			else {
				this.server = MBeanServerFactory.newMBeanServer();
			}
		}
	}

	/**
	 * Attempts to an existing <code>MBeanServer</code>. Called if <code>locateExistingServerIfPossible</code> is set to
	 * <code>true</code>. Default implementation attempts to find an <code>MBeanServer</code> using a standard lookup.
	 * Sub-classes may override to additional location logic.
	 * @see #setLocateExistingServerIfPossible(boolean)
	 * @see org.springframework.jmx.support.JmxUtils#locateMBeanServer()
	 */
	protected MBeanServer locateMBeanServer() {
		try {
			return JmxUtils.locateMBeanServer();
		} catch(MBeanServerNotFoundException ex) {
			return null;
		}
	}

	public Object getObject() {
		return this.server;
	}

	public Class getObjectType() {
		return (this.server != null ? this.server.getClass() : MBeanServer.class);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Unregisters the <code>MBeanServer</code> instance, if necessary.
	 */
	public void destroy() {
		if (this.registerWithFactory) {
			MBeanServerFactory.releaseMBeanServer(this.server);
		}
	}

}
