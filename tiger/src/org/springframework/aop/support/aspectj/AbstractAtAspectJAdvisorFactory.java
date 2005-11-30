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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.AnnotationUtils;

/**
 * Abstract base class for factories that can create Spring AOP Advisors given AspectJ classes from classes honouring
 * the AspectJ 5 annotation syntax.
 * <br>
 * This class handles annotation parsing and validation functionality. 
 * It does not actually generate Spring AOP Advisors, which is deferred to subclasses.
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 1.3
 */
public abstract class AbstractAtAspectJAdvisorFactory implements AtAspectJAdvisorFactory {
	
	protected enum AspectJAnnotationType { 
		AtPointcut, 
		AtBefore, 
		AtAfter, 
		AtAfterReturning, 
		AtAfterThrowing, 
		AtAround 
	};

	protected static class AspectJAnnotation<A extends Annotation> {
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
	
	protected static final ParameterNameDiscoverer ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER = new ParameterNameDiscoverer() {
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
	
	/**
	 * find and return the first AspectJ annotation on the given method
	 * (there <i>should</i> only be one anyway....)
	 * @param aMethod
	 * @return
	 */
	protected static AspectJAnnotation findAspectJAnnotationOnMethod(Method aMethod, Class clazz) {
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
	
	/** Logger available to subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	protected final ParameterNameDiscoverer parameterNameDiscoverer;
	
	
	protected AbstractAtAspectJAdvisorFactory() {
		PrioritizedParameterNameDiscoverer prioritizedParameterNameDiscoverer = new PrioritizedParameterNameDiscoverer();
		prioritizedParameterNameDiscoverer.addDiscoverer(ASPECTJ_ANNOTATION_PARAMETER_NAME_DISCOVERER);
		this.parameterNameDiscoverer = prioritizedParameterNameDiscoverer;
	}

	public boolean isAspect(Class<?> clazz) {
		return //clazz.isAnnotationPresent(Aspect.class);
		clazz.getAnnotation(Aspect.class) != null;
	}

	// TODO add ASM check for code in pointcut, visibility rules
	public void validate(Class<?> aspectClass) throws AopConfigException {
		// If the parent has the annotation and isn't abstract it's an error
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
				!Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			throw new AopConfigException(aspectClass.getName() + " cannot extend concrete aspect " + 
					aspectClass.getSuperclass().getName());
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
	protected AspectJExpressionPointcut createPointcutExpression(Method annotatedMethod, Class declarationScope, String[] pointcutParameterNames) {
		Class<?> [] pointcutParameterTypes = new Class<?>[0];
		if (pointcutParameterNames != null) {
			pointcutParameterTypes = extractPointcutParameterTypes(pointcutParameterNames,annotatedMethod);
		}
		
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(declarationScope,pointcutParameterNames,pointcutParameterTypes);
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

}
