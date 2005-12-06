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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;

/**
 * Metadata for an AspectJ aspect class, with an
 * additional Spring AOP pointcut for the per clause.
 * @author Rod Johnson
 * @since 1.3
 */
public class AspectMetadata {
	
	private final AjType ajType;
	
	private final Pointcut perClausePointcut;
	
	public AspectMetadata(Class<?> aspectClass) {
		this.ajType = AjTypeSystem.getAjType(aspectClass);
		
		switch (ajType.getPerClause().getKind()) {
			case SINGLETON :
				this.perClausePointcut = Pointcut.TRUE;
				return;
				
			case PERTARGET : case PERTHIS :
				AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
				ajexp.setLocation("@Aspect annotation on " + aspectClass.getName());
				
				// TODO fragile, can't we use ajt to get it from PerClause
				ajexp.setExpression(findPerClause(aspectClass));
				this.perClausePointcut = ajexp;
				return;
				
			default :
				throw new AopConfigException("PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for class " +
						aspectClass.getName());
		}
	}
	
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
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
	
	public AjType getAjType() {
		return this.ajType;
		
	}
	
	public Class<?> getAspectClass() {
		return this.ajType.getJavaClass();
	}
	
	/**
	 * Return a Spring pointcut expression for a singleton aspect
	 * @return Pointcut.TRUE if it's a singleton
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}


}
