/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.hibernate.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.Assert;

/**
 * A LabeledEnumResolver that loads persistent enums using <code>Hibernate</code>.
 *
 * @author Keith Donald
 */
public class HibernateLabeledEnumResolver extends HibernateDaoSupport implements LabeledEnumResolver {

	private final Log logger = LogFactory.getLog(getClass());

	public Set getLabeledEnumSet(Class type) {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading all coded enum persistent instances of class: " + type.getName());
		}
		return new TreeSet(getHibernateTemplate().loadAll(type));
	}

	public Map getLabeledEnumMap(Class type) {
		Set all = getLabeledEnumSet(type);
		final Map map = new HashMap(all.size());
		for (Iterator it = all.iterator(); it.hasNext();) {
			LabeledEnum le = (LabeledEnum) it.next();
			map.put(le.getCode(), le);
		}
		return map;
	}

	public LabeledEnum getLabeledEnumByCode(Class type, Comparable code) throws IllegalArgumentException {
		Assert.isTrue(code instanceof Serializable, "code must implement Serializable");
		if (logger.isDebugEnabled()) {
			logger.debug("Loading coded enum persistent implementation class [" + type.getName() +
					"] with id '" + code + "'");
		}
		LabeledEnum le = (LabeledEnum) getHibernateTemplate().get(type, (Serializable) code);
		if (le == null) {
			throw new IllegalArgumentException(
					"No labeled enum instance found of type [" + type.getName() + "] with code '" + code + "'");
		}
		return le;
	}

	public LabeledEnum getLabeledEnumByLabel(Class type, String label) throws IllegalArgumentException {
		LabeledEnum le = (LabeledEnum) DataAccessUtils.uniqueResult(
				getHibernateTemplate().find("from " + type + " t where t.label == '" + label + "'"));
		if (le == null) {
			throw new IllegalArgumentException("No LabeledEnum instance found of type '" + type + "' with label '"
					+ label + "'");
		}
		return le;
	}

}
