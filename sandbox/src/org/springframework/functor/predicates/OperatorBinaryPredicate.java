/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/functor/predicates/OperatorBinaryPredicate.java,v 1.1 2004-03-30 00:32:19 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-03-30 00:32:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.predicates;

import java.util.Comparator;

import org.springframework.functor.BinaryPredicate;

public abstract class OperatorBinaryPredicate implements BinaryPredicate {
    private Comparator comparator;
    
    public OperatorBinaryPredicate() {
        
    }
    
    public OperatorBinaryPredicate(Comparator comparator) {
        this.comparator = comparator;
    }
    
    public boolean evaluate(Object value1, Object value2) {
        if (comparator != null) {
            return evaluateOperatorResult(this.comparator.compare(value1, value2));
        } else {
            Comparable c1 = (Comparable)value1;
            Comparable c2 = (Comparable)value2;
            return evaluateOperatorResult(c1.compareTo(c2));
        }
    }
    
    public abstract boolean evaluateOperatorResult(int result);

}
