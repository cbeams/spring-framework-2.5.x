/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/ValidationEvent.java,v 1.1 2004-06-15 22:59:21 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-15 22:59:21 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.reporting.ValidationResults;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class ValidationEvent {
    private FormModel formModel;

    private UnaryPredicate constraint;

    private ValidationResults results;

    public ValidationEvent(FormModel formModel, UnaryPredicate constraint) {
        this(formModel, constraint, null);
    }

    public ValidationEvent(FormModel formModel, UnaryPredicate constraint,
            ValidationResults results) {
        Assert.notNull(formModel);
        Assert.notNull(constraint);
        this.formModel = formModel;
        this.constraint = constraint;
        this.results = results;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ValidationEvent)) { return false; }
        ValidationEvent e = (ValidationEvent)o;
        return formModel.equals(e.formModel) && constraint.equals(e.constraint);
    }

    public int hashCode() {
        return formModel.hashCode() + constraint.hashCode();
    }

    public FormModel getFormModel() {
        return formModel;
    }

    public UnaryPredicate getConstraint() {
        return constraint;
    }

    public ValidationResults getResults() {
        return results;
    }
}