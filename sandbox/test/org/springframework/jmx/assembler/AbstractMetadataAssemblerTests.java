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

package org.springframework.jmx.assembler;

import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.springframework.jmx.metadata.JmxAttributeSource;

/**
 * @author Rob Harrop
 */
public abstract class AbstractMetadataAssemblerTests extends AbstractJmxAssemblerTests {

	public void testDescription() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		assertEquals("The descriptions are not the same", "My Managed Bean",
				info.getDescription());
	}

	public void testAttributeDescriptionOnSetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = inf.getAttribute("age");

		assertEquals("The description for the age attribute is incorrect",
				"The Age Attribute", attr.getDescription());
	}

	public void testAttributeDescriptionOnGetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = inf.getAttribute("name");

		assertEquals("The description for the name attribute is incorrect",
				"The Name Attribute", attr.getDescription());
	}

	/**
	 * Tests the situation where the attribute is only defined on the getter.
	 */
	public void testReadOnlyAttribute() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = inf.getAttribute("age");

		assertFalse("The age attribute should not be writable",
				attr.isWritable());
	}

	public void testReadWriteAttribute() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = inf.getAttribute("name");

		assertTrue("The name attribute should be writable",
				attr.isWritable());
		assertTrue("The name attribute should be readable",
				attr.isReadable());
	}

	/**
	 * Tests the situation where the property only has
	 * a getter
	 * @throws java.lang.Exception
	 */
	public void testWithOnlyGetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = inf.getAttribute("nickName");

		assertNotNull("Attribute should not be null", attr);
	}

	/**
	 * Tests the situation where the property only
	 * has a setter
	 * @throws java.lang.Exception
	 */
	public void testWithOnlySetter() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = info.getAttribute("superman");

		assertNotNull("Attribute should not be null", attr);
	}

	public void testManagedResourceDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		Descriptor desc = info.getMBeanDescriptor();

		assertEquals("Logging should be set to true", "true", desc.getFieldValue("log"));
		assertEquals("Log file should be jmx.log", "jmx.log", desc.getFieldValue("logFile"));
		assertEquals("Currency Time Limit should be 15", "15", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
		assertEquals("Persist Period should be 200", "200", desc.getFieldValue("persistPeriod"));
		assertEquals("Persist Location should be foo", "./foo", desc.getFieldValue("persistLocation"));
		assertEquals("Persist Name should be bar", "bar.jmx", desc.getFieldValue("persistName"));
	}

	public void testAttributeDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		Descriptor desc = info.getAttribute("name").getDescriptor();

		assertEquals("Default value should be foo", "foo", desc.getFieldValue("default"));
		assertEquals("Currency Time Limit should be 20", "20", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
		assertEquals("Persist Period should be 300", "300", desc.getFieldValue("persistPeriod"));
	}

	public void testOperationDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		Descriptor desc = info.getOperation("myOperation").getDescriptor();

		assertEquals("Currency Time Limit should be 30", "30", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Role should be \"operation\"", "operation", desc.getFieldValue("role"));
	}

	protected abstract String getObjectName();

	protected int getExpectedAttributeCount() {
		return 4;
	}

	protected int getExpectedOperationCount() {
		return 7;
	}

	protected ModelMBeanInfoAssembler getAssembler() {
		MetadataModelMBeanInfoAssembler assembler = new MetadataModelMBeanInfoAssembler();
		assembler.setAttributeSource(getAttributeSource());
		return assembler;
	}

	protected abstract JmxAttributeSource getAttributeSource();

}
