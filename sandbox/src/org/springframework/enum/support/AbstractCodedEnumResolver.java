/*
 * Created on May 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.springframework.enum.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.enum.CodedEnum;
import org.springframework.enum.LocalizedEnumResolver;

/**
 * @author kdonald
 */
public abstract class AbstractCodedEnumResolver implements LocalizedEnumResolver {
    private Map localeCache = new HashMap();

    public List getEnumsAsList(String type, Locale locale) {
        return Collections.unmodifiableList(new ArrayList(getEnumsAsMap(type,
                locale).values()));
    }

    public Map getEnumsAsMap(String type, Locale locale) {
        Map locales = getLocaleEnums(locale);
        Map types = (Map)locales.get(type);
        if (types == null) {
            types = findLocalizedEnums(type, locale);
            locales.put(locale, types);
        }
        return Collections.unmodifiableMap(types);
    }

    public void put(Locale locale, CodedEnum enum) {
        Map enums = (Map)getLocaleEnums(locale).get(enum.getType());
        enums.put(enum.getCode(), enum);
    }

    private Map getLocaleEnums(Locale locale) {
        Map m = (Map)localeCache.get(locale);
        if (m == null) {
            localeCache.put(locale, new HashMap());
        }
        return m;
    }

    public CodedEnum getEnum(String type, Object code, Locale locale) {
        return null;
    }
    
    protected abstract Map findLocalizedEnums(String type, Locale locale);

}