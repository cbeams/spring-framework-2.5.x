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

/**
 * @author Keith Donald
 */
public class LessThanEqualTo extends OperatorBinaryPredicate implements
        BinaryPredicate {

    public LessThanEqualTo() {
        super();
    }

    public LessThanEqualTo(Comparator comparator) {
        super(comparator);
    }

    protected boolean evaluateOperatorResult(int result) {
        return result <= 0;
    }

    public static BinaryPredicate instance() {
        return INSTANCE;
    }

    private static final LessThanEqualTo INSTANCE = new LessThanEqualTo();

}