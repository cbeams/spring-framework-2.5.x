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

package org.springframework.jmx.support.adapter;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * Convenient base class for <code>AdapterHost</code> implementations.
 * Allows for adapters to be started automatically when running inside
 * an <code>ApplicationContext</code> plus encapsulates the logic to
 * register the adapter itself with the <code>MBeanServer</code>.
 * @author Rob Harrop
 */
public abstract class AbstractAdapterHost
		implements AdapterHost, InitializingBean, DisposableBean {

	/**
	 * The <code>MBeanServer</code> to register the adapter with.
	 */
	private MBeanServer server;

	/**
	 * The <code>ObjectName</code> to register the adapter under.
	 */
	private ObjectName objectName;

	/**
	 * Indicates whether the adapter should be started automatically.
	 */
	private boolean startAutomatically = true;

  /**
	 * Sets the <code>MBeanServer</code>.
	 * @param server an <code>MBeanServer</code>.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Sets the <code>ObjectName</code> used to register the adapter.
	 * @param objectName the <code>String</code> representation of the <code>ObjectName</code>.
	 * @throws MalformedObjectNameException if the <code>ObjectName</code> is malformed.
	 */
	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * Sets the flag indicating whether the adapter should be started automatically when
	 * deployed in an <code>ApplicationContext</code>.
	 * @param startAutomatically <code>true</code> to start the adapter automatically.
	 */
	public void setStartAutomatically(boolean startAutomatically) {
		this.startAutomatically = startAutomatically;
	}

  /**
	 * Fired by Spring. Attempts to locate an <code>MBeanServer</code> instance if none
	 * is supplied. Will also register the adapter with the <code>MBeanServer</code> if an
	 * <code>ObjectName</code> is supplied. If <code>startAutomatically</code> is <code>true</code> the
	 * adapter will be started.
	 * @throws JMException if the adapter cannot be registered with the <code>MBeanServer</code>.
	 */
	public void afterPropertiesSet() throws JMException {
		if (this.objectName != null && this.server == null) {
			// attempt to locate MBeanServer
			this.server = JmxUtils.locateMBeanServer();
		}

		initAdapterHost();

		if (this.objectName != null) {
			registerAdapter();
		}

		if (this.startAutomatically) {
			start();
		}
	}

	/**
	 * Registers the adapter with the <code>MBeanServer</code>.
	 * @throws JMException if the adapter cannot be registered.
	 */
	private void registerAdapter() throws JMException {
		// register the adapter with the mbean server
		this.server.registerMBean(getAdapterMBean(), this.objectName);
	}

	/**
	 * Implementation can override to perform some additional initialization.
	 */
	protected void initAdapterHost() {
	}

	/**
	 * Implementation must override to return the adapter MBean.
	 * @return the adapter MBean.
	 */
	protected abstract Object getAdapterMBean();

	/**
	 * Stops the adapter.
	 */
	public void destroy() {
		stop();
	}

}
