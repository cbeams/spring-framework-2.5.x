/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata;

import org.springframework.metadata.support.*;

/**
 * The AttributeParser is responsible for determining if a javadoc style text
 * comments represents the usage of an attribute.  It parses the javadoc text
 * into a helper class, AttributeDefinition, so that constructor and 
 * JavaBean property values can be easily used to create the attribute.
 *
 * @author Mark Pollack
 * 
 */
public interface AttributeParser {

	/**
	 * Set the text to be parsed by ext, taken from the javadoc tag, 
	 * @param attributeText the attribute/tag text from javadoc
	 */
	AttributeDefinition getAttributeDefinition(String attributeText);
}
