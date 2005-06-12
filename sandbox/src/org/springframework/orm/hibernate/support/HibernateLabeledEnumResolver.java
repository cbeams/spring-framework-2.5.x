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
package org.springframework.orm.hibernate.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.util.Assert;

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

	public LabeledEnum getLabeledEnum(Class type, Comparable code) throws IllegalArgumentException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading coded enum persistent implementation class '" + type.getName() + "' with id '"
						+ code + "'");
			}
			LabeledEnum le = (LabeledEnum)hibernateTemplate.get(type, (Serializable)code);
			if (le == null) {
				throw new IllegalArgumentException("No labeled enum instance found of type '" + type + "' with code '"
						+ code + "'");
			}
			return le;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException("The code must be a serializable");
			iae.initCause(e);
			throw iae;
		}
	}

	public LabeledEnum getLabeledEnum(Class type, String label) throws IllegalArgumentException {
		LabeledEnum le = (LabeledEnum)DataAccessUtils.uniqueResult(hibernateTemplate.find("from " + type
				+ " t where t.label == '" + label + "'"));
		if (le == null) {
			throw new IllegalArgumentException("No labeled enum instance found of type '" + type + "' with label '"
					+ label + "'");
		}
		return le;
	}

	public Collection getLabeledEnumCollection(Class type) {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading all coded enum persistent implementations of class '" + type.getName());
		}
		return (Collection)hibernateTemplate.loadAll(type);
	}

	public Map getLabeledEnumMap(Class type) {
		Collection all = getLabeledEnumCollection(type);
		final Map map = new HashMap(all.size());
		Iterator it = all.iterator();
		while (it.hasNext()) {
			LabeledEnum ce = (LabeledEnum)it.next();
			map.put(ce.getCode(), ce);
		}
		return map;
	}
}