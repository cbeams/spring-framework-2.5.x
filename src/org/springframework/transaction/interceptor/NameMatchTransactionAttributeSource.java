package org.springframework.transaction.interceptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple implementation of TransactionAttributeSource that
 * allows attributes to be matched by registered name.
 * @author Juergen Hoeller
 * @since 21.08.2003
 */
public class NameMatchTransactionAttributeSource implements TransactionAttributeSource {

	protected final Log logger = LogFactory.getLog(getClass());

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

	public TransactionAttribute getTransactionAttribute(MethodInvocation invocation) {
		for (Iterator it = this.nameMap.keySet().iterator(); it.hasNext();) {
			String mappedName = (String) it.next();
			if (isMatch(invocation.getMethod().getName(), mappedName)) {
				return (TransactionAttribute) this.nameMap.get(mappedName);
			}
		}
		return null;
	}

	/**
	 * Return if the given method name matches the mapped name.
	 * The default implementation checks for direct and "xxx*" matches.
	 * Can be overridden in subclasses.
	 * @param methodName the method name of the class
	 * @param mappedName the name in the descriptor
	 * @return if the names match
	 */
	protected boolean isMatch(String methodName, String mappedName) {
		return methodName.equals(mappedName) ||
		    (mappedName.endsWith("*") && methodName.startsWith(mappedName.substring(0, mappedName.length() - 1)));
	}

}
