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

package org.springframework.aop.support.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.weaver.tools.PointcutExpression;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.ClassFilters;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.TypePatternClassFilter;
import org.springframework.util.ReflectionUtils;

/**
 * Factory that can create Spring AOP Advisors given AspectJ classes from classes honouring
 * the AspectJ 5 annotation syntax, using reflection to invoke advice methods.
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 1.3
 */
public class ReflectiveAtAspectJAdvisorFactory extends AbstractAtAspectJAdvisorFactory {
	
	/**
	 * Create Spring Advisors for all At AspectJ methods on the given aspect instance.
	 * @param aspectInstance
	 * @return a list of advisors for this class
	 */
	public List<Advisor> getAdvisors(final Object aspectInstance) {
		final List<Advisor> advisors = new LinkedList<Advisor>();
		final AspectInstanceFactory aif = new SingletonAspectInstanceFactory(aspectInstance);
		ReflectionUtils.doWithMethods(aspectInstance.getClass(), new ReflectionUtils.MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				PointcutAdvisor pa = getAdvisor(aspectInstance.getClass(), m, aif);
				if (pa != null) {
					advisors.add(pa);
				}
			}
		}, ReflectionUtils.DECLARED_METHODS);
		
		//	Find introduction fields
		for (Field f : aspectInstance.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
				Advisor a = getDeclareParentsAdvisor(f, aspectInstance);
				if (a != null) {
					advisors.add(a);
				}
			}
		}
		
		return advisors;
	}
	
	public PointcutAdvisor getAdvisor(final Object aspectInstance, Method candidateAspectJAdviceMethod) {
		return getAdvisor(aspectInstance.getClass(), candidateAspectJAdviceMethod, 
				new SingletonAspectInstanceFactory(aspectInstance));
	}
	
	/**
	 * Resulting advisors will need to be evaluated for targets.
	 * @param introductionInstance
	 * @return null if not an advisor
	 */
	private Advisor getDeclareParentsAdvisor(Field f, Object aspectInstance) {
		DeclareParents declareParents = (DeclareParents) f.getAnnotation(DeclareParents.class);
		if (declareParents == null) {
			// Not an introduction field
			return null;
		}
		
		// Work out where it matches, with the ClassFilter
		final Class[] interfaces = new Class[] { f.getType() };
		
		ClassFilter typePatternFilter = new TypePatternClassFilter(declareParents.value());
		ClassFilter exclusion = new ClassFilter() {
			public boolean matches(Class clazz) {
				for (Class<?> introducedInterface : interfaces) {
					if (introducedInterface.isAssignableFrom(clazz)) {
						return false;
					}
				}
				return true;
			}
		};
		final ClassFilter classFilter = ClassFilters.intersection(typePatternFilter, exclusion);

		// Try to instantiate mixin instance and do delegation
		Object introductionInstance;
		try {
			introductionInstance = f.get(aspectInstance);
		} 
		catch (IllegalArgumentException ex) {
			throw new AspectException("Cannot evaluate introduction field " + f, ex);
		} 
		catch (IllegalAccessException ex) {
			throw new AspectException("Cannot evaluate introduction field " + f, ex);
		}
		
		IntroductionAdvisor ia = new DelegatingIntroductionAdvisor(interfaces, classFilter, introductionInstance);//introductionInstance);
		return ia;
	}
	
	
	/**
	 * 
	 * @param candidateAspectClass
	 * @param candidateAspectJAdviceMethod
	 * @param aif
	 * @return null if the method is not an AspectJ advice method or if it is a pointcut
	 * that will be used by other advice but will not create a Springt advice in its own right
	 */
	public PointcutAdvisor getAdvisor(Class<?> candidateAspectClass, Method candidateAspectJAdviceMethod, AspectInstanceFactory aif) {
		AspectJAnnotation<?> aspectJAnnotation = AbstractAtAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod,candidateAspectClass);
		
		if (aspectJAnnotation == null) {
			return null;
		}
		
		// If we get here, we know we have an AspectJ method. 
		// Check that it's an AspectJ-annotated class
		if (!isAspect(candidateAspectClass)) {
			throw new AopConfigException("Advice must be declared inside an aspect type: " +
					"Offending method '" + candidateAspectJAdviceMethod + "' in class " + candidateAspectClass.getName());
		}
		
		log.debug("Found AspectJ method " + candidateAspectJAdviceMethod);

		String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAspectJAdviceMethod,candidateAspectClass);
		AspectJExpressionPointcut ajexp = createPointcutExpression(candidateAspectJAdviceMethod,candidateAspectClass,argNames);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());

		Advice springAdvice;	
	
		switch(aspectJAnnotation.getAnnotationType()) {
		case AtBefore:
			springAdvice = new AspectJMethodBeforeAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);	
			break;
		case AtAfter:
			springAdvice = new AspectJAfterAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);				
			break;
		case AtAfterReturning:
			springAdvice = new AspectJAfterReturningAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);				
			break;
		case AtAfterThrowing:
			springAdvice = new AspectJAfterThrowingAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);				
			break;
		case AtAround:
			springAdvice = new AspectJAroundAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);		
			break;
		case AtPointcut:
			log.debug("Processing pointcut '" + candidateAspectJAdviceMethod.getName() + "'");
			return null;
		default:
			throw new UnsupportedOperationException("Unsupported advice type on method " + candidateAspectJAdviceMethod);			
		}
		
		return new DefaultPointcutAdvisor(ajexp, springAdvice);
	}
	

	/**
	 * Superclass for Spring Advices wrapping an AspectJ aspect
	 * or annotated advice method
	 */
	private abstract class AbstractAspectJAdvice {
		
		protected final Method aspectJAdviceMethod;
		
		private final PointcutExpression pointcutExpression;
		
		private final AspectInstanceFactory aif;
		
		public AbstractAspectJAdvice(Method aspectJAdviceMethod, PointcutExpression pointcutExpression, AspectInstanceFactory aif) {
			this.aspectJAdviceMethod = aspectJAdviceMethod;
			this.pointcutExpression = pointcutExpression;
			this.aif = aif;
		}
		
		/**
		 * Take the arguments in the call to the advised method and output a set of arguments
		 * to the advice method
		 * @param availableArgs arguments to the method being invoked
		 * @return the empty array if there are no arguments
		 */
		protected Object[] argBinding(Object[] availableArgs) {
			// TODO may not bind all of them
			//pointcutExpression.
			
			// TODO wrong way to get target
			//ShadowMatch sm = pointcutExpression.matchesMethodExecution(aspectJAdviceMethod);
			
			return availableArgs;
		}
		
		
		/**
		 * Invoke the advice method
		 * @param argsInCall arguments to the method being invoked, which is adviced by the
		 * advice
		 * @return
		 * @throws Throwable
		 */
		protected Object invokeAdviceMethod(Object[] argsInCall) throws Throwable {
			return invokeAdviceMethodWithGivenArgs(argBinding(argsInCall));
		}
		
		protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
			// TODO really a hack
			if (aspectJAdviceMethod.getParameterTypes().length == 0) {
				args = null;
			}
			
			try {
				// TODO AOPutils.InvokeJoinpointUsingReflection
				return aspectJAdviceMethod.invoke(aif.getAspectInstance(), args);
			}
			catch (IllegalArgumentException ex) {
				throw new AopConfigException("Mismatch on arguments to advice method " + aspectJAdviceMethod + "; " +
						"pointcut expression='" + pointcutExpression.getPointcutExpression() + "'", ex);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}
	
	
	/**
	 * Callback interface implemented to provide instance of the AspectJ aspect on which the
	 * aspect method exists
	 */
	public interface AspectInstanceFactory {
		Object getAspectInstance();
	}
	
	/**
	 * Convenient implementation of AspectJInstanceFactory that wraps a singleton instance
	 *
	 */
	public static class SingletonAspectInstanceFactory implements AspectInstanceFactory {
		private final Object aspectInstance;
		public SingletonAspectInstanceFactory(Object aspectInstance) {
			this.aspectInstance = aspectInstance;
		}
		public Object getAspectInstance() {
			return aspectInstance;
		}
	}
	
	
	private class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice {
		
		public AspectJMethodBeforeAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJBeforeAdviceMethod, pe, aif);
		}
		
		public void before(Method method, Object[] args, Object target) throws Throwable {
			invokeAdviceMethod(args);
		}
	}
	
	private class AspectJAfterAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
		
		public AspectJAfterAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJBeforeAdviceMethod, pe, aif);
		}
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			try {
				return mi.proceed();
			}
			finally {
				invokeAdviceMethod(mi.getArguments());
			}
		}
	}
	
	private class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice {
		
		public AspectJAfterReturningAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJBeforeAdviceMethod, pe, aif);
		}
		
		public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
			invokeAdviceMethod(args);
		}
	}
	
	private class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
		
		public AspectJAfterThrowingAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJBeforeAdviceMethod, pe, aif);
		}
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			try {
				return mi.proceed();
			}
			catch (Throwable t) {
				// TODO need to check arguments
				invokeAdviceMethod(mi.getArguments());
				throw t;
			}
		}
	}
	
	private class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
		public AspectJAroundAdvice(Method aspectJAroundAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJAroundAdviceMethod, pe, aif);
		}

		public Object invoke(MethodInvocation mi) throws Throwable {
			ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(mi);
			Object[] formals = argBinding(mi.getArguments());
			if (formals == null) {
				formals = new Object[0];
			}
			Object[] args = new Object[formals.length + 1];
			args[0] = pjp;
			ReflectiveMethodInvocation invocation = (ReflectiveMethodInvocation) mi;

			String[] argNames = parameterNameDiscoverer.getParameterNames(aspectJAdviceMethod,aspectJAdviceMethod.getDeclaringClass());
			if (argNames == null) {
				// basic mapping applies
				System.arraycopy(formals, 0, args, 1, formals.length);				
			} else {
				// map based on bindings
				Map bindingMap = invocation.getUserAttributes();
				for (int i = 1; i < args.length; i++) {
					// should be made more robust, works for now...
					args[i] = bindingMap.get(argNames[i-1]);
				}
			}
					
			return invokeAdviceMethodWithGivenArgs(args);
		}
	}
}
