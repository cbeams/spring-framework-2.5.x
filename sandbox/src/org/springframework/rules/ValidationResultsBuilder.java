/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.predicates.CompoundUnaryPredicate;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;

/**
 * @author Keith Donald
 */
public class ValidationResultsBuilder implements ValidationResults {
    private static final Log logger =
        LogFactory.getLog(ValidationResultsBuilder.class);    
    private String currentProperty;
    private Map propertyResults = new HashMap();
    private UnaryPredicate top;
    private Stack levels = new Stack();

    public Map getResults() {
        return Collections.unmodifiableMap(propertyResults);
    }

    public UnaryPredicate getResults(String propertyName) {
        return (UnaryPredicate)propertyResults.get(propertyName);
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
    }

    public void pushAnd() {
        UnaryAnd and = new UnaryAnd();
        add(and);
    }

    public void pushOr() {
        UnaryOr or = new UnaryOr();
        add(or);
    }

    public void pushNot() {
        UnaryNot not = new UnaryNot();
        add(not);
    }

    private void add(UnaryPredicate predicate) {
        if (top != null) {
            if (top instanceof UnaryNot) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Negating predicate [" + predicate + "]");
                }
                ((UnaryNot)top).setPredicate(predicate);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Aggregating nested predicate [" + predicate + "]");
                }
                ((CompoundUnaryPredicate)top).add(predicate);
            }
        }
        levels.push(predicate);
        this.top = predicate;
        if (logger.isDebugEnabled()) {
            logger.debug("Predicate [" + predicate + "] is at the top.");
        }
    }

    public void push(UnaryPredicate constraint) {
        if (this.top instanceof CompoundUnaryPredicate) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding constraint [" + constraint + "]");
            }
            ((CompoundUnaryPredicate)this.top).add(constraint);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Negating constraint [" + constraint + "]");
            }
            ((UnaryNot)this.top).setPredicate(constraint);
        }
    }

    public void pop() {
        this.top = (UnaryPredicate)levels.peek();
        levels.pop();
        if (logger.isDebugEnabled()) {
            logger.debug("Top popped; stack now has " + levels.size() + " elements");
        }
        if (levels.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "[Done] collecting results for property '"
                        + getPropertyName()
                        + "'.  Results are ["
                        + top
                        + "]");
            }
            propertyResults.put(getPropertyName(), top);
        }
    }

}
