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

import java.lang.reflect.Method;

/**
 * Advice for after returning advice. This means on normal return,
 * not exception.
 * @author Rod Johnson
 * @version $Id: MethodAfterReturningAdvice.java,v 1.2 2004-03-18 02:46:07 trisberg Exp $
 */
public interface MethodAfterReturningAdvice extends AfterReturningAdvice {
	
	void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable;

}
