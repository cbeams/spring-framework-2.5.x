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
import javax.management.ObjectName;

/**
 * Strategy interface for decoupling MBean registration from the <code>MBeanExporter</code>.
 * Implementations may choose to perform some kind of pre- or post- processing when registering
 * the MBean.
 *
 * @author Rob Harrop
 */
public interface RegistrationStrategy {

	/**
	 * Called by the <code>MBeanExporter</code> to register an MBean with
	 * the supplied <code>ObjectName</code>.
	 *
	 * @param mbean the resource to register.
	 * @param objectName the <code>ObjectName</code> of the managed resource.
	 * @throws JMException if an error occurs during registration.
	 */
	void registerMBean(Object mbean, ObjectName objectName) throws JMException;

	/**
	 * Called by <code>MBeanExporter</code> to unregister the MBean with the
	 * supplied <code>ObjectName</code>.
	 *
	 * @param objectName the <code>ObjectName</code> of the managed resource.
	 * @throws JMException if an error occurs during registration.
	 */
	void unregisterMBean(ObjectName objectName) throws JMException;
}
