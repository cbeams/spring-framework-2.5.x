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
 *
 * Created on 24 Jul 2006 by Adrian Colyer
 */
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * A specialized type of MethodMatcher that takes into account introductions when
 * matching methods. If there are no introductions on the target class, a method
 * matcher may be able to optimize matching more effectively for example.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	/**
	 * Perform static checking. If this returns false, or if the isRuntime() method
	 * returns false, no runtime check will be made.
	 * @param method the candidate method
	 * @param targetClass target class (may be <code>null</code>, in which case the candidate
	 * class must be taken to be the method's declaring class)
	 * @param beanHasIntroductions true if the bean on whose behalf we are asking is the
	 * subject on one or more introductions, and false otherwise
	 * @return whether or not this method matches statically
	 */
	boolean matches(Method method, Class targetClass, boolean beanHasIntroductions);
}
