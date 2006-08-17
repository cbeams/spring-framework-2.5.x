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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.metadata.Attributes;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Implementation of the <code>TransactionAttributeSource</code> interface that
 * reads metadata via Spring's <code>Attributes</code> abstraction.
 *
 * <p>Typically used for reading in source-level attributes via
 * Commons Attributes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.metadata.Attributes
 * @see org.springframework.metadata.commons.CommonsAttributes
 */
public class AttributesTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource
		implements InitializingBean {
	
	/**
	 * Underlying Attributes implementation that we're using.
	 */
	private Attributes attributes;


	/**
	 * Create a new AttributesTransactionAttributeSource.
	 * @see #setAttributes
	 */
	public AttributesTransactionAttributeSource() {
	}

	/**
	 * Create a new AttributesTransactionAttributeSource.
	 * @param attributes the Attributes implementation to use
	 * @see org.springframework.metadata.commons.CommonsAttributes
	 */
	public AttributesTransactionAttributeSource(Attributes attributes) {
		if (attributes == null) {
			throw new IllegalArgumentException("Attributes is required");
		}
		this.attributes = attributes;
	}

	/**
	 * Set the Attributes implementation to use.
	 * @see org.springframework.metadata.commons.CommonsAttributes
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public void afterPropertiesSet() {
		if (this.attributes == null) {
			throw new IllegalArgumentException("'attributes' is required");
		}
	}


	protected Collection findAllAttributes(Class clazz) {
		Assert.notNull(this.attributes, "'attributes' is required");
		return this.attributes.getAttributes(clazz);
	}

	protected Collection findAllAttributes(Method method) {
		Assert.notNull(this.attributes, "'attributes' is required");
		return this.attributes.getAttributes(method);
	}


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AttributesTransactionAttributeSource)) {
			return false;
		}
		AttributesTransactionAttributeSource otherTas = (AttributesTransactionAttributeSource) other;
		return ObjectUtils.nullSafeEquals(this.attributes, otherTas.attributes);
	}

	public int hashCode() {
		return AttributesTransactionAttributeSource.class.hashCode();
	}

}
