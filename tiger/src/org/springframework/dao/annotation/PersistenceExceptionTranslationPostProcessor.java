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

package org.springframework.dao.annotation;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.dao.support.ChainedPersistenceExceptionTranslator;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Bean post-processor that automatically applies persistence exception
 * translation to any bean that carries the Repository annotation.
 *
 * <p>Translates native resource exceptions to Spring's DataAccessException hierarchy.
 * Autodetects beans that implement the PersistenceExceptionTranslator interface,
 * which are subsequently asked to translate candidate exceptions.
 *
 * <p>All of Spring's applicable resource factories implement the
 * PersistenceExceptionTranslator interface out of the box. As a consequence,
 * all that is usually needed to enable automatic exception translation is
 * marking all affected beans (such as DAOs) with the Repository annotation,
 * along with defining this post-processor as bean in the application context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.dao.DataAccessException
 * @see PersistenceExceptionTranslator
 * @see PersistenceExceptionTranslationAdvisor
 */
public class PersistenceExceptionTranslationPostProcessor implements BeanPostProcessor, BeanFactoryAware, Ordered {

	private Class<? extends Annotation> repositoryAnnotationType = Repository.class;

	private PersistenceExceptionTranslationAdvisor persistenceExceptionTranslationAdvisor;


	/**
	 * Set the 'repository' annotation type.
	 * The default required annotation type is the {@link Repository} annotation.
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a class has a
	 * repository role.
	 * @param repositoryAnnotationType the desired annotation type
	 */
	public void setRepositoryAnnotationType(Class<? extends Annotation> repositoryAnnotationType) {
		Assert.notNull(repositoryAnnotationType, "requiredAnnotationType must not be null");
		this.repositoryAnnotationType = repositoryAnnotationType;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException("Cannot use " + getClass().getSimpleName() + " without ListableBeanFactory");
		}
		ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
		
		// Find all translators, being careful not to activate FactoryBeans
		List<PersistenceExceptionTranslator> pets = new LinkedList<PersistenceExceptionTranslator>();
		for (String petBeanName : lbf.getBeanNamesForType(PersistenceExceptionTranslator.class, false, false)) {
			pets.add((PersistenceExceptionTranslator) lbf.getBean(petBeanName));
		}
		pets = validateAndFilter(pets);
		ChainedPersistenceExceptionTranslator cpet = new ChainedPersistenceExceptionTranslator();
		for (PersistenceExceptionTranslator pet : pets) {
			cpet.add(pet);
		}
		
		this.persistenceExceptionTranslationAdvisor =
				new PersistenceExceptionTranslationAdvisor(cpet, this.repositoryAnnotationType);
	}

	public int getOrder() {
		// This should run after all other post-processors, so that it can just add
		// an advisor to existing proxies rather than double-proxy.
		return LOWEST_PRECEDENCE;
	}


	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

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
		
		if (AopUtils.canApply(this.persistenceExceptionTranslationAdvisor, targetClass)) {
			if (bean instanceof Advised) {
				Advised advised = (Advised) bean;
				advised.addAdvisor(this.persistenceExceptionTranslationAdvisor);
				return bean;
			}
			else {
				ProxyFactory pf = new ProxyFactory(bean);
				pf.addAdvisor(this.persistenceExceptionTranslationAdvisor);
				return pf.getProxy();
			}
		}
		else {
			// This is not a repository.
			return bean;
		}
	}

	/**
	 * Validate and filter the given PersistenceExceptionTranslators
	 */
	protected List<PersistenceExceptionTranslator> validateAndFilter(List<PersistenceExceptionTranslator> allPets)
			throws IllegalStateException {

		List<PersistenceExceptionTranslator> filteredPets = new LinkedList<PersistenceExceptionTranslator>();
		for (PersistenceExceptionTranslator pet : allPets) {
			// TODO filter according to rules: one of each class etc.
			filteredPets.add(pet);
		}
		
		if (filteredPets.isEmpty()) {
			throw new IllegalStateException(
					"No persistence exception translators found. Cannot translate. Remove this PostProcessor");
		}
		
		return filteredPets;
	}

}
