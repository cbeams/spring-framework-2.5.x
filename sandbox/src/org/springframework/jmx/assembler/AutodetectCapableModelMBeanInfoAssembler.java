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

package org.springframework.jmx.assembler;


/**
 * Extends the <code>ModelMBeanInfoAssembler</code> to add autodetection logic.
 * Implementations of this interface are given the opportunity by the
 * <code>MBeanExporter</code> to include additional beans in the registration process.
 * The exact mechanism for deciding which beans to include is left to implementing classes.
 *
 * @author Rob Harrop
 * @see org.springframework.jmx.MBeanExporter#autodetectBeans()
 */
public interface AutodetectCapableModelMBeanInfoAssembler extends
		ModelMBeanInfoAssembler {

	/**
	 * Indicates whether a particular bean should be included in the registration
	 * process, if it is not specified in the <code>beans</code> <code>Map</code> of
	 * the <code>MBeanExporter</code>.
	 *
	 * @param beanName the key associated with the MBean in the <code>beans</code> <code>Map</code>
	 * of the <code>MBeanExporter</code>.
	 * @param beanClass the <code>Class</code> of the MBean.
	 * @return <code>true</code> if the bean should be included in the registration process,
	 * otherwise <code>false</code>.
	 */
	public boolean includeBean(String beanName, Class beanClass);
}
