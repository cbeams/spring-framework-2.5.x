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

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.enum.CodedEnum;
import org.springframework.enum.CodedEnumResolver;
import org.springframework.enum.TypeMapping;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;
import org.springframework.util.Cache;

/**
 * @author keith
 */
public class CachingJdbcCodedEnumResolver implements InitializingBean,
        CodedEnumResolver {
    private Map typeMappings;
    private Cache enumTypeCache = new Cache(false) {
        public Object create(Object type) {
            return findEnums((String)type);
        }
    };
    private DataSource dataSource;

    public CachingJdbcCodedEnumResolver() {

    }

    public CachingJdbcCodedEnumResolver(DataSource source) {
        setDataSource(source);
    }

    public void setDataSource(DataSource source) {
        Assert.notNull(source);
        this.dataSource = source;
    }

    public void afterPropertiesSet() {
        Assert.notNull(dataSource,
                "The dataSource property is required and cannot be null.");
    }

    public void setTypeMappings(Map typeMappings) {
        this.typeMappings = typeMappings;
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnums(java.lang.String)
     */
    public Map getEnumsAsMap(String type) {
        return (Map)enumTypeCache.get(type);
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnums(java.lang.String)
     */
    public List getEnumsAsList(String type) {
        return Collections.unmodifiableList(new ArrayList(getEnumsAsMap(type)
                .values()));
    }

    private Map findEnums(String type) {
        TypeMapping mapping = getTypeMapping(type);
        Class clazz = mapping.getEnumClass();
        Assert.isTrue(CodedEnum.class.isAssignableFrom(clazz));
        return executeEnumQuery(mapping);
    }

    private Map executeEnumQuery(final TypeMapping mapping) {
        String enumTypeQuery = "SELECT " + mapping.getCodeColumn() + ", "
                + mapping.getLabelColumn() + " FROM " + mapping.getTable()
                + " ORDER BY " + mapping.getLabelColumn() + ";";

        final Map enumMap = new LinkedHashMap();
        new JdbcTemplate(dataSource).query(enumTypeQuery,
                new RowCallbackHandler() {
                    public void processRow(ResultSet set) throws SQLException {
                        Object code = set.getObject(1);
                        Class enumClass = mapping.getEnumClass();
                        Class codeClass;
                        if (code instanceof String) {
                            codeClass = char.class;
                            String codeStr = (String)code;
                            if (codeStr.length() == 1) {
                                code = new Character(codeStr.charAt(0));
                            }
                        } else if (code instanceof Integer
                                || code instanceof Short) {
                            codeClass = int.class;
                        } else {
                            throw new IllegalArgumentException(
                                    "No supported enum code class found for "
                                            + code);
                        }
                        String label = set.getString(2);
                        try {
                            Constructor c = enumClass
                                    .getConstructor(new Class[] { codeClass,
                                            String.class });
                            CodedEnum enum = (CodedEnum)BeanUtils
                                    .instantiateClass(c, new Object[] { code,
                                            label });
                            enumMap.put(enum.getCode(), enum);
                        } catch (Exception e) {
                            throw new DataIntegrityViolationException(
                                    "Unable to map data in table to enum class "
                                            + enumClass, e);
                        }
                    }
                });
        return enumMap;
    }

    private TypeMapping getTypeMapping(String type) {
        if (typeMappings == null) {
            return new TypeMapping(type);
        } else {
            TypeMapping mapping = (TypeMapping)typeMappings.get(type);
            Assert.notNull(mapping);
            return mapping;
        }
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnum(java.lang.String,
     *      java.lang.Object)
     */
    public CodedEnum getEnum(String type, Object code) {
        return (CodedEnum)getEnumsAsMap(type).get(code);
    }

    public void refreshCache() {
        Iterator keys = enumTypeCache.keys();
        this.enumTypeCache.clear();
        while (keys.hasNext()) {
            String type = (String)keys.next();
            getEnumsAsMap(type);
        }
    }

}