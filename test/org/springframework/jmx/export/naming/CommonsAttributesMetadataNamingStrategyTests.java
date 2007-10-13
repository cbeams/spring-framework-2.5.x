/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.export.naming;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.jmx.IJmxTestBean;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.export.JmxTestUtils;
import org.springframework.jmx.export.metadata.AttributesJmxAttributeSource;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class CommonsAttributesMetadataNamingStrategyTests extends AbstractNamingStrategyTests {

	private static final String OBJECT_NAME = "spring:bean=test";

	static {
		JmxTestUtils.compileCommonsAttributesIfNecessary();
	}
	
	protected ObjectNamingStrategy getStrategy() throws Exception {
		MetadataNamingStrategy mns = new MetadataNamingStrategy();
		mns.setAttributeSource(new AttributesJmxAttributeSource(new CommonsAttributes()));
		return mns;
	}

	public void testWithJdkProxy() throws Exception {
		IJmxTestBean proxy =
				(IJmxTestBean) ProxyFactory.getProxy(IJmxTestBean.class, new SingletonTargetSource(new JmxTestBean()));
		assertEquals(ObjectNameManager.getInstance("spring:bean=test"), getStrategy().getObjectName(proxy, "foo"));
	}

	protected Object getManagedResource() throws Exception {
		return new JmxTestBean();
	}

	protected String getKey() {
		return "foo";
	}

	protected String getCorrectObjectName() {
		return OBJECT_NAME;
	}

}
