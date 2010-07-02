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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractAnnotationPointcutTests extends AbstractPointcutTests {

	//private List<Advised> proxies = new LinkedList<Advised>();
	
	private Method lastMethodIdentifyingPointcut;
	
	@Override
	protected void setUp() throws Exception {
		this.lastMethodIdentifyingPointcut = null;
		
		// Look for all aspect fields in the class
		// create a proxy for them that will identify proxy. Can only call one one time
		ReflectionUtils.doWithFields(this.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				AjType ajType = AjTypeSystem.getAjType(field.getType());
				if (ajType.isAspect()) {
					System.out.println(field + " is aspect, type=" + field.getType());
					// Assign to it
					field.setAccessible(true);
					Object value = createPointcutMethodIdentifyingProxyForAspect(field.getType());
					field.set(AbstractAnnotationPointcutTests.this, value);
				}
			}
		});
	}
	
	private Object createPointcutMethodIdentifyingProxyForAspect(Class<?> aspectClass) {
		System.out.println("create tracking proxy for " + aspectClass);
		ProxyFactory pf = new ProxyFactory(BeanUtils.instantiateClass(aspectClass));
		pf.setProxyTargetClass(true);
		pf.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation mi) throws Throwable {
				AbstractAnnotationPointcutTests.this.lastMethodIdentifyingPointcut = mi.getMethod();
				return getPrimitiveReturnIfNecessary(mi);
			}
		});
		return pf.getProxy();
	}

	protected Object getMatchingProxyFromAnnotatedMethod(Object target, 
			final Class pointcutClass, Method candidateAspectJAdviceMethod) {
		this.lastMethodIdentifyingPointcut = candidateAspectJAdviceMethod;
		AbstractAspectJAdvisorFactory.AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory
				.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			throw new IllegalStateException(candidateAspectJAdviceMethod + " is not annotated");
		}
		
		String[] argNames  = new String[0]; // ajAnnotation.getArgNames()
		Class<?>[] parameterTypes = new Class[0];//candidateAspectJAdviceMethod.getParameterTypes();
		return getMatchingProxy(target, pointcutClass, aspectJAnnotation.getPointcutExpression(),
				argNames, parameterTypes);
	}
	
	protected Object getMatchingProxy(Object target) {
		if (this.lastMethodIdentifyingPointcut == null) {
			throw new IllegalStateException("No method identified");
		}
		return getMatchingProxyFromAnnotatedMethod(target, 
				this.lastMethodIdentifyingPointcut.getDeclaringClass(),
				this.lastMethodIdentifyingPointcut);
	}

}
