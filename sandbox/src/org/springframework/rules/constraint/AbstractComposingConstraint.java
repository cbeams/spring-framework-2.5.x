/*
 * $Header: /usr/local/cvs/module/src/java/File.java,v 1.7 2004/01/16 22:23:11
 * keith Exp $ $Revision: 1.1 $ $Date: 2004-10-12 07:13:28 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.constraint;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.factory.Constraints;
import org.springframework.util.closure.Constraint;

public abstract class AbstractComposingConstraint extends Constraints
        implements Constraint, Serializable {

    public boolean allTrue(Collection collection) {
        return allTrue(collection, this);
    }

    public boolean allTrue(Iterator it) {
        return allTrue(it, this);
    }

    public boolean anyTrue(Collection collection) {
        return anyTrue(collection, this);
    }

    public boolean anyTrue(Iterator it) {
        return anyTrue(it, this);
    }

    public Collection findAll(Collection collection) {
        return findAll(collection, this);
    }

    public Object findAll(Iterator it) {
        return findAll(it, this);
    }

    public Object findFirst(Collection collection) {
        return findFirst(collection, this);
    }

    public Object findFirst(Iterator it) {
        return findFirst(it, this);
    }
}