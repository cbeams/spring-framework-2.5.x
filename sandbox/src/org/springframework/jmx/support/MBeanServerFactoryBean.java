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

package org.springframework.jmx.support;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <code>FactoryBean</code> implementation to create and obtain an <code>MBeanServer</code> instance.
 *
 * @author Rob Harrop
 * @since 1.2
 */
public class MBeanServerFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * Should the <code>MBeanServerFactoryBean</code> instruct the <code>MBeanServerFactory</code> to
	 * maintain an internal reference to the <code>MBeanServer</code>.
	 */
	private boolean haveFactoryHoldReference = true;

	/**
	 * Hold the <code>MBeanServer</code>.
	 */
	private MBeanServer server = null;

	/**
	 * The default domain used by the <code>MBeanServer</code>
	 */
	private String defaultDomain = null;

	/**
	 * Returns the <code>MBeanServer</code> instance
	 *
	 * @return The <code>MBeanServer</code> instance
	 */
	public Object getObject() throws Exception {
		return this.server;
	}

	/**
	 * Returns the default domain used by the <code>MBeanServer</code>
	 *
	 * @return the default domain for the <code>MBeanServer</code>.
	 */
	public String getDefaultDomain() {
		return defaultDomain;
	}

	/**
	 * Set the default domain to be used by the <code>MBeanServer</code>.
	 * Must be set before the <code>MBeanServer</code> is created, that is before
	 * <code>afterPropertiesSet()</code> is called.
	 *
	 * @param defaultDomain The domain name to use
	 */
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	/**
	 * Indicates whether the <code>MBeanServerFactory</code>. was instruced to maintain
	 * a reference to the <code>MBeanServer</code> after creation.
	 *
	 * @return <code>true</code> if the <code>MBeanServerFactory</code> has the reference otherwise <code>false</code>.
	 */
	public boolean getHaveFactoryHoldReference() {
		return haveFactoryHoldReference;
	}

	/**
	 * Setting this value to true will cause the <code>MBeanServer</code> to be created with a call
	 * to <code>MBeanServerFactory.createMBeanServer()</code>, and thus it will be possible to
	 * retreive a reference to the MBeanServer using <code>MBeanServerFactory.findMBeanServer()<code>.
	 */
	public void setHaveFactoryHoldReference(boolean haveFactoryHoldReference) {
		this.haveFactoryHoldReference = haveFactoryHoldReference;
	}

	/**
	 * Convenience method to retreive the <code>MBeanServer</code>
	 * without the need to cast.
	 */
	public MBeanServer getServer() {
		return server;
	}

	/**
	 * Indicates the type of <code>Object</code> returned by this factory bean.
	 *
	 * @return Always <code>MBeanServer</code>.
	 */
	public Class getObjectType() {
		return MBeanServer.class;
	}

	/**
	 * Indicates that the <code>MBeanServer</code> returned by this method is a singleton.
	 *
	 * @return Always <code>true</code>.
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Creates the <code>MBeanServer</code> instance
	 */
	public void afterPropertiesSet() throws Exception {
		if (haveFactoryHoldReference) {
			// create an MBeanServer instance that is accessible
			// using MBeanServerFactory.findMBeanServer
			if (defaultDomain != null) {
				server = MBeanServerFactory.createMBeanServer(defaultDomain);
			}
			else {
				server = MBeanServerFactory.createMBeanServer();
			}
		}
		else {
			// create an MBeanServer instance that is not accessible
			// using MBeanServerFactory.findMBeanServer()
			if (defaultDomain != null) {
				server = MBeanServerFactory.newMBeanServer(defaultDomain);
			}
			else {
				server = MBeanServerFactory.newMBeanServer();
			}
		}
	}

}