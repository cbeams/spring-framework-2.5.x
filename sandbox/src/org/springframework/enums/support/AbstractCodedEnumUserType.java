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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.NullableType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.enums.CodedEnum;
import org.springframework.enums.CodedEnumResolver;
import org.springframework.enums.LetterCodedEnum;
import org.springframework.enums.ShortCodedEnum;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * @author Keith Donald
 */
public abstract class AbstractCodedEnumUserType implements UserType, Serializable {

	private transient final Log logger = LogFactory.getLog(getClass());

	private transient CodedEnumResolver enumResolver = StaticCodedEnumResolver.instance();

	protected CodedEnumResolver getEnumResolver() {
		if (enumResolver == null) {
			return StaticCodedEnumResolver.instance();
		}
		return enumResolver;
	}

	public void setEnumResolver(CodedEnumResolver resolver) {
		this.enumResolver = resolver;
	}

	public int[] sqlTypes() {
		return new int[] { persistentType().sqlType() };
	}

	protected String enumType() {
		return returnedClass().getName();
	}

	public boolean equals(Object o1, Object o2) throws HibernateException {
		return ObjectUtils.nullSafeEquals(o1, o2);
	}

	protected NullableType persistentType() {
		if (ShortCodedEnum.class.isAssignableFrom(returnedClass())) {
			return Hibernate.SHORT;
		}
		else if (LetterCodedEnum.class.isAssignableFrom(returnedClass())) {
			return Hibernate.CHARACTER;
		}
		else {
			return Hibernate.STRING;
		}
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		Comparable code = (Comparable)persistentType().nullSafeGet(rs, names[0]);
		if (code == null) {
			return null;
		}
		CodedEnum e = enumResolver.getEnum(enumType(), code, null);
		if (logger.isDebugEnabled()) {
			logger.debug("Resolved enum '" + e + "' of type '" + enumType() + "' from persisted code " + code);
		}
		return e;
	}

	public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException {
		if ((value != null) && !returnedClass().isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Received value is not a [" + returnedClass().getName() + "] but ["
					+ value.getClass() + "]");
		}
		CodedEnum codedEnum = (CodedEnum)value;
		Comparable code = null;
		if (codedEnum != null) {
			code = codedEnum.getCode();
			Assert.notNull(code, "Enum codes cannot be null, but this one is");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Binding enum code '" + code + "' to parameter index " + index);
		}
		persistentType().nullSafeSet(stmt, code, index);
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}
}