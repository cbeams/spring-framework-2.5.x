/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * This is the interface for accessing attributes at runtime.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * @since Sep 30, 2003
 * @version $Id: Attributes.java,v 1.1 2003-12-12 16:54:29 johnsonr Exp $
 */
public interface Attributes {
    
    /**
     * Return the class attributes of the target class
     * @param targetClass the class that contains attribute information
     * @return a list of attributes, possibly an empty list, never null.
     */
    Collection getAttributes(Class targetClass);
    
    /**
     * Return the class attributes of the target class of a given type. 
     * The class attributes are filtered by providing a java.lang.Class
     * reference to indicate their type.   This is useful if you know the
     * type the attribute you are looking for and don't want to sort 
     * through the unfiltered Collection yourself.
     * 
     * @param targetClass the class that contains attribute information.
     * @param filter specify that only this type of class should be returned.
     * @return return only the Collection of attributes that are of the filter type.
     */
    Collection getAttributes(Class targetClass, Class filter);
    
    /**
     * Return the method attributes of the target method.
     * @param targetMethod the method that contains attribute information.
     * @return a Collection of attriubtes, possibly an empty Collection, never null.
     */
    Collection getAttributes(Method targetMethod);
 
 	/**
 	 * Return the method attributes of the target method of a given type.
 	 * The method attributes are filtered by providing a java.lang.Class
 	 * reference to indicate the type to filter on.  Ths is useful if you know
 	 * the type of the attribute you are looking for and don't want to sort
 	 * through the unfiltered Collection yourself.
 	 * @param targetMethod
 	 * @param filter
 	 * @return a Collection of attributes, possibly an empty Collection, never null.
 	 */
 	Collection getAttributes(Method targetMethod, Class filter);

	/**
	 * Return the field attributes of the target field.
	 * @param targetField the field that contains attribute information
	 * @return a Collection of attribute, possibly an empty Collection, never null.
	 */
	Collection getAttributes(Field targetField);   
	
	/**
 	 * Return the field attributes of the target method of a given type.
 	 * The field attributes are filtered by providing a java.lang.Class
 	 * reference to indicate the type to filter on.  Ths is useful if you know
 	 * the type of the attribute you are looking for and don't want to sort
 	 * through the unfiltered Collection yourself.
 	 * @param targetField
 	 * @param filter
 	 * @return a Collection of attributes, possibly an empty Collection, never null.
	 */
	Collection getAttributes(Field targetField, Class filter);
	
	/**
	 * Set the Collection of attribute packages that will be prepended to
	 * the attribute classnames in order to create a fully qualified classname.
	 * @param packages Collection of attribute packages to prepend to the 
	 * attribute classname.
	 */
	//void setAttributePackages(String[] packages);
}