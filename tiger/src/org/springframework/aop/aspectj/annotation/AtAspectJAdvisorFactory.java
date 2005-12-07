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

package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AopConfigException;

/**
 * Interface for factories that can create Spring AOP Advisors from classes
 * annotated with AspectJ annotation syntax.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public interface AtAspectJAdvisorFactory {
	
	boolean isAspect(Class<?> clazz);
	
	/**
	 * Is it a valid aspect class?
	 * @param aspectClass
	 * @throws AopConfigException
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * Create Spring Advisors for all At AspectJ methods on the given aspect instance.
	 * @param aif prevents eager instantiation. Should cache.
	 * @return a list of advisors for this class
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aif);
	
	
	/**
	 * 
	 * @param aspectClass
	 * @param aif
	 * @return null if the method is not an AspectJ advice method
	 */
	Advisor getAdvisor(Method candidateAspectJAdviceMethod, MetadataAwareAspectInstanceFactory aif);
	
	Advice getAdvice(Method candidateAspectJAdviceMethod, MetadataAwareAspectInstanceFactory aif);

}