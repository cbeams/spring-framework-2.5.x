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

/**
 * A TargetSource is used to obtain the current "target" of 
 * an AOP invocation, which will be invoked via reflection if no
 * around advice chooses to end the interceptor chain itself. 
 * <br>If a TargetSource is "static", it will always
 * return the same target, allowing optimizations in the AOP framework.
 * Dynamic target sources can support pooling, hot swapping etc.
 * <br>Application developers don't usually need to work with TargetSources
 * directly: this is an AOP framework interface.
 * @author Rod Johnson
 * @version $Id: TargetSource.java,v 1.4 2004-03-18 02:46:07 trisberg Exp $
 */
public interface TargetSource {
	
	Class getTargetClass();
	
	boolean isStatic();
	
	Object getTarget() throws Exception;
	
	void releaseTarget(Object target) throws Exception;

}
