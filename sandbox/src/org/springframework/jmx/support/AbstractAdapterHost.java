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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.jmx.support.ObjectNameManager;

/**
 * Convenient base class for <code>AdapterHost</code> implementations.
 *
 * <p>Allows for adapters to be started automatically when running inside
 * an <code>ApplicationContext</code> plus encapsulates the logic to
 * register the adapter itself with the <code>MBeanServer</code>.
 *
 * @author Rob Harrop
 * @since 1.2
 */
public abstract class AbstractAdapterHost
		implements AdapterHost, InitializingBean, DisposableBean {

	/**
	 * the <code>MBeanServer</code> to register the adapter with
	 */
	private MBeanServer server;

	/**
	 * the <code>ObjectName</code> to register the adapter under
	 */
	private ObjectName objectName;

	/**
	 * indicates whether the adapter should be started automatically
	 */
	private boolean startAutomatically = true;


  /**
	 * Set the <code>MBeanServer</code> to register the adapter with.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Set the <code>ObjectName</code> used to register the adapter.
	 * @throws MalformedObjectNameException if the <code>ObjectName</code> is malformed
	 */
	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * Set whether the adapter should be started automatically when
	 * deployed in an <code>ApplicationContext</code>.
	 */
	public void setStartAutomatically(boolean startAutomatically) {
		this.startAutomatically = startAutomatically;
	}


  /**
	 * Fired by Spring. Attempts to locate an <code>MBeanServer</code> instance if none
	 * is supplied. Will also register the adapter with the <code>MBeanServer</code> if an
	 * <code>ObjectName</code> is supplied. If <code>startAutomatically</code> is
	 * <code>true</code>, the adapter will be started.
	 * @throws JMException if the adapter cannot be registered with the <code>MBeanServer</code>
	 */
	public void afterPropertiesSet() throws JMException {
		if (this.objectName != null && this.server == null) {
			// Attempt to locate MBeanServer.
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
	 * @throws JMException if the adapter cannot be registered
	 */
	private void registerAdapter() throws JMException {
		// Register the adapter with the MBean server.
		this.server.registerMBean(getAdapterMBean(), this.objectName);
	}

	/**
	 * A subclass can override this method to perform some additional initialization.
	 */
	protected void initAdapterHost() {
	}

	/**
	 * A subclass must implement this method return the adapter MBean.
	 */
	protected abstract Object getAdapterMBean();


	/**
	 * Stop the adapter.
	 * Automatically called on <code>ApplicationContext</code> shutdown.
	 */
	public void destroy() {
		stop();
	}

}
