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
package org.springframework.enum.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.NullableType;

import org.springframework.enum.CodedEnum;
import org.springframework.enum.CodedEnumResolver;
import org.springframework.enum.LetterCodedEnum;
import org.springframework.enum.ShortCodedEnum;
import org.springframework.enum.StringCodedEnum;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Keith Donald
 */
public class CodedEnumUserType implements UserType {
    private String enumType;
    private Class enumClass;
    private CodedEnumResolver enumResolver = StaticCodedEnumResolver
            .instance();
    private NullableType persistentType;

    protected CodedEnumUserType(Class enumClass) {
        Assert.notNull(enumClass);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        setTypes(enumClass);
        this.enumClass = enumClass;
    }

    protected CodedEnumUserType(Class enumClass, String enumType) {
        Assert.notNull(enumClass);
        Assert.notNull(enumType);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        setTypes(enumClass);
        this.enumClass = enumClass;
        this.enumType = enumType;
    }

    protected CodedEnumUserType(Class enumClass, NullableType type) {
        Assert.notNull(enumClass);
        Assert.notNull(type);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        this.persistentType = type;
    }

    protected CodedEnumUserType(Class enumClass, String enumType, NullableType persistentType) {
        Assert.notNull(enumClass);
        Assert.notNull(enumType);
        Assert.notNull(persistentType);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        this.enumType = enumType;
        this.persistentType = persistentType;
    }


    private void setTypes(Class enumClass) {
        if (ShortCodedEnum.class.isAssignableFrom(enumClass)) {
            this.persistentType = Hibernate.SHORT;
        } else if (LetterCodedEnum.class.isAssignableFrom(enumClass)) {
            this.persistentType = Hibernate.CHARACTER;
        } else if (StringCodedEnum.class.isAssignableFrom(enumClass)) {
            this.persistentType = Hibernate.STRING;
        } else {
            throw new IllegalArgumentException(
                    "Unable to determine enum sql type.");
        }
    }

    public void setResolver(CodedEnumResolver resolver) {
        this.enumResolver = resolver;
    }

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return persistentType.sqlTypes(null);
    }

    /**
     * @see net.sf.hibernate.UserType#returnedClass()
     */
    public Class returnedClass() {
        return enumClass;
    }

    /**
     * @see net.sf.hibernate.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object o1, Object o2) throws HibernateException {
        return ObjectUtils.nullSafeEquals(o1, o2);
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        Object code = rs.getObject(names[0]);
        if (code == null) {
            return null;
        }
        if (enumType == null) {
            enumType = ClassUtils.getShortNameAsProperty(enumClass);
        }
        return enumResolver.getEnum(enumType, code, null);
    }

    /**
     * @see net.sf.hibernate.UserType#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement stmt, Object value, int index)
            throws HibernateException, SQLException {
        if ((value != null)
                && !returnedClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Received value is not a ["
                    + returnedClass().getName() + "] but [" + value.getClass()
                    + "]");
        }
        CodedEnum enum = (CodedEnum)value;
        if (enum != null) {
            Object code = enum.getCode();
            stmt.setObject(index, code);
        } else {
            stmt.setNull(index, sqlTypes()[0]);
        }
    }

    /**
     * @see net.sf.hibernate.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /**
     * @see net.sf.hibernate.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

}