/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util.visitor;

/**
 * Super interface to be implemented by objects that act as visitors.
 * <p><p>
 * This is a type interface and as a result does not define any public
 * methods.  It is here to provide some degree of type safety and description
 * to Vistable dispatchers and for dispatch by reflection.
 * 
 * @author  Keith Donald
 * @see Visitable
 * @see ReflectiveVisitorSupport
 */
public interface Visitor {
}
