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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.enum.CodedEnum;
import org.springframework.enum.CodedEnumResolver;
import org.springframework.rules.Generator;
import org.springframework.rules.UnaryProcedure;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class StaticCodedEnumResolver implements CodedEnumResolver {
    private static final StaticCodedEnumResolver INSTANCE = new StaticCodedEnumResolver();
    private Map enumsByType = new HashMap();

    public static StaticCodedEnumResolver getInstance() {
        return INSTANCE;
    }

    private static class FieldGenerator implements Generator {
        private Class clazz;

        public FieldGenerator(Class clazz) {
            Assert.notNull(clazz);
            this.clazz = clazz;
        }

        public void run(UnaryProcedure procedure) {
            Field[] fields = clazz.getFields();
            for (int i = 0; i < fields.length; i++) {
                procedure.run(fields[i]);
            }
        }
    }

    public void registerStaticEnums(final Class clazz) {
        System.out.println(clazz);
        new FieldGenerator(clazz).run(new UnaryProcedure() {
            public void run(Object o) {
                Field f = (Field)o;
                if (Modifier.isStatic(f.getModifiers())) {
                    if (CodedEnum.class.isAssignableFrom(f.getType())) {
                        try {
                            Object value = f.get(null);
                            Assert.isTrue(CodedEnum.class.isInstance(value));
                            add((CodedEnum)value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    public void add(CodedEnum enum) {
        Map enums = (Map)this.enumsByType.get(enum.getType());
        if (enums == null) {
            enums = new TreeMap();
            enums.put(enum.getCode(), enum);
            enumsByType.put(enum.getType(), enums);
        } else {
            enums.put(enum.getCode(), enum);
        }
    }

    public void add(CodedEnum[] enums) {
        for (int i = 0; i < enums.length; i++) {
            add(enums[i]);
        }
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnumsAsList(java.lang.String)
     */
    public List getEnumsAsList(String type) {
        return Collections.unmodifiableList(new ArrayList(getEnumsAsMap(type)
                .values()));
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnumsAsMap(java.lang.String)
     */
    public Map getEnumsAsMap(String type) {
        Assert.notNull(type);
        SortedMap enums = (SortedMap)this.enumsByType.get(type);
        if (enums == null) {
            return Collections.unmodifiableMap(Collections.EMPTY_MAP);
        } else {
            return Collections.unmodifiableSortedMap(enums);
        }
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnum(java.lang.String,
     *      java.lang.Object)
     */
    public CodedEnum getEnum(String type, Object code) {
        Assert.notNull(code);
        return (CodedEnum)getEnumsAsMap(type).get(code);
    }

}