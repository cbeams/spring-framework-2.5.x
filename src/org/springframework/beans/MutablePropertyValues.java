/*
 * Copyright 2002-2006 the original author or authors.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * Default implementation of the PropertyValues interface.
 * Allows simple manipulation of properties, and provides constructors
 * to support deep copy and construction from a Map.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 13 May 2001
 */
public class MutablePropertyValues implements PropertyValues, Serializable {

	/** List of PropertyValue objects */
	private final List propertyValueList;


	/**
	 * Creates a new empty MutablePropertyValues object.
	 * Property values can be added with the addPropertyValue methods.
	 * @see #addPropertyValue(PropertyValue)
	 * @see #addPropertyValue(String, Object)
	 */
	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList();
	}

	/**
	 * Deep copy constructor. Guarantees PropertyValue references
	 * are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 * @see #addPropertyValues(PropertyValues)
	 */
	public MutablePropertyValues(PropertyValues source) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (source != null) {
			PropertyValue[] pvs = source.getPropertyValues();
			this.propertyValueList = new ArrayList(pvs.length);
			for (int i = 0; i < pvs.length; i++) {
				PropertyValue newPv = new PropertyValue(pvs[i].getName(), pvs[i].getValue());
				this.propertyValueList.add(newPv);
			}
		}
		else {
			this.propertyValueList = new ArrayList(0);
		}
	}

	/**
	 * Construct a new PropertyValues object from a Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 * @see #addPropertyValues(Map)
	 */
	public MutablePropertyValues(Map source) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (source != null) {
			this.propertyValueList = new ArrayList(source.size());
			Iterator it = source.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				PropertyValue newPv = new PropertyValue((String) entry.getKey(), entry.getValue());
				this.propertyValueList.add(newPv);
			}
		}
		else {
			this.propertyValueList = new ArrayList(0);
		}
	}


	/**
	 * Copy all given PropertyValues into this object. Guarantees PropertyValue
	 * references are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 * @return this object to allow creating objects, adding multiple PropertyValues
	 * in a single statement
	 */
	public MutablePropertyValues addPropertyValues(PropertyValues source) {
		if (source != null) {
			PropertyValue[] pvs = source.getPropertyValues();
			for (int i = 0; i < pvs.length; i++) {
				PropertyValue newPv = new PropertyValue(pvs[i].getName(), pvs[i].getValue());
				addPropertyValue(newPv);
			}
		}
		return this;
	}

	/**
	 * Add all property values from the given Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 * @return this object to allow creating objects, adding multiple
	 * PropertyValues in a single statement
	 */
	public MutablePropertyValues addPropertyValues(Map source) {
		if (source != null) {
			Iterator it = source.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				PropertyValue newPv = new PropertyValue((String) entry.getKey(), entry.getValue());
				addPropertyValue(newPv);
			}
		}
		return this;
	}

	/**
	 * Add a PropertyValue object, replacing any existing one
	 * for the corresponding property.
	 * @param pv PropertyValue object to add
	 * @return this object to allow creating objects, adding multiple
	 * PropertyValues in a single statement
	 */
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = (PropertyValue) this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}

	/**
	 * Overloaded version of <code>addPropertyValue</code> that takes
	 * a property name and a property value.
	 * @param propertyName name of the property
	 * @param propertyValue value of the property
	 * @see #addPropertyValue(PropertyValue)
	 */
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	/**
	 * Modify a PropertyValue object held in this object.
	 * Indexed from 0.
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	/**
	 * Overloaded version of <code>removePropertyValue</code> that takes a property name.
	 * @param propertyName name of the property
	 * @see #removePropertyValue(PropertyValue)
	 */
	public void removePropertyValue(String propertyName) {
		removePropertyValue(getPropertyValue(propertyName));
	}

	/**
	 * Remove the given PropertyValue, if contained.
	 * @param pv the PropertyValue to remove
	 */
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	/**
	 * Clear this holder, removing all PropertyValues.
	 */
	public void clear() {
		this.propertyValueList.clear();
	}


	public PropertyValue[] getPropertyValues() {
		return (PropertyValue[])
				this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue pv = (PropertyValue) propertyValueList.get(i);
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null);
	}

	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}

	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// for each property value in the new set
		for (Iterator it = this.propertyValueList.iterator(); it.hasNext();) {
			PropertyValue newPv = (PropertyValue) it.next();
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			}
			else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}


	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuffer sb = new StringBuffer("PropertyValues: length=" + pvs.length + "; ");
		sb.append(StringUtils.arrayToDelimitedString(pvs, "; "));
		return sb.toString();
	}

}
