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

package org.springframework.jmx.export;

import javax.management.ObjectName;

/**
 * Listener to be registered with an <code>MBeanExporter</code>.
 * Allows application code to be notified when an MBean is registered and unregistered.
 *
 * @author Rob Harrop
 * @since 1.2.2
 * @see org.springframework.jmx.export.MBeanExporter#setListeners
 */
public interface MBeanExporterListener {

	/**
	 * Called by <code>MBeanExporter</code> after an MBean has been registered with the
	 * <code>MBeanServer</code>.
	 * @param objectName the <code>ObjectName</code> of the registered MBean
	 */
	void mbeanRegistered(ObjectName objectName);

	/**
	 * Called by <code>MBeanExporter</code> after an MBean has been unregistered from the
	 * <code>MBeanServer</code>.
	 * @param objectName the <code>ObjectName</code> of the unregistered MBean
	 */
	void mbeanUnregistered(ObjectName objectName);

}
