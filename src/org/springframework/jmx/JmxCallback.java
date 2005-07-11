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

package org.springframework.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;

/**
 * A callback interface for encapsulating an operation against a JMX <code>MBeanServer</code>.
 * 
 * @author Rob Harrop
 */
public interface JmxCallback {

	/**
	 * Execute this callback on the supplied <code>MBeanServer</code>.
	 * @throws JMException A JMX management exception occured
	 */
	public Object doWithMBeanServer(MBeanServer server) throws JMException;
}
