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

package org.springframework.aop.aspectj.autoproxy;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.AspectMetadata;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.SingletonMetadataAwareAspectInstanceFactory;
import org.springframework.aop.framework.autoproxy.InvocationContextExposingAdvisorAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * {@link org.springframework.aop.framework.autoproxy.InvocationContextExposingAdvisorAutoProxyCreator} subclass that processes all
 * AspectJ annotation classes in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their advice
 * applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>Processing of Spring Advisors follows the rules established in {@link org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
public class AspectJAutoProxyCreator extends InvocationContextExposingAdvisorAutoProxyCreator {
	
	private static final Log staticLogger = LogFactory.getLog(AspectJAutoProxyCreator.class);

	/**
	 * Look for AspectJ annotated aspect classes in the current bean factory,
	 * and return to a list of Spring AOP advisors representing them.
	 * Create a Spring Advisor for each advice method
	 * @param aspectJAdvisorFactory AdvisorFactory to use
	 * @param beanFactory BeanFactory to look for AspectJ annotated aspects in
	 * @return a list of Spring AOP advisors resulting from AspectJ annotated
	 * classes in the current Spring bean factory
	 */
	public static List<Advisor> createAspectJAdvisors(
			AspectJAdvisorFactory aspectJAdvisorFactory, BeanFactory beanFactory)
			throws BeansException, IllegalStateException {

		if (!(beanFactory instanceof BeanDefinitionRegistry)) {
			throw new IllegalStateException(
					"Cannot look for AspectJ aspects without a BeanDefinitionRegistry");
		}
		BeanDefinitionRegistry owningFactory = (BeanDefinitionRegistry) beanFactory;
		
		List<Advisor> advisors = new LinkedList<Advisor>();

		for (String beanName : owningFactory.getBeanDefinitionNames()) {
			// We must be careful not to instantiate beans eagerly as in this
			// case they would be cached by the Spring container but would not
			// have bean weaved
			BeanDefinition bd = owningFactory.getBeanDefinition(beanName);
			if (!(bd instanceof RootBeanDefinition)) {
				continue;
			}
			RootBeanDefinition rbd = (RootBeanDefinition) bd;
			if (rbd.getBeanClass() == null) {
				continue;
			}

			if (aspectJAdvisorFactory.isAspect(rbd.getBeanClass())) {
				//logger.debug("Found aspect bean '" + beanName + "'");
				AspectMetadata amd = new AspectMetadata(rbd.getBeanClass());
				if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
					// Default singleton binding
					Object beanInstance = beanFactory.getBean(beanName);
					List<Advisor> classAdvisors =
							aspectJAdvisorFactory.getAdvisors(new SingletonMetadataAwareAspectInstanceFactory(beanInstance));
					staticLogger.debug("Found " + classAdvisors.size() + " AspectJ advice methods");
					advisors.addAll(classAdvisors);
				}
				else {
					// Pertarget or per this
					if (rbd.isSingleton()) {
						throw new IllegalArgumentException(
								"Bean with name '" + beanName + "' is a singleton, but aspect instantiation model is not singleton");
					}
					
					List<Advisor> classAdvisors =
							aspectJAdvisorFactory.getAdvisors(new PrototypeAspectInstanceFactory(beanFactory, beanName));
					staticLogger.debug("Found " + classAdvisors.size() + " AspectJ advice methods in bean with name '" + beanName + "'");
					advisors.addAll(classAdvisors);
				}
			}
		}
		return advisors;
	}


	private AspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory();

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected List findCandidateAdvisors() {
		List<Advisor> advisors = new LinkedList<Advisor>();

		// Add all the Spring advisors found according to superclass rules
		advisors.addAll(super.findCandidateAdvisors());

		advisors.addAll(createAspectJAdvisors(aspectJAdvisorFactory, getBeanFactory()));
		return advisors;
	}

}
