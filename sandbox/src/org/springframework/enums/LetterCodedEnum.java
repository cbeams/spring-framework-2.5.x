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

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public abstract class LetterCodedEnum extends AbstractCodedEnum {
    protected LetterCodedEnum(char code) {
        super(new Character(code));
        Assert.isTrue(Character.isLetter(code), "The code " + code
                + " is invalid; it must be a letter.");
    }

    protected LetterCodedEnum(char code, String label) {
        super(new Character(code), label);
        Assert.isTrue(Character.isLetter(code), "The code " + code
                + " is invalid; it must be a letter.");
    }

    public char getLetterCode() {
        return ((Character)getCode()).charValue();
    }

}