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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.Rules;
import org.springframework.rules.RulesSource;
import org.springframework.rules.constraints.beans.BeanPropertyConstraint;
import org.springframework.rules.factory.Constraints;
import org.springframework.util.ToStringBuilder;

/**
 * A default rules source implementation which is simply a in-memory registry
 * for bean validation rules backed by a map.
 * 
 * @author Keith Donald
 */
public class DefaultRulesSource extends Constraints implements RulesSource {
    private static final Log logger = LogFactory
            .getLog(DefaultRulesSource.class);

    private Map rules = new HashMap();

    /**
     * Add or update the rules for a single bean class.
     * 
     * @param rules
     *            The rules.
     */
    public void addRules(Rules rules) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding rules -> " + rules);
        }
        this.rules.put(rules.getBeanClass(), rules);
    }

    /**
     * Set the list of rules retrievable by this source, where each item in the
     * list is a <code>Rules</code> object which maintains validation rules
     * for a bean class.
     * 
     * @param rules
     *            The list of rules.
     */
    public void setRules(List rules) {
        if (logger.isDebugEnabled()) {
            logger.debug("Configuring rules in source...");
        }
        this.rules.clear();
        for (Iterator i = rules.iterator(); i.hasNext();) {
            addRules((Rules)i.next());
        }
    }

    public Rules getRules(Class bean) {
        return (Rules)rules.get(bean);
    }

    public BeanPropertyConstraint getRules(Class bean, String propertyName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving rules for bean '" + bean + "', property '"
                    + propertyName + "'");
        }
        Rules rules = (Rules)this.rules.get(bean);
        if (rules != null) {
            return rules.getRules(propertyName);
        }
        else {
            return null;
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("rules", rules).toString();
    }

}