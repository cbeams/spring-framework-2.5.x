package org.springframework.transaction.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for TransactionAttributeSource implementations.
 * Provides default isMatch check for mapped method names.
 * @author Juergen Hoeller
 * @since 07.11.2003
 */
public abstract class AbstractTransactionAttributeSource implements TransactionAttributeSource {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Return if the given method name matches the mapped name.
	 * The default implementation checks for "xxx*" and "*xxx" matches.
	 * Can be overridden in subclasses.
	 * @param methodName the method name of the class
	 * @param mappedName the name in the descriptor
	 * @return if the names match
	 */
	protected boolean isMatch(String methodName, String mappedName) {
		return (mappedName.endsWith("*") && methodName.startsWith(mappedName.substring(0, mappedName.length() - 1))) ||
				(mappedName.startsWith("*") && methodName.endsWith(mappedName.substring(1, mappedName.length())));
	}

}
