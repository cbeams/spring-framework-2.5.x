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
package org.springframework.functor.predicates;

import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.UnaryPredicate;

/**
 * A unary predicate adapting a binary predicate that uses a parameterized
 * constant value as the second argument when testing.
 * 
 * @author Keith Donald
 */
public class ParameterizedBinaryPredicate implements UnaryPredicate {
    private BinaryPredicate predicate;
    private Object parameter;

    /**
     * Creates a ParameterizedBinaryPredicate that binds the provided parameter
     * constant as the second argument to the predicate during tests.
     * 
     * @param predicate
     *            The binary predicate to adapt as a unary predicate
     * @param parameter
     *            The constant parameter value
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            Object parameter) {
        this.predicate = predicate;
        this.parameter = parameter;
    }

    /**
     * Tests the wrapped binary predicate with the variable argument value,
     * passing in the parameter constant as the second argument.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object value) {
        return predicate.test(value, this.parameter);
    }

}