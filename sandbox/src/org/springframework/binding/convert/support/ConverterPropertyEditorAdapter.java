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
package org.springframework.binding.convert.support;

import java.beans.PropertyEditorSupport;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;
import org.springframework.util.Assert;

/**
 * Adapts a formatter to the property editor interface.
 * @author Keith Donald
 */
public class ConverterPropertyEditorAdapter extends PropertyEditorSupport {

    private Converter converter;

    public ConverterPropertyEditorAdapter(Converter converter) {
        Assert.notNull(converter, "Converter is required");
        this.converter = converter;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(converter.convert(text));
        }
        catch (ConversionException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Could not convert back from string: "
                    + e.getMessage());
            iae.initCause(e);
            throw iae;
        }
    }

    public String getAsText() {
		return (String)converter.convert(getValue());
    }
}