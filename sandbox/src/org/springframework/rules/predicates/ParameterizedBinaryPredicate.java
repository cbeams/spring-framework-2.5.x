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
import org.springframework.rules.UnaryPredicate;

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
     * Convenience constructor for <code>short</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            short number) {
        this(predicate, new Short(number));
    }

    /**
     * Convenience constructor for <code>byte</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            byte b) {
        this(predicate, new Byte(b));
    }

    /**
     * Convenience constructor for <code>integer</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            int number) {
        this(predicate, new Integer(number));
    }

    /**
     * Convenience constructor for <code>float</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            float number) {
        this(predicate, new Float(number));
    }

    /**
     * Convenience constructor for <code>double</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            double number) {
        this(predicate, new Double(number));
    }

    /**
     * Convenience constructor for <code>boolean</code> parameters.
     */
    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            boolean bool) {
        this(predicate, (bool ? Boolean.TRUE : Boolean.FALSE));
    }
    
    public Object getParameter() {
        return parameter;
    }
    
    public BinaryPredicate getPredicate() {
        return predicate;
    }

    /**
     * Tests the wrapped binary predicate with the variable argument value,
     * passing in the parameter constant as the second argument.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object value) {
        return predicate.test(value, this.parameter);
    }

    public String toString() {
        return predicate.toString() + " " + getParameter();
    }

}