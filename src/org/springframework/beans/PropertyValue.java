package org.springframework.beans;

/**
 * Class to hold information and value for an individual property.
 * Using an object here, rather than just storing all properties in a
 * map keyed by property name, allows for more flexibility, and the
 * ability to handle indexed properties etc in a special way if necessary.
 *
 * <p>Note that the value doesn't need to be the final required type:
 * A BeanWrapper implementation should handle any necessary conversion, as
 * this object doesn't know anything about the objects it will be applied to.
 *
 * @author Rod Johnson
 * @since 13 May 2001
 * @version $Id: PropertyValue.java,v 1.2 2004-02-13 08:37:43 jhoeller Exp $
 */
public class PropertyValue {

	/** Property name */
	private String name;

	/** Value of the property */
	private Object value;

	/**
	 * Creates new PropertyValue.
	 * @param name name of the property
	 * @param value value of the property (possibly before type conversion)
	 */
	public PropertyValue(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("Property name cannot be null");
		}
		this.name = name;
		this.value = value;
	}

	/**
	 * Return the name of the property.
	 * @return the name of the property
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of the property.
	 * <p>Note that type conversion will <i>not</i> have occurred here.
	 * It is the responsibility of the BeanWrapper implementation to
	 * perform type conversion.
	 * @return the value of the property
	 */
	public Object getValue() {
		return value;
	}

	public String toString() {
		return "PropertyValue: name='" + name + "'; value=[" + value + "]";
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				((this.value == null && otherPv.value == null) || this.value.equals(otherPv.value)));
	}

	public int hashCode() {
		return this.name.hashCode() * 29 + (this.value != null ? this.value.hashCode() : 0);
	}

}
