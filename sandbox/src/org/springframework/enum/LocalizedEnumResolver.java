/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.springframework.enum;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author kdonald
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface LocalizedEnumResolver {
    public List getEnumsAsList(String type, Locale locale);
    public Map getEnumsAsMap(String type, Locale locale);
    public CodedEnum getEnum(String type, Object code, Locale locale);
}