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
package org.springframework.enums.support;

import org.springframework.enums.AbstractCodedEnum;

/**
 * @author Keith Donald
 */
public class GenericLabeledCodedEnum extends AbstractCodedEnum {
    private String type;

    public GenericLabeledCodedEnum(String type, int code, String label) {
        super(new Integer(code), label);
        this.type = type;
    }

    public GenericLabeledCodedEnum(String type, char code, String label) {
        super(new Character(code), label);
        this.type = type;
    }

    public GenericLabeledCodedEnum(String type, String code, String label) {
        super(code, label);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}