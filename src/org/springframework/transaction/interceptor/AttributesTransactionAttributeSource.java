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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.metadata.Attributes;

/**
 * Implementation of TransactionAttributeSource that uses
 * attributes from an Attributes implementation.
 *
 * @author Rod Johnson
 * @see org.springframework.metadata.Attributes
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource
 */
public class AttributesTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource {
	
	/**
	 * Underlying Attributes implementation we're using
	 */
	private final Attributes attributes;

	public AttributesTransactionAttributeSource(Attributes attributes) {
		this.attributes = attributes;
	}


	/**
	 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#findAllAttributes(java.lang.Class)
	 */
	protected Collection findAllAttributes(Class clazz) {
		return attributes.getAttributes(clazz);
	}
	/**
	 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#findAllAttributes(java.lang.reflect.Method)
	 */
	protected Collection findAllAttributes(Method m) {
		return attributes.getAttributes(m);
	}
}
