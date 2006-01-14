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
 
package org.springframework.aop.aspectj.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.DefaultLockable;
import org.springframework.aop.framework.Lockable;

/**
 * Demonstrates introductions, AspectJ annotation style.
 * <p>
 * <b>This is incomplete as of Spring AOP 2.0 M1. It is
 * possible that introductions may be considered out of scope
 * altogether for Spring 2.0.</b>
 * <p>
 * @author Rod Johnson
 * @since 2.0
 */
@Aspect
public class MakeLockable {
	
	@DeclareParents(value = "org.springframework.aop.aspectj.annotation.*",
			defaultImpl=DefaultLockable.class)
	public static Lockable mixin;
	
	@Before("execution(* set*(*))")
	//@Before(value="execution(* set*(*)) && this(mixin)", argNames="mixin")
	public void checkNotLocked(JoinPoint jp) { 
			//Lockable mixin) { // Bind to arg
		
		// TODO this is work in progress, an example only to indicate a possible Spring implementation strategy
		// This is NOT the correct AspectJ syntax approach
		Lockable mixin = (Lockable) AopContext.currentProxy(); 
			//(Lockable) jp.getThis();
		if (mixin.locked()) {
			throw new IllegalStateException();
		}
	}

}

/*
 * 
 * public aspect MakeLockable {
 *   
 *  declare parents org....* implements Lockable;
 *   
 *  private boolean Lockable.locked;
	
 *	public void Lockable.lock() {
		this.locked = true;
	}
	
 *	public void Lockable.unlock() {
		this.locked = false;
	}
	
 *	public boolean Lockable.locked() {
		return this.locked;
	}
 * 
 * 
 * }
 */
