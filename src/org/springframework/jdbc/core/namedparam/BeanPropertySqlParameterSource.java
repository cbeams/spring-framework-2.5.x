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

}
