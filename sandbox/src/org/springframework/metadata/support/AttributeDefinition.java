/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class that represents the results of parsing the Javadoc text 
 * that is a candidate for attribute usage.  There are methods to determine
 * if the parsing was successfull, return the name of the attribute class, 
 * the list of constructor arguments, and the list of JavaBean properties.
 * 
 * @author Mark Pollack
 * @since September 28, 2003
 * 
 */
public class AttributeDefinition {

	/**
	 * Was the text correctly parsed?
	 */
	private boolean isValid;
	
	/**
	 * The classname of the attribute.
	 */
	private String clazzName = "<No Class>";

	/**
	 * The list of constructor arguments.
	 */
	private List ctorList = new ArrayList();

	/**
	 * The map of JavaBean properties
	 */
	private Map propertyMap = new HashMap();

	/**
	 * Error Text
	 */
	private String errorText;
	
	/**
	 * The raw attribute text
	 */
	private String attributeText;

	/**
	 * Create a AttributeDefinition in the case of parse errors.  The class
	 * with then return false for validity checks and the supplied error
	 * string for the error text.  An empty list will be returned for
	 * the constructor list and an empty map for the attribute properties.
	 * The classname "<No Class>" will be returned for the classname.
	 * @param attribText The raw attribute text
	 * @param parseErrorText  The descriptive error of why the attribute
	 * text could not be parsed.	
	 */
	public AttributeDefinition(String attribText, 
						   String parseErrorText) {
		this.attributeText = attribText;						   	
		this.errorText = parseErrorText;							
	}

	/**
	 * Create a AttributeDefinition class, initializing the
	 * underlying storage.  Null values for the constructor arguments
	 * and JavaBean properties can be passed, this will create an  
	 * empty list and map.
	 * @param the raw attribute text
	 * @param valid If the attribute text was parsed successfully.
	 * @param clazzName The classname of the attribute.  Must not be null.
	 * @param ctorArgs a list of constructor arguments to use when creating
	 * the attribute.  If a null reference is passed, an empty list will
	 * be created.
	 * @param props a map of JavaBean properties to use when creating the
	 * attribute.  If a null reference is passed, an empty map will be
	 * created.
	 * 
	 * @throws IllegalArgumentException thrown if the String for the 
	 * classname or attributeText is null
	 */
	public AttributeDefinition(
		String attribText,
		boolean valid,
		String clazzName,
		List ctorArgs,
		Map props) {
			
		if (attribText == null) {
			throw new IllegalArgumentException("The value of the attribute" +
			 " text must not be NULL");	
		}
		this.attributeText = attribText;
		this.isValid = valid;
		if (clazzName == null) {
			throw new IllegalArgumentException("The value of the attribute" +
			" classname must not be NULL");
		}
		this.clazzName = clazzName;
		if (ctorArgs != null) {
			this.ctorList = ctorArgs;
		}
		if (props != null) {
			this.propertyMap = props;
		}

	}

	/**
	 * Return the raw attribute text.
	 * @return the raw attribute text.
	 */
	public String getAttributeText() {
		return this.attributeText; 
	}
	

	/**
	 * If the attribute was successfully parsed, return true, false otherwise.
	 * @return true if successfully parsed, false otherwise.
	 */
	public boolean isValid() {
		return this.isValid;
	}
	
	/**
	 * Return the classname of the attribute.
	 * @return the classname of the attribute.  In case of a parse error, the
	 * value "<No Class>" will be returned.
	 */
	public String getClassname() {
		return this.clazzName;
	}
	
	/**
	 * Return the list of constructor arguments for the attribute.   
	 * @return The list of constructor arguments for the attribute.  If there
	 * were no constructor arguments or a parse error an 
	 * empty list is returned.
	 */
	public List getConstructorArgs() {
		return this.ctorList;
	}

	/**
	 * Return the map of JavaBean properties for the attribute.
	 *  
	 * @return The map of JavaBean properties for the attribute.  If there
	 * were no properties or a parse error an empty map is returned. 
	 */
	public Map getProperties() {
		return this.propertyMap;
	}
	
	/**
	 * In case the attribute text was not parsable, this method will return
	 * an error description.
	 * @return A description of why the attribute text could not be parsed.
	 */
	public String getErrorText() {
		return this.errorText;
	}
}
