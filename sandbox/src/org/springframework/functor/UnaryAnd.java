/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Keith Donald
 */
public class UnaryAnd extends CompoundUnaryPredicate implements UnaryPredicate {

    public Set predicates = new HashSet();

    public UnaryAnd() {
        super();
    }

    public UnaryAnd(UnaryPredicate predicate1, UnaryPredicate predicate2) {
        super(predicate1, predicate2);
    }

    public UnaryAnd(UnaryPredicate[] predicates) {
        super(predicates);
    }

    public boolean evaluate(Object value) {
        for (Iterator i = iterator(); i.hasNext();) {
            if (!((UnaryPredicate)i.next()).evaluate(value)) {
                return false;
            }
        }
        return true;
    }

}