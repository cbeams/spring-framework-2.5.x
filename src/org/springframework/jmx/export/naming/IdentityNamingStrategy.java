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

package org.springframework.jmx.export.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * An implementation of the <code>ObjectNamingStrategy</code> interface that
 * creates a name based on the the identity of a given instance.
 *
 * <p>The resulting <code>ObjectName</code> will be in the form
 * <i>package</i>:class=<i>class name</i>,hashCode=<i>identity hash (in hex)</i>
 *
 * @author Rob Harrop
 * @since 1.2
 */
public class IdentityNamingStrategy implements ObjectNamingStrategy {

	/**
	 * Returns an instance of <code>ObjectName</code> based on the identity
	 * of the managed resource.
	 */
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		StringBuffer sb = new StringBuffer(256);
		sb.append(managedBean.getClass().getPackage().getName());
		sb.append(":class=");
		sb.append(ClassUtils.getShortName(managedBean.getClass()));
		sb.append(",hashCode=");
		sb.append(ObjectUtils.getIdentityHexString(managedBean));
		return ObjectNameManager.getInstance(sb.toString());
	}

}
