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
package org.springframework.functor;

/**
 * A unary function is a expression that evaluates one argument and returns a
 * result.
 * <p>
 * <p>
 * A unary function is a function object that evaluates a single argument
 * against some expression. For example, a "StringLength" unary function might
 * return the length of an argument's string form.
 * 
 * @author Keith Donald
 */
public interface UnaryFunction {

    /**
     * Evaluate the function with the provided argument, returning the result.
     * 
     * @param argument
     *            the argument
     * @return the function return value
     */
    public Object evaluate(Object argument);
}