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
import org.springframework.enums.StringCodedEnum;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Keith Donald
 */
public class CodedEnumUserType implements UserType, Serializable {
    private transient final Log logger = LogFactory.getLog(getClass());

    private String enumType;

    private Class enumClass;

    private transient CodedEnumResolver enumResolver = StaticCodedEnumResolver.instance();

    private NullableType codePersistentType;

    protected CodedEnumUserType(Class enumClass) {
        Assert.notNull(enumClass);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        initCodePersistentType(enumClass);
    }

    protected CodedEnumUserType(Class enumClass, NullableType persistentType) {
        Assert.notNull(enumClass);
        Assert.notNull(persistentType);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        setCodePersistentType(persistentType);
    }

    protected CodedEnumUserType(Class enumClass, String enumType,
            NullableType persistentType) {
        Assert.notNull(enumClass);
        Assert.notNull(persistentType);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        setEnumType(enumType);
        setCodePersistentType(persistentType);
    }

    protected void initCodePersistentType(Class enumClass) {
        if (ShortCodedEnum.class.isAssignableFrom(enumClass)) {
            setCodePersistentType(Hibernate.SHORT);
        }
        else if (LetterCodedEnum.class.isAssignableFrom(enumClass)) {
            setCodePersistentType(Hibernate.CHARACTER);
        }
        else if (StringCodedEnum.class.isAssignableFrom(enumClass)) {
            setCodePersistentType(Hibernate.STRING);
        }
    }

    protected void setCodePersistentType(NullableType persistentType) {
        this.codePersistentType = persistentType;
    }

    protected void setEnumType(String enumType) {
        this.enumType = enumType;
    }

    public void setEnumResolver(CodedEnumResolver resolver) {
        if (resolver == null) {
            resolver = StaticCodedEnumResolver.instance();
        }
        this.enumResolver = resolver;
    }

    public int[] sqlTypes() {
        return codePersistentType.sqlTypes(null);
    }

    public Class returnedClass() {
        return enumClass;
    }

    public boolean equals(Object o1, Object o2) throws HibernateException {
        return ObjectUtils.nullSafeEquals(o1, o2);
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        Object code;
        Assert.notNull(codePersistentType,
                "The enum code's persistent type must be set");
        if (codePersistentType == Hibernate.SHORT) {
            code = Hibernate.SHORT.nullSafeGet(rs, names[0]);
        }
        else if (codePersistentType == Hibernate.INTEGER) {
            code = Hibernate.INTEGER.nullSafeGet(rs, names[0]);
        }
        else if (codePersistentType == Hibernate.CHARACTER) {
            code = Hibernate.CHARACTER.nullSafeGet(rs, names[0]);
        }
        else {
            code = Hibernate.STRING.nullSafeGet(rs, names[0]);
        }
        if (code == null) { return null; }
        if (enumType == null) {
            enumType = ClassUtils.getShortNameAsProperty(enumClass);
        }
        CodedEnum enum = enumResolver.getEnum(enumType, code, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Resolved enum '" + enum + "' of type '" + enumType
                    + "' from persisted code " + code);
        }
        return enum;
    }

    public void nullSafeSet(PreparedStatement stmt, Object value, int index)
            throws HibernateException, SQLException {
        if ((value != null)
                && !returnedClass().isAssignableFrom(value.getClass())) { throw new IllegalArgumentException(
                "Received value is not a [" + returnedClass().getName()
                        + "] but [" + value.getClass() + "]"); }
        CodedEnum enum = (CodedEnum)value;
        if (enum != null) {
            Object code = enum.getCode();
            // for some reason some characters don't map well, convert to string
            // instead...
            if (code instanceof Character) {
                stmt.setString(index, ((Character)code).toString());
            }
            else {
                stmt.setObject(index, code);
            }
        }
        else {
            stmt.setNull(index, sqlTypes()[0]);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

}