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
package org.springframework.rules.functions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.rules.UnaryFunction;
import org.springframework.util.Assert;

/**
 * A chain of unary functions that evaluate their results in a ordered sequence.
 * <p>
 * For example, declaring
 * <code>new UnaryFunctionChain() { f1, f2, f3 }.evaluate(foo)</code> will
 * trigger the evaluation of <code>f1</code> first, it's result will be passed
 * to <code>f2</code> for evaluation, and f2's result will be passed to
 * <code>f3</code> for evaluation. The final f3 result will be returned to the
 * caller.
 * 
 * @author Keith Donald
 */
public class UnaryFunctionChain implements UnaryFunction {
    private Set functions = new LinkedHashSet();

    /**
     * Constructs a function chain with no initial members. It is expected the
     * client will call "add" to add individual predicates.
     */
    public UnaryFunctionChain() {

    }

    /**
     * Creates a UnaryFunctionChain composed of two functions.
     * 
     * @param function1
     *            the first function
     * @param function2
     *            the second function
     */
    public UnaryFunctionChain(UnaryFunction function1, UnaryFunction function2) {
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
    public UnaryFunctionChain(UnaryFunction[] functions) {
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
    public UnaryFunctionChain add(UnaryFunction function) {
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

    public String toString() {
        StringBuffer buf = new StringBuffer("chain(");
        for (Iterator i = iterator(); i.hasNext();) {
            buf.append(i.next());
            if (i.hasNext()) {
                buf.append("->");
            }
        }
        buf.append(")");
        return buf.toString();
    }
}