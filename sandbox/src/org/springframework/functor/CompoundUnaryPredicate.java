/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public abstract class CompoundUnaryPredicate implements UnaryPredicate {
    private Set predicates = new HashSet();

    public CompoundUnaryPredicate() {

    }

    public CompoundUnaryPredicate(UnaryPredicate predicate1,
            UnaryPredicate predicate2) {
        Assert.isTrue(predicate1 != null && predicate2 != null);
        predicates.add(predicate1);
        predicates.add(predicate2);
    }

    public CompoundUnaryPredicate(UnaryPredicate[] predicates) {
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public CompoundUnaryPredicate add(UnaryPredicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public Iterator iterator() {
        return predicates.iterator();
    }

    public abstract boolean evaluate(Object value);

    public void validateTypeSafety(final Class clazz) {
        UnaryPredicate predicate = new UnaryPredicate() {
            public boolean evaluate(Object o) {
                return o.getClass() != clazz || o.getClass() != this.getClass();
            }
        };
        Object violator = Algorithms.findFirst(predicates, predicate);
        if (violator != null) {
            throw new IllegalArgumentException(violator.getClass()
                    + " class not allowed");
        }
    }

}