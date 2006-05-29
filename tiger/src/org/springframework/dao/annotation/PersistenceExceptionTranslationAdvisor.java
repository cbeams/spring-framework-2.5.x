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

package org.springframework.dao.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * Spring AOP exception translation access for use at Repository or DAO layer level.
 *
 * @author Rod Johnson
 * @since 2.0
 */
class PersistenceExceptionTranslationAdvisor extends DefaultPointcutAdvisor {

	public PersistenceExceptionTranslationAdvisor(
			PersistenceExceptionTranslator persistenceExceptionTranslator,
			Class<? extends Annotation> repositoryAnnotationType) {

		super(
				new RepositoryAnnotationMatchingPointcut(repositoryAnnotationType),
				new PersistenceExceptionTranslationInterceptor(persistenceExceptionTranslator)
		);		
	}


	private static class RepositoryAnnotationMatchingPointcut extends StaticMethodMatcherPointcut {

		public RepositoryAnnotationMatchingPointcut(final Class<? extends Annotation> repositoryAnnotationType) {
			setClassFilter(new ClassFilter() {
				public boolean matches(Class clazz) {
					return clazz.isAnnotationPresent(repositoryAnnotationType);
				}
			});
		}

		public boolean matches(Method method, Class targetClass) {
			return true;
		}
	}


	private static class PersistenceExceptionTranslationInterceptor implements MethodInterceptor {
		
		private final PersistenceExceptionTranslator persistenceExceptionTranslator;
		
		public PersistenceExceptionTranslationInterceptor(PersistenceExceptionTranslator persistenceExceptionTranslator) {
			this.persistenceExceptionTranslator = persistenceExceptionTranslator;
		}
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			try {
				return mi.proceed();
			}
			catch (RuntimeException ex) {
				// Let it throw raw if the type of the exception is on the throws clause of the method
				for (Class<?> exceptionClass : mi.getMethod().getExceptionTypes()) {
					if (exceptionClass.isInstance(ex)) {
						throw ex;
					}
				}
				throw DataAccessUtils.translateIfNecessary(ex, this.persistenceExceptionTranslator);
			}
		}
	}

}
