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
package org.springframework.enums;

import org.springframework.context.MessageSourceResolvable;

/**
 * A interface for objects that are enumerations. Each enum instance has the
 * following characteristics:
 * 
 * <p>
 * A type that identifies the enum's class. For example, "fileFormat".
 * <p>
 * A code that uniquely identifies the enum within the context of its type. For
 * example, "CSV".  Different classes of codes are possible (Character, Integer, String.)
 * <p>
 * A descriptive label. For example, "the CSV File Format".
 * <p>
 * A uniquely identifying key that identifies the enum in the context of all
 * other enums (of potentially different types.) For example, "fileFormat.CSV".
 * 
 * @author Keith Donald
 */
public interface CodedEnum extends MessageSourceResolvable, Comparable {

    /**
     * Returns this enumeration's type.  Each type should be unique.
     * 
     * @return The type.
     */
    public String getType();

    /**
     * Returns this enumeration's code.  Each code should be unique within enumeration's
     * of the same type.
     * 
     * @return The code.
     */
    public Object getCode();

    /**
     * Returns a descriptive, optional label.
     * 
     * @return The label.
     */
    public String getLabel();

    /**
     * Returns a uniquely indentifying key string.  A key generally consists of the
     * <type>.<code> composite and should globally uniquely identify this enumeration.
     * 
     * @return The unique key.
     */
    public String getKey();
}