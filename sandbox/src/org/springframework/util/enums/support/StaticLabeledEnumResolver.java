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
package org.springframework.util.enums.support;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.closure.Closure;
import org.springframework.util.closure.support.AbstractProcessTemplate;
import org.springframework.util.closure.support.Block;
import org.springframework.util.enums.LabeledEnum;

/**
 * Resolves statically (in Java code) defined enumerations.
 * @author Keith Donald
 */
public class StaticLabeledEnumResolver extends AbstractLabeledEnumResolver {

	private static final StaticLabeledEnumResolver INSTANCE = new StaticLabeledEnumResolver();

	public static StaticLabeledEnumResolver instance() {
		return INSTANCE;
	}

	protected Map findLabeledEnums(String type) {
		final Map enums = new TreeMap();
		try {
			new LabeledEnumFieldValueGenerator(ClassUtils.forName(type)).run(new Block() {
				protected void handle(Object value) {
					LabeledEnum e = (LabeledEnum)value;
					enums.put(e.getCode(), e);
				}
			});
		}
		catch (ClassNotFoundException e) {
			IllegalArgumentException iae = new IllegalArgumentException("Type does not map to a valid enum class");
			iae.initCause(e);
			throw iae;
		}
		return enums;
	}

	/**
	 * Generator that generates a list of static field values that can be
	 * processed.
	 * @author Keith Donald
	 */
	private static class LabeledEnumFieldValueGenerator extends AbstractProcessTemplate {
		private static final Log logger = LogFactory.getLog(LabeledEnumFieldValueGenerator.class);

		private Class clazz;

		public LabeledEnumFieldValueGenerator(Class clazz) {
			Assert.notNull(clazz, "clazz is required");
			this.clazz = clazz;
		}

		public void run(Closure fieldValueCallback) {
			Field[] fields = clazz.getFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
					if (clazz.isAssignableFrom(field.getType())) {
						try {
							Object value = field.get(null);
							Assert.isTrue(LabeledEnum.class.isInstance(value),
									"Field value must be a LabeledEnum instance.");
							fieldValueCallback.call(value);
						}
						catch (IllegalAccessException e) {
							logger.warn("Unable to access field value " + field, e);
						}
					}
				}
			}
		}
	}

	public LabeledEnum getLabeledEnum(Class type, Comparable code) {
		return getLabeledEnum(type.getName(), code);
	}

	public LabeledEnum getRequiredLabeledEnum(Class type, Comparable code) {
		return getRequiredEnum(type.getName(), code);
	}

	public Collection getLabeledEnumCollection(Class type) {
		return getLabeledEnumCollection(type.getName());
	}

	public Map getLabeledEnumMap(Class type) {
		return getLabeledEnumMap(type.getName());
	}
}