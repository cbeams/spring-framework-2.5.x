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