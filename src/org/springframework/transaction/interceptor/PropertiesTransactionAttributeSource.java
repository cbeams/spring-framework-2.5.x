/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.util.Iterator;
import java.util.Properties;

/**
 * Simple subclass of NameMatchTransactionAttributeSource that parses
 * property values using the TransactionAttributeEditor. 
 * @author Rod Johnson
 * @version $Id: PropertiesTransactionAttributeSource.java,v 1.1 2003-12-03 13:58:24 johnsonr Exp $
 */
public class PropertiesTransactionAttributeSource extends NameMatchTransactionAttributeSource {

	public void setProperties(Properties transactionAttributes) {
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		for (Iterator it = transactionAttributes.keySet().iterator(); it.hasNext(); ) {
			String methodName = (String) it.next();
			String value = transactionAttributes.getProperty(methodName);
			tae.setAsText(value);
			TransactionAttribute attr = (TransactionAttribute) tae.getValue();
			addTransactionalMethod(methodName, attr);
		}
	}

}