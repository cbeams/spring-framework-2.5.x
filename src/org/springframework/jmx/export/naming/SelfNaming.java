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

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

/**
 * Allows infrastructure components to provide their own <code>ObjectName</code>s to the
 * <code>MBeanExporter</code>.
 * <p/>
 * <stong>Note:</strong> this interface is intended for internal usage only.
 *
 * @author Rob Harrop
 * @see org.springframework.jmx.export.MBeanExporter
 * @since 1.2.2
 */
public interface SelfNaming {

	/**
	 * Gets the <code>ObjectName</code> for the implementing object.
	 */
	ObjectName getObjectName() throws MalformedObjectNameException;
}
