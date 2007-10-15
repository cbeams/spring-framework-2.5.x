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

package org.springframework.beans.factory.support;

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


	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type name whose attributes also match against the provided
	 * map of attributes.
	 * <p>The type name may match the fully-qualified class name of
	 * the annotation or the short class name (without the package).
	 */
	public AutowireCandidateQualifier(String typeName, Map attributes) {
		this.typeName = typeName;
		this.attributes = attributes;
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type name whose <code>value</code> attribute also matches
	 * the specified value.
	 * <p>The type name may match the fully-qualified class name of
	 * the annotation or the short class name (without the package).
	 */
	public AutowireCandidateQualifier(String typeName, Object value) {
		this.typeName = typeName;
		this.setAttribute("value", value);
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type whose <code>value</code> attribute also matches
	 * the specified value.
	 */
	public AutowireCandidateQualifier(Class type, Object value) {
		this(type.getName(), value);
		this.type = type;
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type name.
	 * <p>The type name may match the fully-qualified class name of
	 * the annotation or the short class name (without the package).
	 */
	public AutowireCandidateQualifier(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type.
	 */
	public AutowireCandidateQualifier(Class type) {
		this.type = type;
		this.typeName = type.getName();
	}

	/**
	 * Retrieve the type name. This value will be the same as the
	 * type name provided to the constructor or the fully-qualified
	 * class name if a Class instance was provided to the constructor.
	 */
	public String getTypeName() {
		return this.typeName;
	}

	/**
	 * Retrieve the type. May be <code>null</code> if a type name
	 * was provided to the constructor instead of an actual Class.
	 */
	public Class getType() {
		return this.type;
	}

	/**
	 * Retrieve the attribute value for the given name.
	 */
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	/**
	 * Set the attribute value for the given name. This value
	 * will be matched against any corresponding attribute
	 * value of a qualifier annotation.
	 */
	public Object setAttribute(String name, Object value) {
		return this.attributes.put(name, value);
	}

}
