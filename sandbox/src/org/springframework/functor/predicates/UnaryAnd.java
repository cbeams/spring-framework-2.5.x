/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.functor.predicates;

import java.util.Iterator;

import org.springframework.functor.UnaryPredicate;

/**
 * A "and" compound predicate (aka conjunction).
 * 
 * @author Keith Donald
 */
public class UnaryAnd
    extends CompoundUnaryPredicate
    implements UnaryPredicate {

    /**
     * Creates a empty UnaryAnd conjunction.
     */
    public UnaryAnd() {
        super();
    }

    /**
     * "Ands" two predicates.
     * 
     * @param predicate1
     *            The first predicate.
     * @param predicate2
     *            The second predicate.
     */
    public UnaryAnd(UnaryPredicate predicate1, UnaryPredicate predicate2) {
        super(predicate1, predicate2);
    }

    /**
     * "Ands" the specified predicates.
     * 
     * @param predicates
     *            The predicates
     */
    public UnaryAnd(UnaryPredicate[] predicates) {
        super(predicates);
    }

    /**
     * Tests if all of the predicates aggregated by this compound predicate
     * return <code>true</code> when evaulating this argument.
     * 
     * @see org.springframework.functor.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        for (Iterator i = iterator(); i.hasNext();) {
            if (!((UnaryPredicate)i.next()).test(argument)) {
                return false;
            }
        }
        return true;
    }

}