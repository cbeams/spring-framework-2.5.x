/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.enum.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.enum.CodedEnum;
import org.springframework.enum.CodedEnumResolver;

/**
 * @author Keith Donald
 */
public class StaticCodedEnumResolver implements CodedEnumResolver {
    private static final StaticCodedEnumResolver INSTANCE = new StaticCodedEnumResolver();
    private Map enumsByType = new HashMap();

    public static StaticCodedEnumResolver getInstance() {
        return INSTANCE;
    }

    public void add(CodedEnum enum) {
        Map enums = (Map)this.enumsByType.get(enum.getType());
        System.out.println(enum);
        System.out.println(enum.getCode().getClass());

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
        return (CodedEnum)getEnumsAsMap(type).get(code);
    }

}