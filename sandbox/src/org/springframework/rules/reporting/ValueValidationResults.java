package org.springframework.rules.reporting;

import org.springframework.rules.UnaryPredicate;

/**
 * @author Keith Donald
 */
public class ValueValidationResults implements ValidationResults {
    private Object argument;
    private UnaryPredicate violatedConstraint;
    
    public ValueValidationResults(Object argument, UnaryPredicate violatedConstraint) {
        this.argument = argument;
        this.violatedConstraint = violatedConstraint;
    }
    
    public ValueValidationResults(Object argument) {
        this.argument = argument;
    }
    
    /**
     * @see org.springframework.rules.reporting.ValidationResults#getRejectedValue()
     */
    public Object getRejectedValue() {
        return argument;
    }

    /**
     * @see org.springframework.rules.reporting.ValidationResults#getViolatedConstraint()
     */
    public UnaryPredicate getViolatedConstraint() {
        return violatedConstraint;
    }

    /**
     * @see org.springframework.rules.reporting.ValidationResults#getViolatedCount()
     */
    public int getViolatedCount() {
        if (violatedConstraint != null) {
            return new SummingVisitor(violatedConstraint).sum();
        } else {
            return 0;
        }
    }

    /**
     * @see org.springframework.rules.reporting.ValidationResults#getSeverity()
     */
    public Severity getSeverity() {
        return Severity.ERROR;
    }

}
