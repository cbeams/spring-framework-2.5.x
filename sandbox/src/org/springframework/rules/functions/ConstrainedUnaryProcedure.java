/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules.functions;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;

/**
 * Only execute the specified procedure if a provided constraint is also true.
 * 
 * @author keith
 */
public class ConstrainedUnaryProcedure implements UnaryProcedure {
    private UnaryProcedure procedure;
    private UnaryPredicate constraint;

    public ConstrainedUnaryProcedure(UnaryProcedure procedure,
            UnaryPredicate constraint) {
        this.procedure = procedure;
        this.constraint = constraint;
    }

    /**
     * Will only invoke the procedure against the provided argument if the
     * constraint permits; else no action will be taken.
     * 
     * @see org.springframework.rules.UnaryProcedure#run(java.lang.Object)
     */
    public void run(Object argument) {
        if (constraint.test(argument)) {
            procedure.run(argument);
        }
    }

}