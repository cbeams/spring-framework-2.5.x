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

package org.springframework.aop.framework;

import java.util.List;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Factory for method invocations.
 * @author Rod Johnson
 * @version $Id: MethodInvocationFactory.java,v 1.7 2004-03-18 02:46:05 trisberg Exp $
 */
public interface MethodInvocationFactory {
	
	MethodInvocation getMethodInvocation(Object proxy, Method method,
								Class targetClass, Object target, Object[] args, 
								List interceptorsAndDynamicInterceptionAdvice, AdvisedSupport advised);
	
	void release(MethodInvocation mi);
	

}
