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

import java.io.Serializable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.ToStringBuilder;

/**
 * @author Keith Donald
 */
public abstract class LabeledCodedEnum implements CodedEnum, Serializable,
        MessageSourceResolvable {
    private CodedEnum enum;
    private String label;

    protected LabeledCodedEnum(int code, String label) {
        this.enum = new IntegerCodedEnum(code) {
            public String getType() {
                return LabeledCodedEnum.this.getType();
            }
        };
        this.label = label;
    }

    protected LabeledCodedEnum(char code, String label) {
        this.enum = new LetterCodedEnum(code) {
            public String getType() {
                return LabeledCodedEnum.this.getType();
            }
        };
        this.label = label;
    }

    protected LabeledCodedEnum(String code, String label) {
        this.enum = new StringCodedEnum(code) {
            public String getType() {
                return LabeledCodedEnum.this.getType();
            }
        };
        this.label = label;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof LabeledCodedEnum)) {
            return false;
        }
        LabeledCodedEnum e = (LabeledCodedEnum)o;
        return this.enum.equals(e.enum);
    }

    public int hashCode() {
        return enum.hashCode();
    }

    public String getLabel() {
        return label;
    }

    /**
     * @see org.springframework.enum.CodedEnum#getKey()
     */
    public String getKey() {
        return getType() + "." + getCode();
    }

    /**
     * @see org.springframework.enum.CodedEnum#getType()
     */
    public String getType() {
        return enum.getType();
    }

    /**
     * @see org.springframework.enum.CodedEnum#getCode()
     */
    public Object getCode() {
        return enum.getCode();
    }

    /**
     * @see org.springframework.context.MessageSourceResolvable#getArguments()
     */
    public Object[] getArguments() {
        return null;
    }
    
    /**
     * @see org.springframework.context.MessageSourceResolvable#getCodes()
     */
    public String[] getCodes() {
        return new String[] { getKey() };
    }
    
    /**
     * @see org.springframework.context.MessageSourceResolvable#getDefaultMessage()
     */
    public String getDefaultMessage() {
        return getLabel();
    }

    public String toString() {
        return ToStringBuilder.propertiesToString(this);
    }

}