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
 * @author Keith Donald
 */
public class ParameterizedBinaryPredicate implements UnaryPredicate {
    private BinaryPredicate predicate;
    private Object constant;

    public ParameterizedBinaryPredicate(BinaryPredicate predicate,
            Object constantToBind) {
        this.predicate = predicate;
        this.constant = constantToBind;
    }

    public boolean evaluate(Object value) {
        return predicate.evaluate(value, this.constant);
    }

}