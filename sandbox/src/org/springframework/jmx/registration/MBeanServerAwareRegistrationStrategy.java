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

import javax.management.MBeanServer;

/**
 * Extends the <code>RegistrationStrategy</code> interface to enable
 * the <code>MBeanExporter</code> to inject a reference to the <code>MBeanServer</code>.
 * Some implementations of <code>RegistrationStrategy</code> may not require a reference
 * to the <code>MBeanServer</code>.
 *
 * @author Rob Harrop
 */
public interface MBeanServerAwareRegistrationStrategy extends RegistrationStrategy {

	/**
	 * Sets the <code>MBeanServer</code> used by the implementation.
	 * Called by the <code>MBeanExporter</code>.
	 *
	 * @param server the <code>MBeanServer</code>.
	 */
	void setMBeanServer(MBeanServer server);
}
