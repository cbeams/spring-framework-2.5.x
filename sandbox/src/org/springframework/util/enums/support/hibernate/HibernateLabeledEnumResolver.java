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
package org.springframework.util.enums.support.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.closure.support.Block;
import org.springframework.util.enums.LabeledEnum;
import org.springframework.util.enums.LabeledEnumResolver;

/**
 * A enum resolver that loads enums using the <code>Hibernate</code> data
 * access apis.
 * @author Keith Donald
 */
public class HibernateLabeledEnumResolver implements LabeledEnumResolver {

	private final Log logger = LogFactory.getLog(getClass());
	
	private HibernateTemplate hibernateTemplate;

	public HibernateLabeledEnumResolver(HibernateTemplate template) {
		Assert.notNull(template, "The hibernate template is required");
		this.hibernateTemplate = template;
	}

	public LabeledEnum getLabeledEnum(String type, Comparable code) {
		try {
			Class clazz = ClassUtils.forName(type);
			if (logger.isDebugEnabled()) {
				logger.debug("Loading coded enum persistent implementation class '" + clazz.getName() + "' with id '"
						+ code + "'");
			}
			return (LabeledEnum)hibernateTemplate.load(clazz, (Serializable)code);
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

	public LabeledEnum getLabeledEnum(String type, String label) {
		throw new UnsupportedOperationException();
	}

	public Collection getLabeledEnumCollection(String type) {
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

	public Map getLabeledEnumMap(String type) {
		Collection all = getLabeledEnumCollection(type);
		final Map map = new HashMap(all.size());
		new Block() {
			protected void handle(Object o) {
				LabeledEnum ce = (LabeledEnum)o;
				map.put(ce.getCode(), ce);
			}
		}.forEach(all);
		return map;
	}
}