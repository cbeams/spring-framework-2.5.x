/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

/**
 * @author Keith Donald
 */
public class UnaryComposePredicate implements UnaryPredicate {
    private UnaryPredicate predicate;
    private UnaryFunction function;

    public UnaryComposePredicate(UnaryPredicate predicate,
            UnaryFunction function) {
        this.predicate = predicate;
        this.function = function;
    }

    public boolean evaluate(Object value) {
        return this.predicate.evaluate(function.execute(value));
    }

}