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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.framework.AopConfigException;

/**
 * Metadata for an AspectJ aspect class, with an additional Spring AOP pointcut
 * for the per clause.
 *
 * <p>Uses AspectJ 5 AJType reflection API, so is only supported on Java 5.
 * Enables us to work with different AspectJ instantiation models such as
 * "singleton", "pertarget" and "perthis".
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
public class AspectMetadata {
	
	private final AjType ajType;
	
	/**
	 * Spring AOP pointcut corresponding to the per clause of the
	 * aspect. Will be the Pointcut.TRUE canonical instance in the
	 * case of a singleton, otherwise an AspectJExpressionPointcut.
	 */
	private final Pointcut perClausePointcut;

	/**
	 * The name of this aspect as defined to Spring (the bean name) - 
	 * allows us to determine if two pieces of advice come from the 
	 * same aspect and hence their relative precedence.
	 */
	private String aspectName;


	public AspectMetadata(Class<?> aspectClass, String aspectName) {
		this.aspectName = aspectName;
		this.ajType = AjTypeSystem.getAjType(aspectClass);
		
		if (!ajType.isAspect()) {
			throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
		}
		validate();

		switch (ajType.getPerClause().getKind()) {
			case SINGLETON :
				this.perClausePointcut = Pointcut.TRUE;
				return;
				
			case PERTARGET : case PERTHIS :
				AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
				ajexp.setLocation("@Aspect annotation on " + aspectClass.getName());
				ajexp.setExpression(findPerClause(aspectClass));
				this.perClausePointcut = ajexp;
				return;
				
			case PERTYPEWITHIN :
				// Works with a type pattern
				final ClassFilter typePatternClassFilter = new TypePatternClassFilter(findPerClause(aspectClass));
				this.perClausePointcut = new Pointcut() {
					public ClassFilter getClassFilter() {
						return typePatternClassFilter;
					}
					
					public MethodMatcher getMethodMatcher() {
						return MethodMatcher.TRUE;
					}
				};
				return;
				
			default :
				throw new AopConfigException("PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for class " +
						aspectClass.getName());
		}
	}


	private void validate() throws IllegalArgumentException {
		if (this.ajType.getDeclarePrecedence().length > 0) {
			throw new IllegalArgumentException("DeclarePrecendence not presently supported in Spring AOP");
		}
	}
	
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
	}
	
	public boolean isPerTypeWithin() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTYPEWITHIN);
	}
	
	public boolean isLazilyInstantiated() {
		return isPerThisOrPerTarget() || isPerTypeWithin();
	}

	/**
	 * Extract contents from string of form pertarget(contents)
	 * @param aspectClass
	 * @return
	 */
	private String findPerClause(Class<?> aspectClass) {
		// TODO when AspectJ provides this, we can remove this hack. Hence we don't
		// bother to make it elegant. Or efficient. Or robust :-)
		String s = aspectClass.getAnnotation(Aspect.class).value();
		s = s.substring(s.indexOf("(") + 1);
		s = s.substring(0, s.length() - 1);
		return s;
	}
	
	/**
	 * Return AspectJ reflection information
	 * @return information from AspectJ about the aspect
	 */
	public AjType getAjType() {
		return this.ajType;
	}
	
	public Class<?> getAspectClass() {
		return this.ajType.getJavaClass();
	}
	
	public String getAspectName() {
		return this.aspectName;
	}
	
	/**
	 * Return a Spring pointcut expression for a singleton aspect
	 * @return Pointcut.TRUE if it's a singleton
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}

}
