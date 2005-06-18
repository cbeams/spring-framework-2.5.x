/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.io.Serializable;
import java.util.Comparator;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.comparator.ComparableComparator;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * Comparator that compares two beans by the specified bean property. It is also
 * possible to compare beans based on nested, indexed, combined, or mapped bean
 * properties.
 *
 * @author Keith Donald
 */
public class PropertyComparator implements Comparator, Serializable {

	private String property;

	private Comparator comparator;

	//@TODO favor PropertyAccessStrategy interface
	private transient BeanWrapper beanWrapper;


	/**
	 * Constructs a PropertyComparator without a property set. Until
	 * {@link #setProperty}is called with a non-null value, this comparator
	 * will compare the beans by natural-order only.
	 */
	public PropertyComparator() {
		this(null);
	}

	/**
	 * Constructs a property-based comparator for beans. This compares two beans
	 * by the property specified in the property parameter. This constructor
	 * creates a <code>PropertyComparator</code> that uses a
	 * <code>ComparableComparator</code> to compare the property values.
	 *
	 * Passing <code>null</code> to this constructor will cause the
	 * PropertyComparator to compare beans based on natural order, that is
	 * <code>java.lang.Comparable</code>.
	 * @param property String Name of a bean property, which may contain the name of
	 * a simple, nested, indexed, mapped, or combined property. If
	 * the property passed in is null then the actual objects, which
	 * must be comparables in this case, will be compared.
	 */
	public PropertyComparator(String property) {
		this(property, new NullSafeComparator(new ComparableComparator(), true));
	}

	/**
	 * Constructs a property-based comparator for beans. This constructor
	 * creates a PropertyComparator that uses the supplied Comparator to
	 * compare the property values.
	 * @param property Name of a bean property; may contain the name of a simple,
	 * nested, indexed, mapped, or combined property.
	 * @param comparator PropertyComparator will pass the values of the specified
	 * bean property to this Comparator.
	 */
	public PropertyComparator(String property, Comparator comparator) {
		setProperty(property);
		this.comparator = comparator;
	}


	/**
	 * Sets the method to be called to compare two JavaBeans
	 * @param property String method name to call to compare If the property passed
	 * in is null then the actual objects will be compared
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Gets the property attribute of the BeanComparator
	 * @return String method name to call to compare. A null value indicates
	 *         that the actual objects will be compared
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Gets the Comparator being used to compare beans.
	 */
	public Comparator getComparator() {
		return comparator;
	}


	/**
	 * Compare two JavaBeans by their shared property. If {@link #getProperty}
	 * is null then the actual objects will be compared.
	 * @param o1 Object The first bean to get data from to compare against
	 * @param o2 Object The second bean to get data from to compare
	 * @return int negative or positive based on order
	 */
	public int compare(Object o1, Object o2) {
		if (property == null) {
			return comparator.compare(o1, o2);
		}
		if (beanWrapper == null) {
			beanWrapper = new BeanWrapperImpl(o1);
		}
		else {
			beanWrapper.setWrappedInstance(o1);
		}
		Object value1 = beanWrapper.getPropertyValue(property);
		beanWrapper.setWrappedInstance(o2);
		Object value2 = beanWrapper.getPropertyValue(property);
		return comparator.compare(value1, value2);
	}

	public boolean equals(Object o) {
		if (!(o instanceof PropertyComparator)) {
			return false;
		}
		PropertyComparator c = (PropertyComparator) o;
		return ObjectUtils.nullSafeEquals(property, c.property) && comparator.equals(c.comparator);
	}

	public int hashCode() {
		int hash = (property != null ? property.hashCode() : -1);
		return hash += comparator.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("property", property).append("comparator", comparator).toString();
	}

}
