/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.4 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.validation.rules;

import java.util.Iterator;
import java.util.Set;

import org.springframework.functor.LogicalOperator;
import org.springframework.functor.predicates.CompoundUnaryPredicate;
import org.springframework.functor.predicates.PropertyPresent;
import org.springframework.functor.predicates.UnaryAnd;
import org.springframework.functor.predicates.UnaryOr;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class RequiredIfOthersPresent extends RequiredIfTrue {

    /**
     * Tests that the property is required if all "other properties" are
     * present. Present means they are "non null."
     * 
     * @param otherPropertyNames
     *            one or more other properties, delimited by commas.
     */
    public RequiredIfOthersPresent(String otherPropertyNames) {
        this(otherPropertyNames, LogicalOperator.AND);
    }

    /**
     * Tests that the property is required if all or any of the "other
     * properties" are present.
     * 
     * @param otherPropertyNames
     *            one or more other properties, delimited by commas.
     * @param operator
     *            the logical operator, either AND or OR.
     */
    public RequiredIfOthersPresent(String otherPropertyNames,
            LogicalOperator operator) {
        super();
        Assert.notNull(otherPropertyNames);
        Assert.notNull(operator);
        Set set = StringUtils.commaDelimitedListToSet(otherPropertyNames);
        Assert.hasElements(set);
        CompoundUnaryPredicate compoundPredicate;
        if (operator == LogicalOperator.AND) {
            compoundPredicate = new UnaryAnd();
        } else {
            compoundPredicate = new UnaryOr();
        }
        for (Iterator i = set.iterator(); i.hasNext();) {
            compoundPredicate.add(new PropertyPresent((String)i.next()));
        }
    }

}