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
package org.springframework.rules;

import java.util.Collection;
import java.util.Iterator;

/**
 * Convenience utility class which provides a number of algorithms involving
 * functor objects such as predicates.
 * 
 * @author Keith Donald
 * @version $Id: Algorithms.java,v 1.2 2004-04-11 19:58:00 kdonald Exp $
 */
public abstract class Algorithms {
    
    // static utility class
    private Algorithms() {
        
    }
    
    /**
     * Find the first element in the collection matching the specified unary
     * predicate.
     * 
     * @param collection
     *            the collection
     * @param predicate
     *            the predicate
     * @return The first object match, or null if no match
     */
    public static Object findFirst(Collection collection,
            UnaryPredicate predicate) {
        for (Iterator i = collection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (predicate.test(o)) {
                return o;
            }
        }
        return null;
    }
    
    public static void forEach(Collection collection, UnaryProcedure callback) {
        forEach(collection.iterator(), callback);
    }

    public static void forEach(Iterator it, UnaryProcedure callback) {
        while (it.hasNext()) {
            callback.run(it.next());
        }
    }
    
}