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
 * A TargetSource is used to obtain the current "target" of  an AOP invocation,
 * which will be invoked via reflection if no around advice chooses to end the
 * interceptor chain itself.
 *
 * <p>If a TargetSource is "static", it will always return the same target,
 * allowing optimizations in the AOP framework. Dynamic target sources can
 * support pooling, hot swapping, etc.
 *
 * <p>Application developers don't usually need to work with TargetSources
 * directly: This is an AOP framework interface.
 *
 * @author Rod Johnson
 */
public interface TargetSource {

	/**
	 * Return the type of targets returned by this TargetSource.
	 * Can return null, although certain usages of a TargetSource
	 * might just work with a predetermined target class.
	 */
	Class getTargetClass();
	
	/**
	 * Will all calls to getTarget() return the same object?
	 * In that case, there will be no need to invoke releaseTarget(),
	 * and the AOP framework can cache the return value of getTarget().
	 * @return whether the target is immutable.
	 */
	boolean isStatic();
	
	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * @return the target object, whicch contains the joinpoint
	 * @throws Exception if the target object can't be resolved
	 */
	Object getTarget() throws Exception;
	
	/**
	 * Release the given target object obtained from the getTarget() method.
	 * @param target object obtained from a call to getTarget()
	 * @throws Exception if the object can't be released
	 * @see #getTarget
	 */
	void releaseTarget(Object target) throws Exception;

}
