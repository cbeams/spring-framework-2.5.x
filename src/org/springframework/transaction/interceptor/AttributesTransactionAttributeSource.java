/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.metadata.Attributes;

/**
 * Implementation of TransactionAttributeSource that uses
 * attributes from an Attributes implementation.
 * Defaults to using class's transaction attribute if none is
 * associated with the target method.
 * Any transaction attribute associated with the target method completely
 * overrides a class transaction attribute.
 * @author Rod Johnson
 * @see org.springframework.metadata.Attributes
 * @version $Id: AttributesTransactionAttributeSource.java,v 1.2 2003-12-17 09:25:59 johnsonr Exp $
 */
public class AttributesTransactionAttributeSource implements TransactionAttributeSource {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private final Attributes attributes;
	
	
	public AttributesTransactionAttributeSource(Attributes attributes) {
		this.attributes = attributes;
	}
	

	/**
	 * Return the transaction attribute for this method invocation.
	 * Defaults to the class's transaction attribute if no method
	 * attribute is found
	 * @param mi MethodInvocation 
	 * @return null if the method is non-transactional
	 * @see org.springframework.transaction.interceptor.TransactionAttributeSource#getTransactionAttribute(org.aopalliance.intercept.MethodInvocation)
	 */
	public TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		
		// The method may be on an interface, but we need attributes from the target class.
		// The AopUtils class provides a convenience method for this. If the target class
		// is null, the method will be unchanged.
		method = AopUtils.getMostSpecificMethod(method, targetClass);
		
		Collection atts = this.attributes.getAttributes(method);
		TransactionAttribute txAtt = findTransactionAttribute(atts);
		if (txAtt != null)
			return txAtt;
		return findTransactionAttribute(this.attributes.getAttributes(method.getDeclaringClass()));
	}

	/**
	 * Return the transaction attribute, given this set of attributes
	 * attached to a method or class.
	 * Protected rather than private as subclasses may want to customize
	 * how this is done: for example, returning a TransactionAttribute
	 * affected by the values of other attributes.
	 * This implementation takes into account RollbackRuleAttributes, if
	 * the TransactionAttribute is a RuleBasedTransactionAttribute.
	 * Return null if it's not transactional. 
	 * @param atts attributes attached to a method or class. May
	 * be null, in which case a null TransactionAttribute will be returned.
	 * @return TransactionAttribute configured transaction attribute, or null
	 * if none was found
	 */
	protected TransactionAttribute findTransactionAttribute(Collection atts) {
		if (atts == null)
			return null;
		TransactionAttribute txAttribute = null;
		// Check there is a transaction attribute
		for (Iterator itr = atts.iterator(); itr.hasNext() && txAttribute == null; ) {
			Object att = itr.next();
			if (att instanceof TransactionAttribute) {
				txAttribute = (TransactionAttribute) att;
			}
		}

		// Look for rollback rule attributes if the transaction attribute can understand them
		// TODO should we cache this, rather than do it each time we find
		// a rule based attribute?
		if (txAttribute instanceof RuleBasedTransactionAttribute) {
			RuleBasedTransactionAttribute rbta = (RuleBasedTransactionAttribute) txAttribute;
			// We really want value: bit of a hack
			List l = new LinkedList();
			for (Iterator itr = atts.iterator(); itr.hasNext(); ) {
				Object att = itr.next();
				if (att instanceof RollbackRuleAttribute) {
					if (logger.isDebugEnabled())
						logger.debug("Found RollbackRule " + att);
					l.add(att);
				}
			}
			// Repeatedly setting this isn't elegant, but it works
			rbta.setRollbackRules(l);
		}
		
		return txAttribute;
	}

}
