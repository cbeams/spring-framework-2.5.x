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
package org.springframework.util;

/**
 * A strategy interface for pretty-printing toString() methods. Ecapsulates the
 * print algorithms; some other object such as a builder should provide the
 * workflow.
 * 
 * @author Keith Donald
 */
public interface ToStringStyler {

    /**
     * Style a toString()'ed object before its fields are styled.
     * 
     * @param buffer
     *            the buffer to print to.
     * @param o
     *            The object to style.
     */
    public void styleStart(StringBuffer buffer, Object o);

    /**
     * Style a toString()'ed object after it's fields are styled.
     * 
     * @param buffer
     *            the buffer to print to.
     * @param o
     *            The object to style.
     */
    public void styleEnd(StringBuffer buffer, Object o);

    /**
     * Style a field value as a string.
     * 
     * @param buffer
     *            buffer to print to.
     * @param fieldName
     *            The name of the field.
     * @param value
     *            The field value.
     * @return The pretty-printed string.
     */
    public void styleField(StringBuffer buffer, String fieldName, Object value);

    /**
     * Style the field separator.
     * 
     * @param buffer
     *            buffer to print to.
     */
    public void styleFieldSeparator(StringBuffer buffer);
}
