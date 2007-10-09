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

package org.springframework.jdbc.core.namedparam;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.PropertyAccessor;

import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyDescriptor;

/**
 * SqlParameterSource implementation that obtains parameter values
 * from bean properties of a given JavaBean object. The names of
 * the bean properties have to match the parameter names.
 *
 * <p>Uses a Spring BeanWrapper for bean property access underneath.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcTemplate
 * @see org.springframework.beans.BeanWrapper
 */
public class BeanPropertySqlParameterSource extends AbstractSqlParameterSource {

	private final BeanWrapper beanWrapper;

	private String[] propertyNames = null;

	/**
	 * Create a new BeanPropertySqlParameterSource for the given bean.
	 * @param object the bean instance to wrap
	 */
	public BeanPropertySqlParameterSource(Object object) {
		this.beanWrapper = new BeanWrapperImpl(object);
	}


	public boolean hasValue(String paramName) {
		return this.beanWrapper.isReadableProperty(paramName);
	}

	public Object getValue(String paramName) throws IllegalArgumentException {
		try {
			return this.beanWrapper.getPropertyValue(paramName);
		}
		catch (NotReadablePropertyException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * Provide access to the property names of the wrapped bean.
	 * Uses support provided in the {@link PropertyAccessor} interface.
	 * @return an array containing all the known property names
	 */
	public String[] getReadablePropertyNames() {

		synchronized(this) {
			if (propertyNames == null) {
				List names = new ArrayList();
				PropertyDescriptor[] props = beanWrapper.getPropertyDescriptors();
				for  (int i = 0; i < props.length; i++) {
					if (beanWrapper.isReadableProperty(props[i].getName())) {
						names.add(props[i].getName());
					}
				}
				propertyNames = (String[]) names.toArray(new String[names.size()]);
			}

		}

		return propertyNames;
	}

}
