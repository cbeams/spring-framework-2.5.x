/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.enums.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.enums.CodedEnum;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.closure.support.Block;

/**
 * @author Keith Donald
 */
public class HibernateCodedEnumResolver extends AbstractCodedEnumResolver {
    private HibernateTemplate hibernateTemplate;

    public HibernateCodedEnumResolver(HibernateTemplate template) {
        super(false);
        Assert.notNull(template, "The hibernate template is required");
        this.hibernateTemplate = template;
    }

    public CodedEnum getEnum(String type, Comparable code, Locale locale) {
        try {
            return (CodedEnum)hibernateTemplate.load(ClassUtils.forName(type), (Serializable)code);
        }
        catch (ClassNotFoundException e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "The type must be a valid java class identifier");
            iae.initCause(e);
            throw iae;
        }
        catch (ClassCastException e) {
            IllegalArgumentException iae = new IllegalArgumentException("The code must be a serializable");
            iae.initCause(e);
            throw iae;
        }
    }

    public Collection getEnumsAsCollection(String type, Locale locale) {
        try {
            return (Collection)hibernateTemplate.loadAll(ClassUtils.forName(type));
        }
        catch (ClassNotFoundException e) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "The type must be a valid java class identifier");
            iae.initCause(e);
            throw iae;
        }
    }

    public Map getEnumsAsMap(String type, Locale locale) {
        Collection all = getEnumsAsCollection(type, locale);
        final Map map = new HashMap(all.size());
        new Block() {
            protected void handle(Object o) {
                CodedEnum ce = (CodedEnum)o;
                map.put(ce.getCode(), ce);
            }
        }.forEach(all);
        return map;
    }

}