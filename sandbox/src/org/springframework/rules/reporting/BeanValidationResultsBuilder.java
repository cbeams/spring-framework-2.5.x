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
package org.springframework.rules.reporting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.functions.GetProperty;

/**
 * @author Keith Donald
 */
public class BeanValidationResultsBuilder extends ValidationResultsBuilder
        implements ValidationResults {
    private String currentProperty;
    private Map beanResults = new HashMap();
    private GetProperty getProperty;

    public BeanValidationResultsBuilder(Object bean) {
        getProperty = new GetProperty(bean);
    }

    public Map getResults() {
        return Collections.unmodifiableMap(beanResults);
    }

    public PropertyResults getResults(String propertyName) {
        return (PropertyResults)beanResults.get(propertyName);
    }

    public int getViolatedCount() {
        int count = 0;
        Iterator it = beanResults.values().iterator();
        while (it.hasNext()) {
            count += ((PropertyResults)it.next()).getViolatedCount();
        }
        return count;
    }

    protected void constraintViolated(UnaryPredicate constraint) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Done] collecting results for property '"
                    + getPropertyName() + "'.  Constraints violated: ["
                    + constraint + "]");
        }
        PropertyResults results = new PropertyResults(getPropertyName(),
                getProperty.evaluate(getPropertyName()), constraint);
        beanResults.put(getPropertyName(), results);
    }

    protected void constraintSatisfied() {
        if (logger.isDebugEnabled()) {
            logger.debug("[Done] collecting results for property '"
                    + getPropertyName() + "'.  All constraints met.");
        }
    }

    /**
     * @return Returns the propertyName.
     */
    public String getPropertyName() {
        return currentProperty;
    }

    /**
     * @param propertyName
     *            the propertyName to set.
     */
    public void setPropertyName(String propertyName) {
        this.currentProperty = propertyName;
        clear();
    }
}