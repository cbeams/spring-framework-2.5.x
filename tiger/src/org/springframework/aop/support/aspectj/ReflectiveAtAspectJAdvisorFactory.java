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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
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
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Factory that can create Spring AOP Advisors given AspectJ classes from classes honouring
 * the AspectJ 5 annotation syntax.
 * 
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 1.3
 */
public class ReflectiveAtAspectJAdvisorFactory implements AtAspectJAdvisorFactory {
	
	private enum AspectJAnnotationType { 
		AtPointcut, 
		AtBefore, 
		AtAfter, 
		AtAfterReturning, 
		AtAfterThrowing, 
		AtAround 
	};

	private static class AspectJAnnotation<A extends Annotation> {
		private static Map<Class,AspectJAnnotationType> annotationTypes = new HashMap<Class,AspectJAnnotationType>();
		static {
			annotationTypes.put(Pointcut.class,AspectJAnnotationType.AtPointcut);
			annotationTypes.put(After.class,AspectJAnnotationType.AtAfter);
			annotationTypes.put(AfterReturning.class,AspectJAnnotationType.AtAfterReturning);
			annotationTypes.put(AfterThrowing.class,AspectJAnnotationType.AtAfterThrowing);
			annotationTypes.put(Around.class,AspectJAnnotationType.AtAround);
			annotationTypes.put(Before.class,AspectJAnnotationType.AtBefore);			
		}
		
		private A annotation;
		private AspectJAnnotationType annotationType;
		
		public AspectJAnnotation(A aspectjAnnotation) {
			this.annotation = aspectjAnnotation;
			for(Class c : annotationTypes.keySet()) {
				if (c.isInstance(this.annotation)) {
					this.annotationType = annotationTypes.get(c);
				}
			}
			if (this.annotationType == null) {
				throw new IllegalStateException("unknown annotation type: " + this.annotation.toString());
			}
		}
		
		public AspectJAnnotationType getAnnotationType() { return this.annotationType; }
		
		public A getAnnotation() { 
			return this.annotation; 
		}
		
		public String getPointcutExpression() {
			switch(this.annotationType) {
			case AtPointcut:
				return ((Pointcut)this.annotation).value();
			case AtBefore:
				return ((Before)this.annotation).value();				
			case AtAfter:
				return ((After)this.annotation).value();				
			case AtAfterReturning:
				return ((AfterReturning)this.annotation).value();				
			case AtAfterThrowing:
				return ((AfterThrowing)this.annotation).value();				
			case AtAround:
				return ((Around)this.annotation).value();				
			default:
				throw new UnsupportedOperationException("Unknown annotation type: " + this.annotationType);
			}
		}
		
		public String getArgNames() {
			switch(this.annotationType) {
			case AtPointcut:
				return ((Pointcut)this.annotation).argNames();
			case AtBefore:
				return ((Before)this.annotation).argNames();				
			case AtAfter:
				return ((After)this.annotation).argNames();				
			case AtAfterReturning:
				return ((AfterReturning)this.annotation).argNames();				
			case AtAfterThrowing:
				return ((AfterThrowing)this.annotation).argNames();				
			case AtAround:
				return ((Around)this.annotation).argNames();				
			default:
				throw new UnsupportedOperationException("Unknown annotation type: " + this.annotationType);
			}			
		}
		
		public String toString() { 
			return this.annotation.toString(); 
		}
	}
	
	private static final ParameterNameDiscoverer ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER = new ParameterNameDiscoverer() {
		public String[] getParameterNames(Constructor ctor) {
			throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
		}
		
		public String[] getParameterNames(Method m, Class clazz) {
			AspectJAnnotation annotation = findAspectJAnnotationOnMethod(m,clazz);
			if (annotation == null) {
				return null;
			}
			
			StringTokenizer strTok = new StringTokenizer(annotation.getArgNames(),",");
			String[] ret = new String[strTok.countTokens()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = strTok.nextToken();
			}
			
			return ret;
		}
	};
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private final ParameterNameDiscoverer parameterNameDiscoverer;
	
	public ReflectiveAtAspectJAdvisorFactory() {
		PrioritizedParameterNameDiscoverer prioritizedParameterNameDiscoverer = new PrioritizedParameterNameDiscoverer();
		prioritizedParameterNameDiscoverer.addDiscoverer(ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER);
		this.parameterNameDiscoverer = prioritizedParameterNameDiscoverer;
	}
	
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
		
		//	Find inner aspect classes
		// TODO: need to go up tree? ReflectionUtils.doWithClasses
		for (Class innerClass : aspectInstance.getClass().getClasses()) {
			//System.out.println(aspectInstance.getClass() + ": " +innerClass);
			if (Modifier.isStatic(innerClass.getModifiers())) {
				// TODO do we really want new instances all the time?
				Object innerInstance = BeanUtils.instantiateClass(innerClass);
				advisors.addAll(getDeclareParentsAdvisors(innerClass, new SingletonAspectInstanceFactory(innerInstance)));
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
	 * @return
	 */
	// TODO is there any point passing in AIF instead of instance here?
	private List<Advisor> getDeclareParentsAdvisors(Class clazz, final AspectInstanceFactory aif) {
		List<Advisor> advisors = new LinkedList<Advisor>();
		DeclareParents declareParentsHack = (DeclareParents) clazz.getAnnotation(DeclareParents.class);
		if (declareParentsHack == null) {
			throw new IllegalArgumentException("Class of type " + clazz + " doesn't have an introduction");
		}
		
		// Work out where it matches, with the ClassFilter
		// TODO
		
		// TODO validate them, can there be more than one?
		final Class[] interfaces = clazz.getInterfaces();
		
		ClassFilter patternFilter = ClassFilter.TRUE;
		ClassFilter exclusion = new ClassFilter() {
			public boolean matches(Class clazz) {
				for (Class introducedInterface : interfaces) {
					if (introducedInterface.isAssignableFrom(clazz)) {
						return false;
					}
				}
				return true;
			}
		};
		final ClassFilter classFilter = ClassFilters.intersection(patternFilter, exclusion);

		// Do delegation
		IntroductionAdvisor ia = new IntroductionAdvisorImpl(interfaces, classFilter, aif.getAspectInstance());//introductionInstance);
		advisors.add(ia);
		return advisors;
	}
	
	/**
	 * 
	 * @param candidateAspectClass
	 * @param candidateAspectJAdviceMethod
	 * @param aif
	 * @return null if the method is not an AspectJ advice method or if it is a pointcut
	 * that will be used by other advice but will not create a Springt advice in its own right
	 */
	public PointcutAdvisor getAdvisor(Class candidateAspectClass, Method candidateAspectJAdviceMethod, AspectInstanceFactory aif) {
		AspectJAnnotation<?> aspectJAnnotation = findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod,candidateAspectClass);
		
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
		AspectJExpressionPointcut ajexp = createPointcutExpression(candidateAspectJAdviceMethod,argNames);
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
			throw new UnsupportedOperationException("Need to add after returning support!");
//			springAdvice = new AspectJAfterAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);				
//			break;
		case AtAfterThrowing:
			throw new UnsupportedOperationException("Need to add after throwing support!");
//			springAdvice = new AspectJAfterAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif);				
//			break;
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
	 * find and return the first AspectJ annotation on the given method
	 * (there *should* only be one anyway....)
	 * @param aMethod
	 * @return
	 */
	private static AspectJAnnotation findAspectJAnnotationOnMethod(Method aMethod, Class clazz) {
		Class<? extends Annotation>[] classesToLookFor = (Class<? extends Annotation>[]) new Class[] {
					Before.class, 
					Around.class, 
					After.class, 
					AfterReturning.class, 
					AfterThrowing.class, 
					Pointcut.class
				};
		for (Class<? extends Annotation> c : classesToLookFor) {
			AspectJAnnotation foundAnnotation = findAnnotation(c,aMethod,clazz);
			if (foundAnnotation != null) {
				return foundAnnotation;
			}
		}
		return null;
	}
	
	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Class<A> toLookFor,Method method, Class clazz) {
		A result = AnnotationUtils.findMethodAnnotation(toLookFor,method,clazz);
		if (result != null) {
			return new AspectJAnnotation<A>(result);
		} else {
			return null;
		}
	}
	
	/**
	 * The pointcut and advice annotations both have an "argNames" member which contains a 
	 * comma-separated list of the argument names. We use this (if non-empty) to build the
	 * formal parameters for the pointcut.
	 * @param annotatedMethod
	 * @param foundAnnotation
	 * @return
	 */
	private AspectJExpressionPointcut createPointcutExpression(Method annotatedMethod, String[] pointcutParameterNames) {
		
		Class<?> [] pointcutParameterTypes = new Class<?>[0];
				
		if (pointcutParameterNames != null) {
			pointcutParameterTypes = extractPointcutParameterTypes(pointcutParameterNames,annotatedMethod);
		}
		
		
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(annotatedMethod.getClass(),pointcutParameterNames,pointcutParameterTypes);
		ajexp.setLocation(annotatedMethod.toString());
		return ajexp;
	}
	
	/**
	 * Create the pointcut parameters needed by aspectj based on the given argument names
	 * and the argument types that are available from the adviceMethod. Needs to take into
	 * account (ignore) any JoinPoint based arguments as these are not pointcut context but
	 * rather part of the advice execution context (thisJoinPoint, thisJoinPointStaticPart)
	 * @param argNames
	 * @param adviceMethod
	 * @return
	 */
	private Class<?>[] extractPointcutParameterTypes(String[] argNames, Method adviceMethod) {
		Class<?>[] ret = new Class<?>[argNames.length];
		Class<?>[] paramTypes = adviceMethod.getParameterTypes();
		if (argNames.length > paramTypes.length) {
			// TODO Spring logging here??
			throw new IllegalStateException("Expecting at least " + argNames.length + 
					     " arguments in the advice declaration, but only found " +
					     paramTypes.length);
		}
		// make the simplifying assumption for now that all of the JoinPoint based arguments
		// come first in the advice declaration
		int typeOffset = paramTypes.length - argNames.length;
		for (int i = 0; i < ret.length; i++) {
			ret[i] = paramTypes[i+typeOffset];			
		}
		return ret;
	}
	

	/**
	 * Introduction advisor delegating to the given object
	 * 
	 * @author Rod Johnson
	 *
	 */
	private final class IntroductionAdvisorImpl implements IntroductionAdvisor {
		private final Class[] interfaces;

		private final ClassFilter filter;

		private final Object instance;

		private IntroductionAdvisorImpl(Class[] interfaces, ClassFilter filter, Object instance) {
			this.interfaces = interfaces;
			this.filter = filter;
			this.instance = instance;
		}

		public ClassFilter getClassFilter() {
			return filter;
		}

		public void validateInterfaces() throws IllegalArgumentException {				
		}

		public boolean isPerInstance() {
			return true;
		}

		public Advice getAdvice() {
			return new DelegatingIntroductionInterceptor(instance);
		}

		public Class[] getInterfaces() {
			return interfaces;
		}
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
	
	private class AspectJAfterAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice {
		
		public AspectJAfterAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
			super(aspectJBeforeAdviceMethod, pe, aif);
		}
		
		public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
			invokeAdviceMethod(args);
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

	public boolean isAspect(Class clazz) {
		return //clazz.isAnnotationPresent(Aspect.class);
		clazz.getAnnotation(Aspect.class) != null;
	}

	// TODO add ASM check for code in pointcut, visibility rules
	public void validate(Class aspectClass) throws AopConfigException {
		// If the parent has the annotation and isn't abstract it's an error
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
				!Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			throw new AopConfigException(aspectClass.getName() + " cannot extend concrete aspect " + 
					aspectClass.getSuperclass().getName());
		}
	}
}
