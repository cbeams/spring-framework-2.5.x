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

package org.springframework.jmx.util;

import javax.management.JMException;

/**
 * Thrown when <code>JmxUtils</code> cannot locate an instance of <code>MBeanServer</code>
 * running locally, or when more than one instance is found.
 *
 * @author Rob Harrop
 * @see JmxUtils#locateMBeanServer()
 */
public class MBeanServerNotFoundException extends JMException {

	/**
	 * Create a new <code>MBeanServerNotFoundException</code> with the
	 * supplied error message.
	 * @param msg the error message.
	 */
	public MBeanServerNotFoundException(String msg) {
		super(msg);
	}

}
