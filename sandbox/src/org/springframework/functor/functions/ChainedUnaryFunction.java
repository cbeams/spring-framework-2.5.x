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
package org.springframework.functor.functions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.functor.UnaryFunction;
import org.springframework.util.Assert;

/**
 * A chain of unary functions that evaluate their results in a ordered sequence.
 * 
 * @author Keith Donald
 */
public class ChainedUnaryFunction implements UnaryFunction {
    private Set functions = new LinkedHashSet();

    /**
     * Constructs a function chain with no initial members. It is expected the
     * client will call "add" to add individual predicates.
     */
    public ChainedUnaryFunction() {

    }

    /**
     * Creates a UnaryFunctionChain composed of two functions.
     * 
     * @param function1
     *            the first function
     * @param function2
     *            the second function
     */
    public ChainedUnaryFunction(UnaryFunction function1, UnaryFunction function2) {
        Assert.isTrue(function1 != null && function2 != null);
        functions.add(function1);
        functions.add(function2);
    }

    /**
     * Creates a UnaryFunctionChain composed of the ordered array of functions.
     * 
     * @param functions
     *            the aggregated functions
     */
    public ChainedUnaryFunction(UnaryFunction[] functions) {
        this.functions.addAll(Arrays.asList(functions));
    }

    /**
     * Add the specified function to the set of functions aggregated by this
     * function chain.
     * 
     * @param function
     *            the function to add
     * @return A reference to this, to support easy chaining.
     */
    public ChainedUnaryFunction add(UnaryFunction function) {
        this.functions.add(function);
        return this;
    }

    /**
     * Return an iterator over the aggregated predicates.
     * 
     * @return An iterator
     */
    public Iterator iterator() {
        return functions.iterator();
    }

    public Object evaluate(Object argument) {
        Iterator i = iterator();
        Assert.isTrue(i.hasNext(), "No functions to evaluate");
        Object result = argument;
        while (i.hasNext()) {
            UnaryFunction f = (UnaryFunction)i.next();
            result = f.evaluate(result);
        }
        return result;
    }

}