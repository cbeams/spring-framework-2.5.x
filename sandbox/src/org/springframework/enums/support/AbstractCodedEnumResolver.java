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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.enums.CodedEnum;
import org.springframework.enums.CodedEnumResolver;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;

/**
 * Abstract base class for localized coded enum resolvers.
 * 
 * @author Keith Donald
 */
public abstract class AbstractCodedEnumResolver implements CodedEnumResolver {

    private Map localeCache = new HashMap();

    protected final Log logger = LogFactory.getLog(getClass());

    public Set getEnumsAsSet(String type, Locale locale) {
        return Collections.unmodifiableSet(new TreeSet(getEnumsAsMap(type, locale).values()));
    }

    public Map getEnumsAsMap(String type, Locale locale) {
        Assert.notNull(type, "No type specified");
        Map localizedEnumTypes = getLocaleEnums(locale);
        Map typeEnums = (Map)localizedEnumTypes.get(type);
        if (typeEnums == null) {
            typeEnums = findLocalizedEnums(type, locale);
            if (typeEnums == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No enum types found for locale " + locale + "; returning empty map.");
                }
                return Collections.unmodifiableMap(Collections.EMPTY_MAP);
            }
            localizedEnumTypes.put(type, typeEnums);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returned map of enums of type '" + type + "; map contents="
                    + DefaultObjectStyler.call(typeEnums));
        }
        return Collections.unmodifiableMap(typeEnums);
    }

    public CodedEnum getEnum(String type, Object code, Locale locale) {
        Assert.notNull(code, "No enum code specified");
        Map typeEnums = (Map)getEnumsAsMap(type, locale);
        CodedEnum enum = (CodedEnum)typeEnums.get(code);
        if (enum == null) {
            logger.info("No enum found of type '" + type + "' with '" + code.getClass() + " code " + code + "', returning null.");
        }
        return enum;
    }

    private Map getLocaleEnums(Locale locale) {
        Map m = (Map)localeCache.get(locale);
        if (m == null) {
            m = new HashMap();
            localeCache.put(locale, m);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning enum type map for locale " + locale);
        }
        return m;
    }

    protected void put(Locale locale, CodedEnum enum) {
        Map localizedTypes = (Map)getLocaleEnums(locale);
        Map typeEnums = (Map)localizedTypes.get(enum.getType());
        if (typeEnums == null) {
            typeEnums = new HashMap();
            localizedTypes.put(enum.getType(), typeEnums);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Registering enum of type '" + enum.getType() + "', details=" + enum);
        }
        typeEnums.put(enum.getCode(), enum);
    }

    protected void add(CodedEnum enum) {
        put(null, enum);
    }

    protected Map findLocalizedEnums(String type, Locale locale) {
        logger.info("Assuming no enums exist for type " + type + " and locale " + locale);
        return null;
    }

}