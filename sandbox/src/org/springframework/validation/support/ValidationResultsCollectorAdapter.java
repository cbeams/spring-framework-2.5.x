/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringBuilder;
import org.springframework.validation.PropertyValidationRule;
import org.springframework.validation.PropertyValidator;
import org.springframework.validation.ValidationResultsCollector;

/**
 * Convenience implementation of the ValidationResultsCollector interface.
 * Subclasses may override any or all of the validation result callbacks.
 * 
 * This implementation conveniently keeps track of the current nestedPath
 * during each validation run.
 * 
 * @author Keith Donald
 */
public class ValidationResultsCollectorAdapter implements
        ValidationResultsCollector {
    protected Log logger = LogFactory
            .getLog(ValidationResultsCollectorAdapter.class);
    private Stack nestedPath = new Stack();

    protected String getFullNestedPath() {
        return getNestedPath(0);
    }

    protected String getNestedPath() {
        return getNestedPath(1);
    }

    private String getNestedPath(int index) {
        if (index >= nestedPath.size()) {
            return null;
        }
        StringBuffer pathString = new StringBuffer(64);
        for (Iterator i = nestedPath.listIterator(index); i.hasNext();) {
            pathString.append(i.next());
            if (i.hasNext()) {
                pathString.append('.');
            }
        }
        return pathString.toString();
    }

    public void beanValidationStarted(Object bean) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bean validator '" + bean + "' starting...");
        }
        nestedPath.push(getObjectName(bean));
    }

    protected String getObjectName(Object bean) {
        return StringUtils.uncapitalize(ClassUtils
                .getShortName(bean.getClass()));
    }

    public void beanValidationCompleted(Object bean) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bean validator '" + bean + "' completed.");
        }
        nestedPath.pop();
    }

    public void propertyValidationStarted(PropertyValidator validator,
            Object bean, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Property validator '" + validator.getPropertyName()
                    + "' starting...");
        }
        nestedPath.push(validator.getPropertyName());
    }

    public void propertyValidationCompleted(PropertyValidator validator,
            Object bean, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Property validator '" + validator.getPropertyName()
                    + "' completed.");
        }
        nestedPath.pop();
    }

    public void validationErrorOccured(PropertyValidator validator,
            PropertyValidationRule rule, Object bean, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Property validator '" + validator.getPropertyName()
                    + "' reported error, caused by rule violation '" + rule);
        }
    }

    public void reset() {
        nestedPath.clear();
    }

    public String toString() {
        return new ToStringBuilder(this).append("nestedPath", nestedPath)
                .toString();
    }
}