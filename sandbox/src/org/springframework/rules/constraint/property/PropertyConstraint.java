/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.constraint.property;

import org.springframework.util.closure.Constraint;

/**
 * A predicate that constrains a bean property in some way.
 *
 * @author Keith Donald
 */
public interface PropertyConstraint extends Constraint {

	/**
	 * Returns the constrained property name.
	 *
	 * @return The property name
	 */
	public String getPropertyName();

	/**
	 * Returns <code>true</code> if this property constraint is dependent on
	 * the provided propertyName for test evaluation; that is, it should be retested
	 * when propertyName changes.
	 * @param propertyName
	 * @return true or false
	 */
	public boolean tests(String propertyName);
}