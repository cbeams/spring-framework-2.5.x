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

package org.springframework.jmx.registration;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Default implementation of <code>RegistrationStrategy</code> that simply re-routes
 * calls directly to the <code>MBeanServer</code> without performing any additional
 * processing.
 *
 * @author Rob Harrop
 */
public class DefaultRegistrationStrategy implements MBeanServerAwareRegistrationStrategy {

	/**
	 * The <code>MBeanServer</code> to register the bean with.
	 */
	private MBeanServer server;

	/**
	 * Sets the <code>MBeanServer</code>.
	 *
	 * @param server the <code>MBeanServer</code>.
	 */
	public void setMBeanServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Registers the supplied MBean with the <code>MBeanServer</code> using the supplied
	 * <code>ObjectName</code>.
	 *
	 * @param mbean the resource to register.
	 * @param objectName the <code>ObjectName</code> of the managed resource.
	 * @throws JMException if an error occurs during registration.
	 */
	public void registerMBean(Object mbean, ObjectName objectName) throws JMException {
		server.registerMBean(mbean, objectName);
	}

	/**
	 * Unregisters the MBean corresponding to the supplied <code>ObjectName</code>.
	 *
	 * @param objectName the <code>ObjectName</code> of the managed resource.
	 * @throws JMException if an error occurs during registration.
	 */
	public void unregisterMBean(ObjectName objectName) throws JMException {
		server.unregisterMBean(objectName);
	}
}
