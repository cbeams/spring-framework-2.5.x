package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple implementation of TransactionAttributeSource that
 * allows attributes to be matched by registered name.
 * @author Juergen Hoeller
 * @since 21.08.2003
 * @see #isMatch
 */
public class NameMatchTransactionAttributeSource extends AbstractTransactionAttributeSource {

	private Map nameMap = new HashMap();

	/**
	 * Add an attribute for a transactional method.
	 * Method names can end with "*" for matching multiple methods.
	 * @param methodName the name of the method
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(String methodName, TransactionAttribute attr) {
		logger.debug("Adding transactional method [" + methodName + "] with attribute [" + attr + "]");
		this.nameMap.put(methodName, attr);
	}

	public TransactionAttribute getTransactionAttribute(Method m, Class targetClass) {
		String methodName = m.getName();
		TransactionAttribute attr = (TransactionAttribute) this.nameMap.get(methodName);
		if (attr != null) {
			return attr;
		}
		else {
			for (Iterator it = this.nameMap.keySet().iterator(); it.hasNext();) {
				String mappedName = (String) it.next();
				if (isMatch(methodName, mappedName)) {
					return (TransactionAttribute) this.nameMap.get(mappedName);
				}
			}
			return null;
		}
	}

}
