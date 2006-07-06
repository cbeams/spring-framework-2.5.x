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

package org.springframework.validation;

import java.io.Serializable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.util.Assert;

/**
 * Default implementation of the Errors and BindingResult interfaces,
 * for registration and evaluation of binding errors on JavaBean objects.
 * Performs standard JavaBean property access, also supporting nested properties.
 *
 * <p>Normally, application code will work with the Errors interface
 * or the BindingResult interface. A DataBinder returns its BindingResult
 * via <code>getBindingResult()</code>.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder#getBindingResult()
 * @see DataBinder#initBeanPropertyAccess()
 * @see DirectFieldBindingResult
 */
public class BeanPropertyBindingResult extends AbstractPropertyBindingResult implements Serializable {

	private final Object target;

	private transient BeanWrapper beanWrapper;


	/**
	 * Create a new BeanPropertyBindingResult instance.
	 * @param target the target bean to bind onto
	 * @param objectName the name of the target object
	 */
	public BeanPropertyBindingResult(Object target, String objectName) {
		super(objectName);
		Assert.notNull(target, "Target bean must not be null");
		this.target = target;
	}

	public Object getTarget() {
		return this.target;
	}

	/**
	 * Returns the canonical JavaBeans property name,
	 * as understood by the BeanWrapper.
	 */
	protected String canonicalFieldName(String field) {
		return BeanUtils.canonicalPropertyName(field);
	}

	/**
	 * Returns the BeanWrapper that this instance uses.
	 * Creates a new one if none existed before.
	 */
	public ConfigurablePropertyAccessor getPropertyAccessor() {
		if (this.beanWrapper == null) {
			this.beanWrapper = new BeanWrapperImpl(this.target);
			this.beanWrapper.setExtractOldValueForEditor(true);
		}
		return this.beanWrapper;
	}

}
