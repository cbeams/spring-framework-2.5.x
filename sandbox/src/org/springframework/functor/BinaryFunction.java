/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.functor;

/**
 * @author  Keith Donald
 */
public interface BinaryFunction {
    public Object evaluate(Object value1, Object value2);
}
