/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.functor.predicates;

import java.util.Comparator;

import org.springframework.functor.BinaryPredicate;

public abstract class OperatorBinaryPredicate implements BinaryPredicate {
    private Comparator comparator;
    
    public OperatorBinaryPredicate() {
        
    }
    
    public OperatorBinaryPredicate(Comparator comparator) {
        this.comparator = comparator;
    }
    
    public boolean evaluate(Object value1, Object value2) {
        if (comparator != null) {
            return evaluateOperatorResult(this.comparator.compare(value1, value2));
        } else {
            Comparable c1 = (Comparable)value1;
            Comparable c2 = (Comparable)value2;
            return evaluateOperatorResult(c1.compareTo(c2));
        }
    }
    
    public abstract boolean evaluateOperatorResult(int result);

}
