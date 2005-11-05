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

package org.springframework.core.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;

/**
 * Abstract base class for LabeledEnumResolver implementations,
 * caching all retrieved LabeledEnum instances.
 *
 * <p>Subclasses need to implement the template method
 * <code>findLabeledEnums(type)</code>.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 * @see #findLabeledEnums(Class)
 */
public abstract class AbstractCachingLabeledEnumResolver implements LabeledEnumResolver {

	protected transient final Log logger = LogFactory.getLog(getClass());


	private CachingMapDecorator labeledEnumCache = new CachingMapDecorator() {
		protected Object create(Object key) {
			Set typeEnums = findLabeledEnums((Class) key);
			if (typeEnums == null || typeEnums.isEmpty()) {
				throw new IllegalArgumentException(
						"Unsupported labeled enumeration type '" + key + "': "
						+ "make sure you've properly defined this enumeration: "
						+ "if it's static, are the class and its fields public/static/final?");
			}
			Map typeEnumMap = new HashMap(typeEnums.size());
			for (Iterator it = typeEnums.iterator(); it.hasNext();) {
				LabeledEnum labeledEnum = (LabeledEnum) it.next();
				typeEnumMap.put(labeledEnum.getCode(), labeledEnum);
			}
			return Collections.unmodifiableMap(typeEnumMap);
		}
	};


	public Set getLabeledEnumSet(Class type) throws IllegalArgumentException {
		return new TreeSet(getLabeledEnumMap(type).values());
	}

	public Map getLabeledEnumMap(Class type) throws IllegalArgumentException {
		Assert.notNull(type, "No type specified");
		return (Map) this.labeledEnumCache.get(type);
	}

	public LabeledEnum getLabeledEnumByCode(Class type, Comparable code) throws IllegalArgumentException {
		Assert.notNull(code, "No enum code specified");
		Map typeEnums = getLabeledEnumMap(type);
		LabeledEnum codedEnum = (LabeledEnum) typeEnums.get(code);
		if (codedEnum == null) {
			throw new IllegalArgumentException(
					"No enumeration with code '" + code + "'" + " of type [" + type.getName()
					+ "] exists: this is likely a configuration error; "
					+ "make sure the code value matches a valid instance's code property");
		}
		return codedEnum;
	}

	public LabeledEnum getLabeledEnumByLabel(Class type, String label) throws IllegalArgumentException {
		Map typeEnums = getLabeledEnumMap(type);
		Iterator it = typeEnums.values().iterator();
		while (it.hasNext()) {
			LabeledEnum value = (LabeledEnum) it.next();
			if (value.getLabel().equalsIgnoreCase(label)) {
				return value;
			}
		}
		throw new IllegalArgumentException(
				"No enumeration with label '" + label + "' of type [" + type
				+ "] exists: this is likely a configuration error; "
				+ "make sure the label string matches a valid instance's label property");
	}


	/**
	 * Template method to be implemented by subclasses.
	 * Supposed to find all LabeledEnum instances for the given type.
	 * @param type the enum type
	 * @return the Set of LabeledEnum instances
	 * @see org.springframework.core.enums.LabeledEnum
	 */
	protected abstract Set findLabeledEnums(Class type);

}
