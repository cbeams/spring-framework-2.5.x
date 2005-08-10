/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jmx.export.naming;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.jmx.export.metadata.AttributesJmxAttributeSource;
import org.springframework.metadata.commons.CommonsAttributes;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

/**
 * @author Rob Harrop
 */
public class MetadataNamingStrategyTests extends AbstractNamingStrategyTests {

	private static final String OBJECT_NAME = "spring:bean=test";

	protected ObjectNamingStrategy getStrategy() throws Exception {
		MetadataNamingStrategy mns = new MetadataNamingStrategy();
		mns.setAttributeSource(new AttributesJmxAttributeSource(new CommonsAttributes()));
		return mns;
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

	public void testWithLazyInit() throws InstanceNotFoundException, MalformedObjectNameException {
		ConfigurableApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("org/springframework/jmx/export/naming/lazyNaming.xml");
			MBeanServer server = (MBeanServer)ctx.getBean("server");
			ObjectInstance objectInstance = server.getObjectInstance(ObjectNameManager.getInstance("bean:name=testBean4"));
			assertNotNull(objectInstance);
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

}
