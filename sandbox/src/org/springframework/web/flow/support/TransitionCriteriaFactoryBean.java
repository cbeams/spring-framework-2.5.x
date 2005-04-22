/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.config.TransitionCriteriaCreator;

/**
 * A factory bean that produces Flow state transition criteria when requested.
 * 
 * @author Keith Donald
 */
public class TransitionCriteriaFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * The criteria creation strategy.
	 */
	private TransitionCriteriaCreator criteriaCreator = new OgnlTransitionCriteriaCreator();

	/**
	 * The encoded criteria expression.
	 */
	private String encodedCriteria = "*";

	/**
	 * Creates a transition criteria factory bean that initially produces
	 * TransitionCriteria that always returns true.
	 */
	public TransitionCriteriaFactoryBean() {

	}

	/**
	 * Creates a transition criteria factory bean that produces
	 * TransitionCriteria that enforces the specified string expression.
	 * @param encodedCritera the encodedCriteria
	 */
	public TransitionCriteriaFactoryBean(String encodedCriteria) {
		setEncodedCriteria(encodedCriteria);
	}

	/**
	 * Set encoded criteria expression.
	 * @param encodedCriteria
	 */
	public void setEncodedCriteria(String encodedCriteria) {
		this.encodedCriteria = encodedCriteria;
	}

	/**
	 * Set the criteria creation strategy.
	 * @param criteriaCreator
	 */
	public void setCriteriaCreator(TransitionCriteriaCreator criteriaCreator) {
		this.criteriaCreator = criteriaCreator;
	}

	public void afterPropertiesSet() {
		Assert.hasText(encodedCriteria, "The encoded criteria expression string must have some text");
		Assert.notNull(criteriaCreator, "The criteria creator is required");
	}

	public Object getObject() throws Exception {
		return this.criteriaCreator.create(encodedCriteria);
	}

	public Class getObjectType() {
		return TransitionCriteria.class;
	}

	public boolean isSingleton() {
		return false;
	}
}