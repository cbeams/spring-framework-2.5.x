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

import java.beans.PropertyEditorSupport;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author keith
 */
public class CodedEnumEditor extends PropertyEditorSupport {
    private CodedEnumResolver resolver;

    public void setEnumResolver(CodedEnumResolver resolver) {
        this.resolver = resolver;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        String[] keyParts = StringUtils.delimitedListToStringArray(text, ".");
        Assert.isTrue(keyParts.length == 2, "Enum key must be formatted <type>.<code>");

        Object code;
        String strCode = keyParts[1];
        if (strCode.length() == 1) {
            char c = strCode.charAt(0);
            if (Character.isLetter(c)) {
                code = new Character(c);
            } else if (Character.isDigit(c)) { 
                code = new Integer(c);
            } else {
                throw new IllegalArgumentException("Invalid enum code '" + strCode + "'");
            }
        } else {
            try {
                code = new Integer(strCode);
            } catch (NumberFormatException e) {
                code = strCode;
            }            
        }
        CodedEnum enum = resolver.getEnum(keyParts[0], code);
        Assert.notNull(enum, "No enum with key " + text + " was found");
        setValue(enum);
    }

    public String getAsText() {
        CodedEnum enum = (CodedEnum)getValue();
        return String.valueOf(enum.getKey());
    }

}