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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.autoproxy.AspectJInvocationContextExposingAdvisorAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.aop.framework.autoproxy.InvocationContextExposingAdvisorAutoProxyCreator}
 * subclass that processes all AspectJ annotation classes in the current application context,
 * as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their advice
 * applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by
 * an include pattern will be considered as defining aspects to use for Spring autoproxying.
 *
 * <p>Processing of Spring Advisors follows the rules established in
 * {@link org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJInvocationContextExposingAdvisorAutoProxyCreator {

	private static final long serialVersionUID = -3347584141231774337L;

	private static final Log staticLogger = LogFactory.getLog(AnnotationAwareAspectJAutoProxyCreator.class);

	private static final String ORDER_PROPERTY = "order";


	private AspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory();

	private List<Pattern> includePatterns;


	public AnnotationAwareAspectJAutoProxyCreator() {
		super();
		// previously we setProxyTargetClass(true) here, but that has too broad an
		// impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
	}

	// See comment in constructor for why we do this...
	protected boolean isInfrastructureClass(Class beanClass) {
		return (super.isInfrastructureClass(beanClass) || this.aspectJAdvisorFactory.isAspect(beanClass));
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(this.aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<Pattern>(patterns.size());
		for(String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	/**
	 * If no &lt;aop:include&gt; elements were used then includePatterns will be
	 * null and all beans are included. If includePatterns is non-null, then one
	 * of the patterns must match.
	 */
	private boolean isIncluded(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	protected List findCandidateAdvisors() {
		List<Advisor> advisors = new LinkedList<Advisor>();

		// Add all the Spring advisors found according to superclass rules
		advisors.addAll(super.findCandidateAdvisors());

		// Safety of cast is already checked in superclass.
		ListableBeanFactory lbf = (ListableBeanFactory) getBeanFactory();

		advisors.addAll(createAspectJAdvisors(this.aspectJAdvisorFactory, lbf));

		return advisors;
	}

	/**
	 * Look for AspectJ annotated aspect classes in the current bean factory,
	 * and return to a list of Spring AOP advisors representing them.
	 * Create a Spring Advisor for each advice method
	 * @param aspectJAdvisorFactory the AdvisorFactory to use
	 * @param beanFactory the BeanFactory to look for AspectJ annotated aspects in
	 * @return a list of Spring AOP advisors resulting from AspectJ annotated
	 * classes in the current Spring bean factory
	 */
	public List<Advisor> createAspectJAdvisors (
			AspectJAdvisorFactory aspectJAdvisorFactory, ListableBeanFactory beanFactory)
			throws BeansException, IllegalStateException {

		List<Advisor> advisors = new LinkedList<Advisor>();

		String[] beanNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Object.class, true, false);
		for (String beanName : beanNames) {
			if (!isIncluded(beanName)) {
				continue;
			}
			
			// We must be careful not to instantiate beans eagerly as in this
			// case they would be cached by the Spring container but would not
			// have been weaved
			Class<?> beanType = beanFactory.getType(beanName);
			if (beanType == null) {
				continue;
			}

			if (aspectJAdvisorFactory.isAspect(beanType)) {
				AspectMetadata amd = new AspectMetadata(beanType, beanName);
				if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
					// Default singleton binding.
					try {
						Object beanInstance = beanFactory.getBean(beanName);
						List<Advisor> classAdvisors =
								aspectJAdvisorFactory.getAdvisors(new SingletonMetadataAwareAspectInstanceFactory(beanInstance,beanName));
						setAdvisorOrderIfNecessary(classAdvisors,beanInstance);
						if (staticLogger.isDebugEnabled()) {
							staticLogger.debug("Found " + classAdvisors.size() + " AspectJ advice methods");
						}
						advisors.addAll(classAdvisors);
					}
					catch (BeanCreationException ex) {
						if (ex.contains(BeanCurrentlyInCreationException.class)) {
							if (logger.isDebugEnabled()) {
								logger.debug("Ignoring currently created advisor '" + beanName + "': " + ex.getMessage());
							}
							// Ignore: indicates a reference back to the bean we're trying to advise.
							// We want to find advisors other than the currently created bean itself.
						}
						else {
							throw ex;
						}
					}
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
					if (staticLogger.isDebugEnabled()) {
						staticLogger.debug("Found " + classAdvisors.size() +
								" AspectJ advice methods in bean with name '" + beanName + "'");
					}
					advisors.addAll(classAdvisors);
				}
			}
		}
		return advisors;
	}

	private void setAdvisorOrderIfNecessary(List<Advisor> advisors, Object beanInstance) {
		if (beanInstance instanceof Ordered) {
			int order = ((Ordered)beanInstance).getOrder();
			for (Advisor advisor : advisors) {
				if (advisor instanceof InstantiationModelAwarePointcutAdvisor) {
					((InstantiationModelAwarePointcutAdvisor)advisor).setOrder(order);
				}
			}
		}
	}
	
	// We can't instantiate a bean instance, so we look for a prototype order property value
	// and use that instead...
	private void setAdvisorOrderIfNecessary(List<Advisor> advisors, BeanFactory beanFactory, String beanName) {
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

}
