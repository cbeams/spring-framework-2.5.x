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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.enum.CodedEnum;
import org.springframework.enum.CodedEnumResolver;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;

/**
 * Abstract base class for localized coded enum resolvers.
 * 
 * @author Keith Donald
 */
public abstract class AbstractCodedEnumResolver implements CodedEnumResolver {
    private Map localeCache = new HashMap();

    protected static final Log logger = LogFactory
            .getLog(AbstractCodedEnumResolver.class);

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnumsAsList(java.lang.String,
     *      java.util.Locale)
     */
    public List getEnumsAsList(String type, Locale locale) {
        return Collections.unmodifiableList(new ArrayList(getEnumsAsMap(type,
                locale).values()));
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnumsAsMap(java.lang.String,
     *      java.util.Locale)
     */
    public Map getEnumsAsMap(String type, Locale locale) {
        Assert.notNull(type);
        Map localizedEnumTypes = getLocaleEnums(locale);
        Map typeEnums = (Map)localizedEnumTypes.get(type);
        if (typeEnums == null) {
            typeEnums = findLocalizedEnums(type, locale);
            if (typeEnums == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No enum types found for locale " + locale
                            + "; returning empty map.");
                }
                return Collections.unmodifiableMap(Collections.EMPTY_MAP);
            }
            localizedEnumTypes.put(locale, typeEnums);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returned map of enums of type '" + type
                    + "; map contents="
                    + DefaultObjectStyler.evaluate(typeEnums));
        }
        return Collections.unmodifiableMap(typeEnums);
    }

    /**
     * @see org.springframework.enum.CodedEnumResolver#getEnum(java.lang.String,
     *      java.lang.Object, java.util.Locale)
     */
    public CodedEnum getEnum(String type, Object code, Locale locale) {
        Assert.notNull(code);
        Map typeEnums = (Map)getEnumsAsMap(type, locale);
        CodedEnum e = (CodedEnum)typeEnums.get(new Integer(12));
        CodedEnum enum = (CodedEnum)typeEnums.get(code);
        if (enum == null) {
            logger.info("No enum found with code " + code + "; code argument class="
                    + code.getClass());
        }
        return enum;
    }

    private Map getLocaleEnums(Locale locale) {
        Map m = (Map)localeCache.get(locale);
        if (m == null) {
            m = new HashMap();
            localeCache.put(locale, m);
        }
        return m;
    }

    protected void put(Locale locale, CodedEnum enum) {
        Map localizedTypes = (Map)getLocaleEnums(locale);
        Map enums = (Map)localizedTypes.get(enum.getType());
        if (enums == null) {
            enums = new HashMap();
            localizedTypes.put(enum.getType(), enums);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Registering enum of type '" + enum.getType()
                    + "', details=" + enum);
        }
        enums.put(enum.getCode(), enum);
    }

    protected void add(CodedEnum enum) {
        put(null, enum);
    }

    protected Map findLocalizedEnums(String type, Locale locale) {
        return null;
    }

}