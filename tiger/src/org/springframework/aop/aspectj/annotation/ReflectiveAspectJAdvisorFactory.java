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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.aop.AspectException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Factory that can create Spring AOP Advisors given AspectJ classes from classes honouring
 * the AspectJ 5 annotation syntax, using reflection to invoke advice methods.
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory {

	/**
	 * Create Spring Advisors for all At AspectJ methods on the given aspect instance.
	 * @return a list of advisors for this class
	 */
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory maaif) {
		final Class<?> aspectClass = maaif.getAspectMetadata().getAspectClass();
		final String aspectName = maaif.getAspectMetadata().getAspectName();
		validate(aspectClass);

		// We need to wrap the MetadataAwareAspectInstanceFactory with a decorator
		// so that it will only instantiate once.
		final MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonMetadataAwareAspectInstanceFactoryDecorator(maaif);

		final List<Advisor> advisors = new LinkedList<Advisor>();
		//final AspectInstanceFactory aif = new AspectInstanceFactory.SingletonAspectInstanceFactory(aspectInstance);
		ReflectionUtils.doWithMethods(aspectClass, new ReflectionUtils.MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
				// Exclude pointcuts
				if (AnnotationUtils.getAnnotation(m, Pointcut.class) == null) {
					PointcutAdvisor pa = getAdvisor(m, lazySingletonAspectInstanceFactory, advisors.size(), aspectName);
					if (pa != null) {
						advisors.add(pa);
					}
				}
			}
		});

		// If it's a per target aspect, emit the dummy instantiating aspect
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}

		//	Find introduction fields
		for (Field f : aspectClass.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
				Advisor a = getDeclareParentsAdvisor(f);
				if (a != null) {
					advisors.add(a);
				}
			}
		}

		return advisors;
	}

	/**
	 * Resulting advisors will need to be evaluated for targets.
	 * @return <code>null</code> if not an advisor
	 */
	private Advisor getDeclareParentsAdvisor(Field introductionField) {
		DeclareParents declareParents = (DeclareParents) introductionField.getAnnotation(DeclareParents.class);
		if (declareParents == null) {
			// Not an introduction field
			return null;
		}

		if (declareParents.defaultImpl() == DeclareParents.class) {
			// This is what comes back if it wasn't set. This seems bizarre...
			// TODO this restriction possibly should be relaxed
			throw new AspectException("defaultImpl must be set on DeclareParents");
		}

		return new DeclareParentsAdvisor(introductionField.getType(), declareParents.value(), declareParents.defaultImpl());
	}


	public InstantiationModelAwarePointcutAdvisorImpl getAdvisor(
			Method candidateAspectJAdviceMethod, MetadataAwareAspectInstanceFactory aif,
			int declarationOrderInAspect, String aspectName) {
		validate(aif.getAspectMetadata().getAspectClass());

		AspectJExpressionPointcut ajexp =
				getPointcut(candidateAspectJAdviceMethod, aif.getAspectMetadata().getAspectClass());
		if (ajexp == null) {
			return null;
		}
		return new InstantiationModelAwarePointcutAdvisorImpl(this, ajexp, aif, candidateAspectJAdviceMethod, declarationOrderInAspect, aspectName);
	}

	/**
	 * @return <code>null</code> if the method is not an AspectJ advice method or if it is a pointcut
	 * that will be used by other advice but will not create a Springt advice in its own right
	 */
	public Advice getAdvice(
			Method candidateAspectJAdviceMethod,
			AspectJExpressionPointcut ajexp,
			MetadataAwareAspectInstanceFactory aif,
			int declarationOrderInAspect,
			String aspectName) {
		Class<?> candidateAspectClass = aif.getAspectMetadata().getAspectClass();

		validate(aif.getAspectMetadata().getAspectClass());

		AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod, candidateAspectClass);
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

		logger.debug("Found AspectJ method " + candidateAspectJAdviceMethod);

		AbstractAspectJAdvice springAdvice;

		switch(aspectJAnnotation.getAnnotationType()) {
		case AtBefore:
			springAdvice = new AspectJMethodBeforeAdvice(candidateAspectJAdviceMethod, ajexp, aif);
			break;
		case AtAfter:
			springAdvice = new AspectJAfterAdvice(candidateAspectJAdviceMethod, ajexp, aif);
			break;
		case AtAfterReturning:
			springAdvice = new AspectJAfterReturningAdvice(candidateAspectJAdviceMethod, ajexp, aif);
			AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
			if (StringUtils.hasText(afterReturningAnnotation.returning())) {
				springAdvice.setReturningName(afterReturningAnnotation.returning());
			}
			break;
		case AtAfterThrowing:
			springAdvice = new AspectJAfterThrowingAdvice(candidateAspectJAdviceMethod, ajexp, aif);
			AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
			if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
				springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
			}
			break;
		case AtAround:
			springAdvice = new AspectJAroundAdvice(
					candidateAspectJAdviceMethod, ajexp, aif, parameterNameDiscoverer);
			break;
		case AtPointcut:
			logger.debug("Processing pointcut '" + candidateAspectJAdviceMethod.getName() + "'");
			return null;
		default:
			throw new UnsupportedOperationException("Unsupported advice type on method " + candidateAspectJAdviceMethod);
		}

		// now to configure the advice...
		springAdvice.setAspectName(aspectName);
		springAdvice.setDeclarationOrder(declarationOrderInAspect);
		String[] argNames = getArgNames(candidateAspectJAdviceMethod,candidateAspectClass);
		if (argNames != null) {
			springAdvice.setArgNamesFromStringArray(argNames);
		}
		try {
			springAdvice.afterPropertiesSet();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Advice configuration failed",ex);
		}
		return (Advice) springAdvice;
	}

	private String[] getArgNames(Method forMethod, Class inClass) {
		String[] argNames = this.parameterNameDiscoverer.getParameterNames(forMethod,inClass);
		if (argNames != null) {
			if (forMethod.getParameterTypes().length == (argNames.length + 1) ) {
				// may need to add implicit join point arg name
				Class firstArgType = forMethod.getParameterTypes()[0];
				if (firstArgType == JoinPoint.class ||
					firstArgType == ProceedingJoinPoint.class ||
					firstArgType == JoinPoint.StaticPart.class) {
					String[] oldNames = argNames;
					argNames = new String[oldNames.length + 1];
					argNames[0] = "THIS_JOIN_POINT";
					System.arraycopy(oldNames,0,argNames,1,oldNames.length);
				}
			}
		}
		return argNames;
	}

	private AspectJExpressionPointcut getPointcut(Method candidateAspectJAdviceMethod, Class<?> candidateAspectClass) {
		AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod,candidateAspectClass);
		if (aspectJAnnotation == null) {
			return null;
		}


		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,new String[0],new Class[0]);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		return ajexp;
	}


	/**
	 * Synthetic advisor that instantiates the aspect.
	 * Triggered by perclause pointcut on non-singleton aspect.
	 * The advice has no effect.
	 */
	protected static class SyntheticInstantiationAdvisor extends DefaultPointcutAdvisor {

		private static final long serialVersionUID = -7789221134469113954L;

		public SyntheticInstantiationAdvisor(final MetadataAwareAspectInstanceFactory aif) {
			super(aif.getAspectMetadata().getPerClausePointcut(), new MethodBeforeAdvice() {
				public void before(Method method, Object[] args, Object target) {
					// Simply instantiate the aspect
					aif.getAspectInstance();
				}
			});
		}
	}

}
