/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx;

import javax.management.modelmbean.ModelMBean;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public abstract class AbstractModelMBeanProviderTests extends TestCase {

	protected abstract Class getImplementationClass();

	protected abstract ModelMBeanProvider getProvider();

	public void testCorrectImplementationClass() throws Exception {
		ModelMBean bean = getProvider().getModelMBean();
		assertTrue(bean.getClass() == getImplementationClass());
	}

	public void testInstancesAreDifferent() throws Exception {
		ModelMBean one = getProvider().getModelMBean();
		ModelMBean two = getProvider().getModelMBean();
		assertTrue(one != two);
	}
	
}
