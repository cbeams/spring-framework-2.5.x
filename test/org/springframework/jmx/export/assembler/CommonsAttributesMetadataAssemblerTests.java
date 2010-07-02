/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jmx.export.assembler;

import org.springframework.jmx.export.JmxTestUtils;
import org.springframework.jmx.export.metadata.AttributesJmxAttributeSource;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * @author Rob Harrop
 */
public class CommonsAttributesMetadataAssemblerTests extends AbstractMetadataAssemblerTests {

	static {
		JmxTestUtils.compileCommonsAttributesIfNecessary();
	}

	private static final String OBJECT_NAME = "bean:name=testBean3";

	protected JmxAttributeSource getAttributeSource() {
		return new AttributesJmxAttributeSource(new CommonsAttributes());
	}

	protected String getObjectName() {
		return OBJECT_NAME;
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/export/assembler/metadataAssembler.xml";
	}



}
