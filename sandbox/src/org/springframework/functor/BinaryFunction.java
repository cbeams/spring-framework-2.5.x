/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.functor;

/**
 * A function object that evaluates two arguments and returns a single result.
 * <p>
 * <p>
 * A binary function evaluates two arguments against some expression. For
 * example, a "Maximum" binary function might return the max of two numbers.
 * 
 * @author Keith Donald
 */
public interface BinaryFunction {

    /**
     * Evaluate the function with the provided arguments, returning the result.
     * 
     * @param argument1
     *            the first argument
     * @param argument2
     *            the second argument
     * @return the function return value
     */
    public Object evaluate(Object argument1, Object argument2);
}