/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.binding.format;

import java.beans.PropertyEditorSupport;

import org.springframework.util.Assert;

/**
 * Adapts a formatter to the property editor interface.
 * @author Keith Donald
 */
public class FormatterPropertyEditorAdapter extends PropertyEditorSupport {

    private Formatter formatter;

    public FormatterPropertyEditorAdapter(Formatter formatter) {
        Assert.notNull(formatter, "Formatter is required");
        this.formatter = formatter;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(formatter.parseValue(text));
        }
        catch (InvalidFormatException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Could not convert back from string: "
                    + e.getMessage());
            iae.initCause(e);
            throw iae;
        }
    }

    public String getAsText() {
		return formatter.formatValue(getValue());
    }
}