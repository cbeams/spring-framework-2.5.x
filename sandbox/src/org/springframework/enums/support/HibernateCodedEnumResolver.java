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

package org.springframework.enums.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.enums.CodedEnum;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.closure.support.Block;

/**
 * @author Keith Donald
 */
public class HibernateCodedEnumResolver extends AbstractCodedEnumResolver {

	private HibernateTemplate hibernateTemplate;

	public HibernateCodedEnumResolver(HibernateTemplate template) {
		super(false);
		Assert.notNull(template, "The hibernate template is required");
		this.hibernateTemplate = template;
	}

	public CodedEnum getEnum(String type, Comparable code, Locale locale) {
		try {
			Class clazz = ClassUtils.forName(type);
			if (logger.isDebugEnabled()) {
				logger.debug("Loading coded enum persistent implementation class '" + clazz.getName() + "' with id '"
						+ code + "'");
			}
			return (CodedEnum)hibernateTemplate.load(clazz, (Serializable)code);
		}
		catch (ClassNotFoundException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"The type must be a valid java class identifier");
			iae.initCause(e);
			throw iae;
		}
		catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException("The code must be a serializable");
			iae.initCause(e);
			throw iae;
		}
	}

	public Collection getEnumsAsCollection(String type, Locale locale) {
		try {
			Class clazz = ClassUtils.forName(type);
			if (logger.isDebugEnabled()) {
				logger.debug("Loading all coded enum persistent implementations of class '" + clazz.getName());
			}
			return (Collection)hibernateTemplate.loadAll(clazz);
		}
		catch (ClassNotFoundException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"The type must be a valid java class identifier");
			iae.initCause(e);
			throw iae;
		}
	}

	public Map getEnumsAsMap(String type, Locale locale) {
		Collection all = getEnumsAsCollection(type, locale);
		final Map map = new HashMap(all.size());
		new Block() {
			protected void handle(Object o) {
				CodedEnum ce = (CodedEnum)o;
				map.put(ce.getCode(), ce);
			}
		}.forEach(all);
		return map;
	}

}