/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms.support;

/**
 * Information about a JMS Destination.  Queue and Topic subclasses
 * contain information specific to those desintation types.
 * @author Mark Pollack
 */
public abstract class DestinationInfo {

	/**
	 * The name of the destination
	 */
	private String _name;
	
	/**
	 * The array of JNDI names for the destination
	 */
	private String[] _jndiNames;
	
	
	
	
	/**
	 * The array of JNDI names for the destination
	 * @return the array of JNDI names for the destination or null if 
	 * there are no JNDI names for this destination.
	 */
	public String[] getJndiNames() {
		return _jndiNames;
	}

	/**
	 * Get the name of the destination.
	 * @return the name of the destination.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the JNDI names of the destination.
	 * @param names the JNDI names of the destination.
	 */
	public void setJndiNames(String[] names) {
		_jndiNames = names;
	}

	/**
	 * Set the name of the destination.
	 * @param name
	 */
	public void setName(String name) {
		_name = name;
	}

}
