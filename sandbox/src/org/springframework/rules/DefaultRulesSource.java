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
package org.springframework.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ToStringBuilder;

/**
 * @author Keith Donald
 */
public class DefaultRulesSource implements RulesSource {
    private static final Log logger = LogFactory
            .getLog(DefaultRulesSource.class);
    private Map rules = new HashMap();

    public void addRules(Rules rules) {
        this.rules.put(rules.getBeanClass(), rules);
    }

    public void setBeanRules(List rules) {
        for (Iterator i = rules.iterator(); i.hasNext();) {
            Rules r = (Rules)i.next();
            this.rules.put(r.getBeanClass(), r);
        }
    }

    /**
     * @see org.springframework.rules.RulesSource#getRules(java.lang.Class)
     */
    public Rules getRules(Class bean) {
        return (Rules)rules.get(bean);
    }

    /**
     * @see org.springframework.rules.RulesSource#getRules(java.lang.Class,
     *      java.lang.String)
     */
    public BeanPropertyExpression getRules(Class bean, String propertyName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving rules for bean '" + bean + "', property '"
                    + propertyName + "'");
        }
        Rules rules = (Rules)this.rules.get(bean);
        if (rules != null) {
            return rules.getRules(propertyName);
        } else {
            return null;
        }
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("rules", rules).toString();
    }

}