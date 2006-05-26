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

package org.springframework.dao.support;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;

/**
 * Exception translation aspect applying to DAOs annotated with the
 * Repository annotation.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class PersistenceExceptionTranslationPostProcessor 
				extends InstantiationAwareBeanPostProcessorAdapter 
				implements BeanFactoryAware, Ordered {
	
	private PersistenceExceptionTranslationAdvisor persistenceExceptionTranslationAdvisor;
	
	public int getOrder() {
		// This should run after all other post processors, so
		// that it can just add an advisor to existing proxies rather than double proxy
		return LOWEST_PRECEDENCE;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException("Cannot use " + getClass().getSimpleName() + " without ListableBeanFactory");
		}
		ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
		
		// Find all translators, being careful not to activate FactoryBeans
		List<PersistenceExceptionTranslator> pets = new LinkedList<PersistenceExceptionTranslator>();
		for (String petBeanName : lbf.getBeanNamesForType(PersistenceExceptionTranslator.class, false, false)) {
			if (lbf.isSingleton(petBeanName)) {
				pets.add((PersistenceExceptionTranslator) lbf.getBean(petBeanName));
			}
		}
		pets = validateAndFilter(pets);
		ChainedPersistenceExceptionTranslator cpet = new ChainedPersistenceExceptionTranslator();
		for (PersistenceExceptionTranslator pet : pets) {
			cpet.add(pet);
		}
		
		this.persistenceExceptionTranslationAdvisor = new PersistenceExceptionTranslationAdvisor(cpet);
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> targetClass;
		if (bean instanceof Advised) {
			Advised advised = (Advised) bean;
			targetClass = advised.getTargetSource().getTargetClass();
		}
		else {
			targetClass = bean.getClass();
		}
		
		if (targetClass == null) {
			// Can't do much here
			return bean;
		}
		
		if (AopUtils.canApply(persistenceExceptionTranslationAdvisor, targetClass)) {
			if (bean instanceof Advised) {
				Advised advised = (Advised) bean;
				advised.addAdvisor(persistenceExceptionTranslationAdvisor);
				return bean;
				//throw new UnsupportedOperationException();
			}			
			else {
				ProxyFactory pf = new ProxyFactory(bean);
				pf.addAdvisor(persistenceExceptionTranslationAdvisor);
				return pf.getProxy();
			}
		}
		else {
			// This is not a repository
			return bean;
		}
	}

	/**
	 * Validate and filter the given PersistenceExceptionTranslators
	 * @param allPets
	 * @return
	 */
	protected List<PersistenceExceptionTranslator> validateAndFilter(List<PersistenceExceptionTranslator> allPets) throws IllegalStateException {
		List<PersistenceExceptionTranslator> filteredPets = new LinkedList<PersistenceExceptionTranslator>();
		for (PersistenceExceptionTranslator pet : allPets) {
			// TODO filter according to rules: one of each class etc.
			filteredPets.add(pet);
		}
		
		if (filteredPets.isEmpty()) {
			throw new IllegalStateException("No persistence exception translators found. Cannot translate. Remove this PostProcessor");
		}
		
		return filteredPets;
	}

}
