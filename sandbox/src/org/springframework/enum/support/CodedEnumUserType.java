/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.enum.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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
    private Class enumClass;
    private CodedEnumResolver enumResolver = StaticCodedEnumResolver
            .getInstance();
    private NullableType type;

    protected CodedEnumUserType(Class enumClass) {
        Assert.notNull(enumClass);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        setTypes(enumClass);
        this.enumClass = enumClass;
    }

    protected CodedEnumUserType(Class enumClass, NullableType type) {
        Assert.notNull(enumClass);
        Assert.notNull(type);
        Assert.isTrue(CodedEnum.class.isAssignableFrom(enumClass));
        this.enumClass = enumClass;
        this.type = type;
    }

    private void setTypes(Class enumClass) {
        if (ShortCodedEnum.class.isAssignableFrom(enumClass)) {
            this.type = Hibernate.SHORT;
        } else if (LetterCodedEnum.class.isAssignableFrom(enumClass)) {
            this.type = Hibernate.CHARACTER;
        } else if (StringCodedEnum.class.isAssignableFrom(enumClass)) {
            this.type = Hibernate.STRING;
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
        return type.sqlTypes(null);
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
        String type = ClassUtils.getShortNameAsProperty(enumClass);
        return enumResolver.getEnum(type, code);
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
        Object code = enum.getCode();
        stmt.setObject(index, code);
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