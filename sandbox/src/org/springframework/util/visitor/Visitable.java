/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util.visitor;

/**
 * The super vistable interface of the visitor design pattern.
 * 
 * @author Keith Donald
 */
public interface Visitable {

    /**
     * Accept a visitor and perform a dispatch. Within accept(), Vistables pass
     * themselves back to the visitor. The visitor then executes an
     * encapsulated algorithm unique to that type of Visitable object. As an
     * alternative to defining Vistable implementations, consider using
     * ReflectiveVisitorSupport.
     * 
     * @param visitor
     *            The visitor to accept.
     */
    public void accept(Visitor visitor);
}
