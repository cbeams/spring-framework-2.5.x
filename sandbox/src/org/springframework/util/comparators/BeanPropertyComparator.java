/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.springframework.util.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * This comparator compares two beans by the specified bean property. It is also
 * possible to compare beans based on nested, indexed, combined, mapped bean
 * properties. Please see the {@link PropertyUtilsBean}documentation for all
 * property name possibilities.
 */
public class BeanPropertyComparator implements Comparator, Serializable {
    private String property;

    private Comparator comparator;

    /**
     * Constructs a Bean Comparator without a property set. <strong>Note
     * </strong> that this is intended to be used only in bean-centric
     * environments. Until {@link #setProperty}is called with a non-null value.
     * this comparator will compare the Objects only.
     */
    public BeanPropertyComparator() {
        this(null);
    }

    /**
     * Constructs a property-based comparator for beans. This compares two beans
     * by the property specified in the property parameter. This constructor
     * creates a <code>BeanComparator</code> that uses a
     * <code>ComparableComparator</code> to compare the property values.
     * 
     * Passing "null" to this constructor will cause the BeanComparator to
     * compare objects based on natural order, that is
     * <code>java.lang.Comparable</code>.
     * 
     * @param property
     *            String Name of a bean property, which may contain the name of
     *            a simple, nested, indexed, mapped, or combined property. See
     *            {@link PropertyUtilsBean}for property query language syntax.
     *            If the property passed in is null then the actual objects will
     *            be compared
     */
    public BeanPropertyComparator(String property) {
        this(property, ComparableComparator.instance());
    }

    /**
     * Constructs a property-based comparator for beans. This constructor
     * creates a BeanComparator that uses the supplied Comparator to compare the
     * property values.
     * 
     * @param property
     *            Name of a bean property, can contain the name of a simple,
     *            nested, indexed, mapped, or combined property.
     * @param comparator
     *            BeanComparator will pass the values of the specified bean
     *            property to this Comparator. If your bean property is not a
     *            comparable or contains null values, a suitable comparator may
     *            be supplied in this constructor.
     */
    public BeanPropertyComparator(String property, Comparator comparator) {
        setProperty(property);
        this.comparator = comparator;
    }

    /**
     * Sets the method to be called to compare two JavaBeans
     * 
     * @param property
     *            String method name to call to compare If the property passed
     *            in is null then the actual objects will be compared
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Gets the property attribute of the BeanComparator
     * 
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
     * 
     * @param o1
     *            Object The first bean to get data from to compare against
     * @param o2
     *            Object The second bean to get data from to compare
     * @return int negative or positive based on order
     */
    public int compare(Object o1, Object o2) {
        if (property == null) {
            // compare the actual objects
            return comparator.compare(o1, o2);
        }
        BeanWrapper wrapper = new BeanWrapperImpl(o1);
        wrapper.setWrappedInstance(o1);
        Object value1 = wrapper.getPropertyValue(property);

        wrapper.setWrappedInstance(o2);
        Object value2 = wrapper.getPropertyValue(property);
        return comparator.compare(value1, value2);
    }

}

