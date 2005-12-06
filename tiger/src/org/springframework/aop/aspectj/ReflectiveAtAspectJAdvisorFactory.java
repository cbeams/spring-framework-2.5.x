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

package org.springframework.aop.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.aop.AspectException;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.ClassFilters;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.TypePatternClassFilter;
import org.springframework.util.ReflectionUtils;

/**
 * Factory that can create Spring AOP Advisors given AspectJ classes from classes honouring
 * the AspectJ 5 annotation syntax, using reflection to invoke advice methods.
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public class ReflectiveAtAspectJAdvisorFactory extends AbstractAtAspectJAdvisorFactory {
	
	/**
	 * Create Spring Advisors for all At AspectJ methods on the given aspect instance.
	 * @param aspectInstance
	 * @return a list of advisors for this class
	 */
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory maaif) {
		final Class<?> aspectClass = maaif.getAspectMetadata().getAspectClass();
		validate(aspectClass);
		
		// We need to wrap the MetadataAwareAspectInstanceFactory with a decorator
		// so that it will only instantiate once.
		final MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory = new LazySingletonMetadataAwareAspectInstanceFactoryDecorator(maaif);
		
		final List<Advisor> advisors = new LinkedList<Advisor>();
		//final AspectInstanceFactory aif = new AspectInstanceFactory.SingletonAspectInstanceFactory(aspectInstance);
		ReflectionUtils.doWithMethods(aspectClass, new ReflectionUtils.MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				// Exclude pointcuts
				if (m.getAnnotation(Pointcut.class) == null) {
					PointcutAdvisor pa = getAdvisor(m, lazySingletonAspectInstanceFactory);
					if (pa != null) {
						advisors.add(pa);
					}
				}
			}
		}, ReflectionUtils.DECLARED_METHODS);
		
		// If it's a per target aspect, emit dummy instantiating aspect
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isPerThisOrPerTarget()) {
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}
		
		//	Find introduction fields
		for (Field f : aspectClass.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
				Advisor a = getDeclareParentsAdvisor(f, lazySingletonAspectInstanceFactory.getAspectInstance());
				if (a != null) {
					advisors.add(a);
				}
			}
		}
		
		return advisors;
	}
	
	/**
	 * Synthetic advisor that instantiates the aspect.
	 * Triggered by perclause pointcut on non-singleton aspect.
	 * The advice has no effect.
	 */
	protected static class SyntheticInstantiationAdvisor extends DefaultPointcutAdvisor {
		
		public SyntheticInstantiationAdvisor(final MetadataAwareAspectInstanceFactory aif) {
			super(aif.getAspectMetadata().getPerClausePointcut(), new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) {
					// Simply instantiate the aspect
					aif.getAspectInstance();
				}
			});
		}
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
	
	
	public InstantiationModelAwarePointcutAdvisor getAdvisor(Method candidateAspectJAdviceMethod, MetadataAwareAspectInstanceFactory aif) {
		validate(aif.getAspectMetadata().getAspectClass());
		
		AspectJExpressionPointcut ajexp = getPointcut(candidateAspectJAdviceMethod, aif.getAspectMetadata().getAspectClass());
		if (ajexp == null) {
			return null;
		}
		return new InstantiationModelAwarePointcutAdvisor(this, ajexp, aif, candidateAspectJAdviceMethod);
	}
	
	
	/**
	 * 
	 * @param candidateAspectClass
	 * @param candidateAspectJAdviceMethod
	 * @param aif
	 * @return null if the method is not an AspectJ advice method or if it is a pointcut
	 * that will be used by other advice but will not create a Springt advice in its own right
	 */
	public Advice getAdvice(Method candidateAspectJAdviceMethod, MetadataAwareAspectInstanceFactory aif) {
		Class<?> candidateAspectClass = aif.getAspectMetadata().getAspectClass();
		
		validate(aif.getAspectMetadata().getAspectClass());
		
		AspectJAnnotation<?> aspectJAnnotation = AbstractAtAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod, candidateAspectClass);
		if (aspectJAnnotation == null) {
			//throw new IllegalStateException("Class " + aif.getAspectMetadata().getAspectClass() + " must be an aspect");
			return null;
		}
	
		// If we get here, we know we have an AspectJ method. 
		// Check that it's an AspectJ-annotated class
		if (!isAspect(candidateAspectClass)) {
			throw new AopConfigException("Advice must be declared inside an aspect type: " +
					"Offending method '" + candidateAspectJAdviceMethod + "' in class " + candidateAspectClass.getName());
		}
		
		log.debug("Found AspectJ method " + candidateAspectJAdviceMethod);

		AspectJExpressionPointcut ajexp = getPointcut(candidateAspectJAdviceMethod, candidateAspectClass);
		
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
			springAdvice = new AspectJAroundAdvice(candidateAspectJAdviceMethod, ajexp.getPointcutExpression(), aif, parameterNameDiscoverer);		
			break;
		case AtPointcut:
			log.debug("Processing pointcut '" + candidateAspectJAdviceMethod.getName() + "'");
			return null;
		default:
			throw new UnsupportedOperationException("Unsupported advice type on method " + candidateAspectJAdviceMethod);			
		}
		
		return springAdvice;
	}


	private AspectJExpressionPointcut getPointcut(Method candidateAspectJAdviceMethod, Class<?> candidateAspectClass) {
		AspectJAnnotation<?> aspectJAnnotation = AbstractAtAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod,candidateAspectClass);
		if (aspectJAnnotation == null) {
			return null;
		}
		
		String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAspectJAdviceMethod,candidateAspectClass);
		AspectJExpressionPointcut ajexp = createPointcutExpression(candidateAspectJAdviceMethod,candidateAspectClass,argNames);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		return ajexp;
	}
}
