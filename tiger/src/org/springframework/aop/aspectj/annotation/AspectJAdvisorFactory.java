/*
 * Copyright 2002-2006 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;

/**
 * Interface for factories that can create Spring AOP Advisors from classes
 * annotated with AspectJ annotation syntax.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public interface AspectJAdvisorFactory {
	
	/**
	 * Return whether or not the given class is an aspect,
	 * as reported by AspectJ's AJTypeSystem. Will simply return false
	 * if the supposed aspect is invalid (such as an extension of
	 * a concrete aspect class). Will return true for some aspects
	 * that Spring AOP cannot process, such as those with unsupported
	 * instantiation models. Use the validate() method to handle
	 * these cases if necessary
	 * @param clazz supposed annotation-style AspectJ class
	 * @return whether or not this class is recognized by AspectJ as
	 * an aspect class
	 */
	boolean isAspect(Class<?> clazz);
	
	/**
	 * Is the given class a valid aspect class?
	 * @param aspectClass supposed AspectJ annotation-style class
	 * to validate
	 * @throws AopConfigException if the class is an invalid aspect
	 * (which can never be legal)
	 * @throws NotAnAtAspectException if the class is not an aspect at all
	 * (which may or may be legal, depending on the context)
	 */
	void validate(Class<?> aspectClass) throws AopConfigException, NotAnAtAspectException;

	/**
	 * Create Spring Advisors for all At AspectJ methods on the given aspect instance.
	 * @param aif prevents eager instantiation. Should cache.
	 * @return a list of advisors for this class
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aif);
	
	/**
	 * TODO: javadoc
	 * @return <code>null</code> if the method is not an AspectJ advice method
	 */
	Advisor getAdvisor(Method candidateAspectJAdviceMethod,
			MetadataAwareAspectInstanceFactory aif, int declarationOrderInAspect, String aspectName);
	
	/**
	 * TODO: javadoc
	 * @return <code>null</code> if the method is not an AspectJ advice method
	 */
	Advice getAdvice(Method candidateAspectJAdviceMethod, AspectJExpressionPointcut pointcut,
			MetadataAwareAspectInstanceFactory aif, int declarationOrderInAspect, String aspectName);

}
