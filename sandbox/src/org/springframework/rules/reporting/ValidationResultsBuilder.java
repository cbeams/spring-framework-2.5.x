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

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.predicates.CompoundUnaryPredicate;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.util.ToStringBuilder;

/**
 * @author Keith Donald
 */
public abstract class ValidationResultsBuilder {
    protected static final Log logger = LogFactory
            .getLog(ValidationResultsBuilder.class);
    private UnaryPredicate top;
    private Stack levels = new Stack();

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
                ((UnaryNot)this.top).setPredicate(predicate);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Aggregating nested predicate [" + predicate
                            + "]");
                }
                ((CompoundUnaryPredicate)this.top).add(predicate);
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
        } else if (this.top instanceof UnaryNot) {
            if (logger.isDebugEnabled()) {
                logger.debug("Negating constraint [" + constraint + "]");
            }
            ((UnaryNot)this.top).setPredicate(constraint);
        } else if (this.top == null) {
            constraintViolated(constraint);
        } else {
            throw new IllegalArgumentException(constraint.toString());
        }
    }

    public void pop(boolean result) {
        UnaryPredicate p = (UnaryPredicate)levels.pop();
        if (logger.isDebugEnabled()) {
            logger.debug("Top [" + p + "] popped; result was " + result
                    + "; stack now has " + levels.size() + " elements");
        }
        if (levels.isEmpty()) {
            if (!result) {
                constraintViolated(top);
            } else {
                constraintSatisfied();
            }
            top = null;
        } else {
            this.top = (UnaryPredicate)levels.peek();
            if (result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing compound predicate [" + p
                            + "]; tested true.");
                }
                ((CompoundUnaryPredicate)this.top).remove(p);
            }
        }
    }

    protected void clear() {
        levels.clear();
        top = null;
    }

    public boolean negated() {
        if (levels.size() == 0) {
            return false;
        }
        return peek() instanceof UnaryNot;
    }
    
    private UnaryPredicate peek() {
        return (UnaryPredicate)levels.peek();
    }

    protected abstract void constraintViolated(UnaryPredicate constraint);
    protected abstract void constraintSatisfied();

    public String toString() {
        return new ToStringBuilder(this).append("topOfStack", top).append(
                "levelsStack", levels).toString();
    }

}