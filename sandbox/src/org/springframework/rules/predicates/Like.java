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
 * A like predicate, supporting "starts with%", "%ends with", and "%contains%".
 * 
 * @author Keith Donald
 */
public class Like implements UnaryPredicate {
    public static final String STARTS_WITH = "like.startsWith";
    public static final String ENDS_WITH = "like.endsWith";
    public static final String CONTAINS = "like.contains";
    private String type;
    private String stringToMatch;

    public Like(String likeString) {
        if (likeString.startsWith("%")) {
            if (likeString.endsWith("%")) {
                this.type = CONTAINS;
            } else {
                this.type = ENDS_WITH;
            }
        } else if (likeString.endsWith("%")) {
            this.type = STARTS_WITH;
        } else {
            this.type = CONTAINS;
        }
        stringToMatch = StringUtils.deleteAny(likeString, "%");
    }

    /**
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        String value = String.valueOf(argument);
        if (type == STARTS_WITH) {
            return value.startsWith(stringToMatch);
        } else if (type == ENDS_WITH) {
            return value.endsWith(stringToMatch);
        } else {
            return value.indexOf(stringToMatch) != -1;
        }
    }

    public String getType() {
        return type;
    }

    public String getString() {
        return stringToMatch;
    }

}