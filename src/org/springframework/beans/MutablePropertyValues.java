
/*
 * Copyright 2002-2004 the original author or authors.
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
 * @author Rod Johnson
 * @since 13 May 2001
 * @version $Id: MutablePropertyValues.java,v 1.8 2004-05-18 07:36:40 jhoeller Exp $
 */
public class MutablePropertyValues implements PropertyValues, Serializable {

	/** List of PropertyValue objects */
	private final List propertyValuesList = new ArrayList();

	/**
	 * Creates a new empty MutablePropertyValues object.
	 * Property values can be added with the addPropertyValue methods.
	 * @see #addPropertyValue(PropertyValue)
	 * @see #addPropertyValue(String, Object)
	 */
	public MutablePropertyValues() {
	}

	/**
	 * Deep copy constructor. Guarantees PropertyValue references
	 * are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 * @see #addPropertyValues(PropertyValues)
	 */
	public MutablePropertyValues(PropertyValues source) {
		addPropertyValues(source);
	}

	/**
	 * Construct a new PropertyValues object from a Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 * @see #addPropertyValues(Map)
	 */
	public MutablePropertyValues(Map source) {
		addPropertyValues(source);
	}

	/**
	 * Copy all given PropertyValues into this object. Guarantees PropertyValue
	 * references are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 */
	public void addPropertyValues(PropertyValues source) {
		if (source != null) {
			PropertyValue[] pvs = source.getPropertyValues();
			for (int i = 0; i < pvs.length; i++) {
				addPropertyValue(new PropertyValue(pvs[i].getName(), pvs[i].getValue()));
			}
		}
	}

	/**
	 * Add all property values from the given Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 */
	public void addPropertyValues(Map source) {
		if (source != null) {
			Iterator it = source.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				addPropertyValue(new PropertyValue(key, source.get(key)));
			}
		}
	}

	/**
	 * Add a PropertyValue object, replacing any existing one
	 * for the respective property.
	 * @param pv PropertyValue object to add
	 */
	public void addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValuesList.size(); i++) {
			PropertyValue currentPv = (PropertyValue) this.propertyValuesList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				this.propertyValuesList.set(i, pv);
				return;
			}
		}
		this.propertyValuesList.add(pv);
	}

	/**
	 * Overloaded version of addPropertyValue that takes
	 * a property name and a property value.
	 * @param propertyName name of the property
	 * @param propertyValue value of the property
	 * @see #addPropertyValue(PropertyValue)
	 */
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	/**
	 * Remove the given PropertyValue, if contained.
	 * @param pv the PropertyValue to remove
	 */
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValuesList.remove(pv);
	}

	/**
	 * Overloaded version of removePropertyValue that takes
	 * a property name.
	 * @param propertyName name of the property
	 * @see #removePropertyValue(PropertyValue)
	 */
	public void removePropertyValue(String propertyName) {
		removePropertyValue(getPropertyValue(propertyName));
	}

	/**
	 * Modify a PropertyValue object held in this object.
	 * Indexed from 0.
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValuesList.set(i, pv);
	}

	public PropertyValue[] getPropertyValues() {
		return (PropertyValue[]) this.propertyValuesList.toArray(new PropertyValue[0]);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		for (int i = 0; i < this.propertyValuesList.size(); i++) {
			PropertyValue pv = (PropertyValue) this.propertyValuesList.get(i);
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	public boolean contains(String propertyName) {
		return getPropertyValue(propertyName) != null;
	}

	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this)
			return changes;

		// for each property value in the new set
		for (int i = 0; i < this.propertyValuesList.size(); i++) {
			PropertyValue newPv = (PropertyValue) this.propertyValuesList.get(i);
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
		StringBuffer sb = new StringBuffer("MutablePropertyValues: length=" + pvs.length + "; ");
		sb.append(StringUtils.arrayToDelimitedString(pvs, ","));
		return sb.toString();
	}

}
