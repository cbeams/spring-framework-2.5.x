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

package org.springframework.jmx.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Wraps the creation of <code>ObjectName</code> instances and caches instances
 * for reuse. Provides <code>ObjectName</code> caching to older JMX clients.
 * @author Rob Harrop
 * @since 1.2
 */
public class ObjectNameManager {

	/**
	 * Cache for the <code>ObjectName</code> instance.
	 */
	private static Map objectNameCache = Collections.synchronizedMap(new HashMap());

	/**
	 * Retreive the <code>ObjectName</code> instance corresponding to the supplied
	 * name.
	 * @param objectName the <code>ObjectName</code> in <code>String</code> format.
	 * @return the <code>ObjectName</code> instance.
	 * @throws MalformedObjectNameException if the supplied name is invalid.
	 */
	public static ObjectName getInstance(String objectName) throws MalformedObjectNameException {
		ObjectName name = (ObjectName) objectNameCache.get(objectName);
		if (name == null) {
			name = new ObjectName(objectName);
			objectNameCache.put(objectName, name);
		}
		return name;
	}

}
