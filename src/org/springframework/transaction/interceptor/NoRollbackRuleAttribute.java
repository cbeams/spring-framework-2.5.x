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
 * @version $Id: NoRollbackRuleAttribute.java,v 1.4 2003-12-23 08:44:53 johnsonr Exp $
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {
	
	/**
	 * Constrct a new NoRollbackRule for the given throwable class.
	 * @param clazz throwable class
	 */
	public NoRollbackRuleAttribute(Class clazz) {
		super(clazz);
	}

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
