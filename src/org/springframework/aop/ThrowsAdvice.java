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

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * Tag interface for throws advice.
 * There aren't any methods on this interface, as methods are invoked by reflection.
 * Implementing classes should implement methods of the form:
 * <code>
 * afterThrowing([Method], [args], [target], Throwable subclass) 
 * </code>
 * The first three arguments are optional, and only useful if
 * we want further information about the joinpoint, as in AspectJ
 * <b>after throwing</b> advice.
 * @author Rod Johnson
 */
public interface ThrowsAdvice extends Advice {

}
