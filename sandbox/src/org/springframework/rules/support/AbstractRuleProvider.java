/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.support;

import org.springframework.rules.Rules;
import org.springframework.rules.RulesProvider;
import org.springframework.rules.constraint.property.PropertyConstraint;
import org.springframework.util.closure.Constraint;

/**
 * @author Keith Donald
 */
public abstract class AbstractRuleProvider extends DefaultRulesSource implements
		RulesProvider {

	private Rules rules = Rules.createRules(this.getClass());

	protected AbstractRuleProvider() {
		addRules(rules);
	}

	protected void add(String property, Constraint valueConstraint) {
		rules.add(property, valueConstraint);
	}

	public PropertyConstraint getRules(String propertyName) {
		return getRules(this.getClass(), propertyName);
	}
}