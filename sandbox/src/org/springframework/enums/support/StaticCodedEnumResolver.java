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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.enums.CodedEnum;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.closure.Closure;
import org.springframework.util.closure.ProcessTemplate;
import org.springframework.util.closure.support.Block;

/**
 * Resolves statically (in Java code) defined enumerations.
 * @author Keith Donald
 */
public class StaticCodedEnumResolver extends AbstractCodedEnumResolver {

	private static final StaticCodedEnumResolver INSTANCE = new StaticCodedEnumResolver();

	public static StaticCodedEnumResolver instance() {
		return INSTANCE;
	}

	protected Map findLocalizedEnums(String type, Locale locale) {
		final Map enums = new TreeMap();
		try {
			new CodedEnumFieldValueGenerator(ClassUtils.forName(type)).run(new Block() {
				protected void handle(Object value) {
					CodedEnum e = (CodedEnum) value;
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
	 * Call to register all the statically defined enumerations for a specific
	 * enumeration <code>Class</code>.
	 * <p>
	 * Iterates over the static fields of the class and adds all instances of
	 * <code>CodedEnum</code> to the list resolvable by this resolver.
	 *
	 * @param clazz The enum class.
	 */
	public void registerStaticEnums(final Class clazz) {
		if (logger.isDebugEnabled()) {
			logger.debug("Registering statically defined coded enums for class " + clazz);
		}
		new CodedEnumFieldValueGenerator(clazz).run(new Block() {
			protected void handle(Object value) {
				add((CodedEnum) value);
			}
		});
	}

	/**
	 * Generator that generates a list of static field values that can be
	 * processed.
	 *
	 * @author Keith Donald
	 */
	private static class CodedEnumFieldValueGenerator implements ProcessTemplate {

		private static final Log logger = LogFactory.getLog(CodedEnumFieldValueGenerator.class);

		private Class clazz;

		public CodedEnumFieldValueGenerator(Class clazz) {
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
							Assert.isTrue(CodedEnum.class.isInstance(value),
									"Field value must be a CodedEnum instance.");
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

	public CodedEnum getEnum(Class type, Comparable code) {
		return getEnum(type.getName(), code, null);
	}

	public CodedEnum getRequiredEnum(Class type, Comparable code) throws ObjectRetrievalFailureException {
		return getRequiredEnum(type.getName(), code, null);
	}

	public Collection getEnumAsCollection(Class type) {
		return getEnumsAsCollection(type.getName(), null);
	}

	public Map getEnumAsMap(Class type) {
		return getEnumsAsMap(type.getName(), null);
	}

}
