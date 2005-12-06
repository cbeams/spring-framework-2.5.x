/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Interface to discover parameter names for methods
 * and constructors. This is not always possible, but various
 * strategies are available to try, such as looking for debug
 * information that may have been emitted at compile time, and
 * looking for argname annotation values optionally accompanying AspectJ
 * annotated methods.
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public interface ParameterNameDiscoverer {
	
	/**
	 * Return parameter names or null for this method if
	 * they cannot be determined
	 * @param m method to find parameter names for
	 * @param clazz leaf class to look at in hierarchy
	 * @return null if the parameter names cannot be resolved,
	 * an array of parameter names if they can be
	 */
	String[] getParameterNames(Method m, Class clazz);
	
	/**
	 * Return parameter names or null for this constructor if
	 * they cannot be determined
	 * @param ctor constructor to find parameter names for
	 * @return null if the parameter names cannot be resolved,
	 * an array of parameter names if they can be
	 */
	String[] getParameterNames(Constructor ctor);

}
