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
package org.springframework.rules.predicates;

import org.springframework.rules.UnaryPredicate;
import org.springframework.util.StringUtils;

/**
 * Validates a required property. Required is defined as non-null and, if the
 * object is a string, not empty and not blank.
 * 
 * @author Keith Donald
 */
public class Required implements UnaryPredicate {
    private static final Required instance = new Required();

    /**
     * Tests if this argument is present (non-null, not-empty, not blank)
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        if (argument != null) {
            if (argument instanceof String) {
                if (StringUtils.hasText((String)argument)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static UnaryPredicate instance() {
        return instance;
    }
    
    public String toString() {
        return "required";
    }

}