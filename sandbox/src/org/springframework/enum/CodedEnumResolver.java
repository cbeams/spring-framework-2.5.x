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
package org.springframework.enum;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interface for looking up <code>CodedEnum</code> instances. If you require
 * localized enumerations, use the Locale parameter to specify the locale to
 * use.
 * 
 * @author Keith Donald
 */
public interface CodedEnumResolver {

    /**
     * Returns a list of enumerations of a particular type. Each element in the
     * list should be an instance of CodedEnum.
     * 
     * @param type
     *            the enum type
     * @param locale
     *            the locale
     * @return A list of localized enumeration instances for the provided type.
     */
    public List getEnumsAsList(String type, Locale locale);

    /**
     * Returns a map of enumerations of a particular type. Each element in the
     * map should be a key->value pair, where the key is the enum code, and the
     * value is the <code>CodedEnum</code> instance.
     * 
     * @param type
     *            the enum type
     * @param locale
     *            the locale
     * @return A map of localized enumeration instances.
     */
    public Map getEnumsAsMap(String type, Locale locale);

    /**
     * Resolves a single <code>CodedEnum</code> by its identifying code.
     * 
     * @param type
     *            the enum type
     * @param code
     *            the enum code
     * @param locale
     *            the locale
     * @return The enum, or <code>null</code> if not found.
     */
    public CodedEnum getEnum(String type, Object code, Locale locale);
}