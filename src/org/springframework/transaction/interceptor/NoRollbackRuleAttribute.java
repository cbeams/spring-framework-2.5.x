/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

/**
 * Tag class. Its class means it has the opposite 
 * behaviour to the RollbackRule superclass.
 * @author Rod Johnson
 * @since 09-Apr-2003
 * @version $Id: NoRollbackRuleAttribute.java,v 1.2 2003-10-21 08:56:44 johnsonr Exp $
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {

	/**
	 * @param exceptionName
	 */
	public NoRollbackRuleAttribute(String exceptionName) {
		super(exceptionName);
	}
	
	public String toString() {
		return "No" + super.toString();
	}

}
