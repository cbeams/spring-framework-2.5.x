/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

/**
 * Tag class. Its class means it has the opposite behaviour to the
 * RollbackRule superclass.
 * @author Rod Johnson
 * @since 09-Apr-2003
 * @version $Id: NoRollbackRuleAttribute.java,v 1.3 2003-11-27 18:36:18 jhoeller Exp $
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {

	/**
	 * Construct a new NoRollbackRule for the given exception name.
	 * This can be a substring, with no wildcard support at present.
	 * A value of "ServletException" would match ServletException and
	 * subclasses, for example.
	 * @param exceptionName the exception pattern
	 */
	public NoRollbackRuleAttribute(String exceptionName) {
		super(exceptionName);
	}
	
	public String toString() {
		return "No" + super.toString();
	}

}
