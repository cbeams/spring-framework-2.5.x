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

import org.springframework.rules.BinaryPredicate;
import org.springframework.rules.RelationalOperator;
import org.springframework.util.ObjectUtils;

/**
 * Predicate that tests object equality (not identity.)
 * 
 * @author Keith Donald
 */
public class EqualTo implements BinaryPredicate {
    private static final EqualTo INSTANCE = new EqualTo();

    public EqualTo() {
        super();
    }

    /**
     * Test if the two arguments are equal.
     * 
     * @param argument1
     *            the first argument
     * @param argument2
     *            the second argument
     * @return true if they are equal, false otherwise
     */
    public boolean test(Object argument1, Object argument2) {
        return ObjectUtils.nullSafeEquals(argument1, argument2);
    }

    public static BinaryPredicate instance() {
        return INSTANCE;
    }

    public String toString() {
        return RelationalOperator.EQUAL_TO.toString();
    }
}