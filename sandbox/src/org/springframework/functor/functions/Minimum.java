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
package org.springframework.functor.functions;

import org.springframework.functor.BinaryFunction;

/**
 * @author  Keith Donald
 */
public class Minimum implements BinaryFunction {
    private static final Minimum INSTANCE = new Minimum();
    
    public Minimum() {
        super();
    }

    /**
     * @see org.springframework.functor.BinaryFunction#evaluate(java.lang.Object, java.lang.Object)
     */
    public Object evaluate(Object value1, Object value2) {
        int compare = ((Comparable)value1).compareTo(value2);
        if (compare == 0) {
            return value2;
        } else if (compare > 0) {
            return value2;
        } else {
            return value1;
        }
    }
    
    public static final Minimum instance() {
        return INSTANCE;
    }

}
