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
 * @author Rob Harrop
 */
public abstract class AbstractAdapterHost
		implements AdapterHost, InitializingBean, DisposableBean {

	private MBeanServer server;

	private ObjectName objectName;

	private boolean startAutomatically = true;


	public void setServer(MBeanServer server) {
		this.server = server;
	}

	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	public void setStartAutomatically(boolean startAutomatically) {
		this.startAutomatically = startAutomatically;
	}


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

	private void registerAdapter() throws JMException {
		// register the adapter with the mbean server
		this.server.registerMBean(getAdapterMBean(), this.objectName);
	}

	/**
	 * Implementation can override to perform some additional initialization.
	 */
	protected void initAdapterHost() {
	}

	protected abstract Object getAdapterMBean();

	public void destroy() {
		stop();
	}

}
