/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.functor.CompoundUnaryPredicate;
import org.springframework.functor.LogicalOperator;
import org.springframework.functor.UnaryAnd;
import org.springframework.functor.UnaryOr;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.predicates.PropertyPresent;
import org.springframework.functor.predicates.Required;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Validates property value as 'required' if and only if some other properties
 * are also provided.
 * 
 * @author Seth Ladd
 * @author Keith Donald
 */
public class RequiredIfOthersPresent implements UnaryPredicate {
    private String propertyName;
    private CompoundUnaryPredicate compoundPredicate;

    public RequiredIfOthersPresent(String otherPropertyNames) {
        this(otherPropertyNames, LogicalOperator.AND);
    }

    public RequiredIfOthersPresent(String otherPropertyNames,
            LogicalOperator operator) {
        Assert.notNull(otherPropertyNames);
        Assert.notNull(operator);
        Set set = StringUtils.commaDelimitedListToSet(otherPropertyNames);
        Assert.hasElements(set);
        if (operator == LogicalOperator.AND) {
            this.compoundPredicate = new UnaryAnd();
        } else {
            this.compoundPredicate = new UnaryOr();
        }
        for (Iterator i = set.iterator(); i.hasNext();) {
            compoundPredicate.add(new PropertyPresent((String)i.next()));
        }
    }

    public RequiredIfOthersPresent(CompoundUnaryPredicate compoundPredicate) {
        Assert.notNull(compoundPredicate);
        compoundPredicate.validateTypeSafety(PropertyPresent.class);
        this.compoundPredicate = compoundPredicate;
    }

    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public boolean evaluate(Object bean) {
        Assert.notNull(propertyName);
        boolean present = compoundPredicate.evaluate(bean);
        if (present) {
            return Required.instance().evaluate(
                    new BeanWrapperImpl(bean).getPropertyValue(propertyName));
        } else {
            return true;
        }
    }

}