/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/functor/predicates/BindSecond.java,v 1.1 2004-03-30 00:32:19 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-03-30 00:32:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.predicates;

import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.UnaryPredicate;

/**
 * @author Keith Donald
 */
public class BindSecond implements UnaryPredicate {
    private BinaryPredicate predicate;
    private Object constant;
    
    public BindSecond(BinaryPredicate predicate, Object constant) {
        this.predicate = predicate;
        this.constant = constant;
    }
    
    public boolean evaluate(Object value) {
        return predicate.evaluate(value, this.constant);
    }

}
