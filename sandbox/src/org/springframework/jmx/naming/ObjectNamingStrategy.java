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

package org.springframework.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Strategy interface that encapsulates the creation of <code>ObjectName</code> instances.
 * Used by the <code>MBeanExporter</code> to obtain <code>ObjectName</code>s when registering
 * beans.
 *
 * @author Rob Harrop
 * @see org.springframework.jmx.MBeanExporter
 * @see javax.management.ObjectName
 */
public interface ObjectNamingStrategy {

	/**
	 * Obtains an <code>ObjectName</code> for the supplied bean.
	 *
	 * @param managedResource the bean that will be exposed under the returned <code>ObjectName</code>.
	 * @param key the key associated with this bean in the <code>beans</code>
	 * <code>Map</code> passed to the <code>MBeanExporter</code>.
	 * @return the <code>ObjectName</code> instance.
	 * @throws MalformedObjectNameException if the resulting <code>ObjectName</code> is invalid.
	 */
	public ObjectName getObjectName(Object managedResource, String key) throws MalformedObjectNameException;

}
