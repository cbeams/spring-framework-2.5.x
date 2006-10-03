/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jmx.export;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

/**
 * @author Rob Harrop
 */
public class TestDynamicMBean implements DynamicMBean {

	public void setFailOnInit(boolean failOnInit) {
		if (failOnInit) {
			throw new IllegalArgumentException("Failing on initialization");
		}
	}

	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException {

		if ("name".equals(attribute)) {
			return "Rob Harrop";
		}
		else {
			return null;
		}
	}

	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
	}

	public AttributeList getAttributes(String[] arg0) {
		return null;
	}

	public AttributeList setAttributes(AttributeList arg0) {
		return null;
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo attr =
				new MBeanAttributeInfo("name", "java.lang.String", "", true, false, false);

		MBeanInfo inf = new MBeanInfo(
				TestDynamicMBean.class.getName(), "",
				new MBeanAttributeInfo[]{attr},
				new MBeanConstructorInfo[0],
				new MBeanOperationInfo[0],
				new MBeanNotificationInfo[0]);

		return inf;
	}


	public boolean equals(Object obj) {
		return (obj instanceof TestDynamicMBean);
	}

	public int hashCode() {
		return TestDynamicMBean.class.hashCode();
	}

}
