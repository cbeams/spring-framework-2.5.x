/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * Very simple implementation of TransactionAttributeSource which will always return
 * the same TransactionAttribute for all methods fed to it. The TransactionAttribute
 * may be specified, but will otherwise default to PROPOGATION_REQUIRED. This may be
 * used in the cases where you want to use the same transaction attribute with all
 * methods being handled by a transaction interceptor.
 *
 * @author Colin Sampaleanu
 * @since 15.10.2003
 * @version $Id: MatchAlwaysTransactionAttributeSource.java,v 1.3 2003-11-28 11:57:28 johnsonr Exp $
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.support.BeanNameAutoProxyCreator
 */
public class MatchAlwaysTransactionAttributeSource implements TransactionAttributeSource {
  
	TransactionAttribute _transactionAttribute;

	/**
	 * Create an instance. Will default to PROPOGATION_REQUIRED, which may be overriden
	 * by calling {@link #setTransactionAttribute}.
	 */
	public MatchAlwaysTransactionAttributeSource() {
		setTransactionAttribute("PROPAGATION_REQUIRED");
	}

	/**
	 * Allows a transaction attribute to be specified, using the String form, for
	 * example, "PROPOGATION_REQUIRED".
	 * @param transactionAttribute The String form of the transactionAttribute to use.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public void setTransactionAttribute(String transactionAttribute) {
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		tae.setAsText(transactionAttribute);
		_transactionAttribute = (TransactionAttribute) tae.getValue();
	}

	public TransactionAttribute getTransactionAttribute(Method m, Class targetClass) {
		return _transactionAttribute;
	}

}
