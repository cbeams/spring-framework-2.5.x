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

package org.springframework.jmx;

import javax.management.MBeanException;
import javax.management.modelmbean.ModelMBean;

/**
 * Implementations of this interface are used by the
 * <code>MBeanExporter</code> class to obtain concrete implementations of
 * the <code>ModelMBean</code> interface.
 *
 * @author Rob Harrop
 */
public interface ModelMBeanProvider {

	/**
	 * Return an instance of a class that implements <code>ModelMBean</code>. This
	 * method is called by the <code>MBeanExporter</code> to obtain a <code>ModelMBean</code>
	 * instance to use when registering a bean. This method is called once per bean during the
	 * registration phase and each invocation MUST return a new instance of <code>ModelMBean</code>
	 *
	 * @return an instance of a class that implements <code>ModelMBean</code>.
	 * @throws MBeanException indicating an error occured whilst creating an instance of
	 *                        the <code>ModelMBean</code> implementation class.
	 */
	ModelMBean getModelMBean() throws MBeanException;
}
