/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.functor;

/**
 * A binary function is a expression that evaluates two arguments and returns a
 * single result.
 * <p>
 * <p>
 * A binary function is a function object that evaluates two arguments with some
 * expression. For example, a "Maximum" binary function might return the max of
 * two numbers.
 * 
 * @author Keith Donald
 */
public interface BinaryFunction {
    
    /**
     * Evaluate the function with the provided arguments, returning the result.
     * 
     * @param value
     *            the first argument
     * @param value
     *            the second argument
     * @return the function return value
     */
    public Object evaluate(Object value1, Object value2);
}