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
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.AspectMetadata;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.SingletonMetadataAwareAspectInstanceFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

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
public class AspectJAutoProxyCreator extends AspectJInvocationContextExposingAdvisorAutoProxyCreator {

	private static final long serialVersionUID = -3347584141231774337L;
	private static final Log staticLogger = LogFactory.getLog(AspectJAutoProxyCreator.class);
	private static final String ORDER_PROPERTY = "order"; 
	
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

		List<Advisor> advisors = new LinkedList<Advisor>();

		// Safety of cast is already enforced by superclass
		String[] beanDefinitionNames = BeanFactoryUtils.beanNamesIncludingAncestors((ListableBeanFactory) beanFactory);		
		
		for (String beanName : beanDefinitionNames) {
			// We must be careful not to instantiate beans eagerly as in this
			// case they would be cached by the Spring container but would not
			// have been weaved
			Class<?> beanType = beanFactory.getType(beanName);
			if (beanType == null) {
				continue;
			}

			if (aspectJAdvisorFactory.isAspect(beanType)) {
				//logger.debug("Found aspect bean '" + beanName + "'");
				AspectMetadata amd = new AspectMetadata(beanType,beanName);
				if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
					// Default singleton binding
					Object beanInstance = beanFactory.getBean(beanName);
					List<Advisor> classAdvisors =
							aspectJAdvisorFactory.getAdvisors(new SingletonMetadataAwareAspectInstanceFactory(beanInstance,beanName));
					setAdvisorOrderIfNecessary(classAdvisors,beanInstance);
					staticLogger.debug("Found " + classAdvisors.size() + " AspectJ advice methods");
					advisors.addAll(classAdvisors);
				}
				else {
					// Pertarget or per this
					if (beanFactory.isSingleton(beanName)) {
						throw new IllegalArgumentException(
								"Bean with name '" + beanName + "' is a singleton, but aspect instantiation model is not singleton");
					}
					
					List<Advisor> classAdvisors =
							aspectJAdvisorFactory.getAdvisors(new PrototypeAspectInstanceFactory(beanFactory, beanName));
					setAdvisorOrderIfNecessary(classAdvisors,beanFactory,beanName);
					staticLogger.debug("Found " + classAdvisors.size() + " AspectJ advice methods in bean with name '" + beanName + "'");
					advisors.addAll(classAdvisors);
				}
			}
		}
		return advisors;
	}

	// TODO: consider creating intermediate OrderedPointcutAdvisor interface between
	// PointcutAdvisor and InstantiationModelAwarePointcutAdvisor
	private static void setAdvisorOrderIfNecessary(List<Advisor> advisors, Object beanInstance) {
		if (beanInstance instanceof Ordered) {
			int order = ((Ordered)beanInstance).getOrder();
			for (Advisor advisor : advisors) {
				if (advisor instanceof InstantiationModelAwarePointcutAdvisor) {
					((InstantiationModelAwarePointcutAdvisor)advisor).setOrder(order);
				}
			}
		}
	}
	
	// we can't instantiate a bean instance, so we look for a prototype order property value
	// and use that instead...
	private static void setAdvisorOrderIfNecessary(List<Advisor> advisors, BeanFactory beanFactory, String beanName) {
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			BeanDefinition beanDef = ((ConfigurableListableBeanFactory)beanFactory).getBeanDefinition(beanName);
			MutablePropertyValues mpvs = beanDef.getPropertyValues();
			if (mpvs.contains(ORDER_PROPERTY)) {
				int order = Integer.parseInt((String)mpvs.getPropertyValue(ORDER_PROPERTY).getValue());
				for (Advisor advisor : advisors) {
					if (advisor instanceof InstantiationModelAwarePointcutAdvisor) {
						((InstantiationModelAwarePointcutAdvisor)advisor).setOrder(order);
					}
				}
			}
		}
	}

	private AspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory();

	public AspectJAutoProxyCreator() {
		super();
		// we need to always use class proxying for @AspectJ aspects, as there is no
		// guarantee that all of the advice methods are contained in any interface
		// that the aspect may implement (typically, an @AspectJ aspect won't implement
		// any interfaces anyway, so this is not a big loss).
		setProxyTargetClass(true);
	}
	
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
