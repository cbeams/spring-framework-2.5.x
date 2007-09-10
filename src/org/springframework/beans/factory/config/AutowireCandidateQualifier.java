/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Qualifier for resolving autowire candidates. A bean definition that
 * includes one or more such qualifiers enables fine-grained matching
 * against annotations on a field or parameter to be autowired.
 * 
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.beans.factory.annotation.Qualifier
 */
public class AutowireCandidateQualifier {

	private String typeName;

	private Class type;

	private Map attributes = new HashMap();


	public AutowireCandidateQualifier(String typeName, Map attributes) {
		this.typeName = typeName;
		this.attributes = attributes;
	}

	public AutowireCandidateQualifier(String typeName, Object value) {
		this.typeName = typeName;
		this.setAttribute("value", value);
	}

	public AutowireCandidateQualifier(Class type, Object value) {
		this(type.getClass().getName(), value);
		this.type = type;
	}

	public AutowireCandidateQualifier(String typeName) {
		this.typeName = typeName;
	}

	public AutowireCandidateQualifier(Class type) {
		this.type = type;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public Class getType() {
		return this.type;
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Object setAttribute(String name, Object value) {
		return this.attributes.put(name, value);
	}

}
