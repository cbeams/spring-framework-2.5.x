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
package org.springframework.functor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Keith Donald
 */
public class UnaryOr extends CompoundUnaryPredicate implements UnaryPredicate {

    public Set predicates = new HashSet();

    public UnaryOr() {
        super();
    }

    public UnaryOr(UnaryPredicate predicate1, UnaryPredicate predicate2) {
        super(predicate1, predicate2);
    }

    public UnaryOr(UnaryPredicate[] predicates) {
        super(predicates);
    }

    public boolean evaluate(Object value) {
        for (Iterator i = iterator(); i.hasNext();) {
            if (!((UnaryPredicate)i.next()).evaluate(value)) {
                return true;
            }
        }
        return false;
    }

}