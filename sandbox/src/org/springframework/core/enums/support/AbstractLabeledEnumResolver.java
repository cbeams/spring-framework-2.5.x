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
package org.springframework.core.enums.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapTemplate;

/**
 * Abstract base class for labeled enum resolvers.
 * @author Keith Donald
 */
public abstract class AbstractLabeledEnumResolver implements LabeledEnumResolver {
	protected transient final Log logger = LogFactory.getLog(getClass());

	private CachingMapTemplate labeledEnumCache = new CachingMapTemplate() {
		protected Object create(Object key) {
			Map typeEnums = findLabeledEnums((Class)key);
			if (typeEnums != null && !typeEnums.isEmpty()) {
				return typeEnums;
			}
			else {
				throw new IllegalArgumentException("Unsupported labeled enumeration type '" + key + "'"
						+ " make sure you've properly defined this enumeration: "
						+ "if it's static, are the class and its fields public/static/final?");
			}
		}
	};

	protected AbstractLabeledEnumResolver() {
	}

	public Collection getLabeledEnumCollection(Class type) throws IllegalArgumentException {
		return Collections.unmodifiableSet(new TreeSet(getLabeledEnumMap(type).values()));
	}

	public Map getLabeledEnumMap(Class type) throws IllegalArgumentException {
		Assert.notNull(type, "No type specified");
		Map typeEnums = (Map)labeledEnumCache.get(type);
		return Collections.unmodifiableMap(typeEnums);
	}

	public LabeledEnum getLabeledEnum(Class type, Comparable code) throws IllegalArgumentException {
		Assert.notNull(code, "No enum code specified");
		Map typeEnums = getLabeledEnumMap(type);
		LabeledEnum codedEnum = (LabeledEnum)typeEnums.get(code);
		if (codedEnum == null) {
			throw new IllegalArgumentException("No enumeration with code '" + code + "'" + " of type '" + type
					+ "' exists--this is likely a configuration error;"
					+ " make sure the code value matches a valid instance's code property");
		}
		return codedEnum;
	}

	public LabeledEnum getLabeledEnum(Class type, String label) throws IllegalArgumentException {
		Map typeEnums = getLabeledEnumMap(type);
		Iterator it = typeEnums.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			LabeledEnum value = (LabeledEnum)entry.getValue();
			if (value.getLabel().equalsIgnoreCase(label)) {
				return value;
			}
		}
		throw new IllegalArgumentException("No enumeration with label '" + label + "'" + " of type '" + type
				+ "' exists--this is likely a configuration error;"
				+ " make sure the label string matches a valid instance's label property");
	}

	protected Map findLabeledEnums(Class type) {
		logger.info("Assuming no enums exist for type " + type + "'");
		return null;
	}
}