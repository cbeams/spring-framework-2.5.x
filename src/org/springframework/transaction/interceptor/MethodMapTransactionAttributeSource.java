/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionUsageException;

/**
 * Simple implementation of TransactionAttributeSource that
 * allows attributes to be stored per method in a map.
 * @since 24-Apr-2003
 * @version $Id: MethodMapTransactionAttributeSource.java,v 1.1 2003-08-21 15:45:50 jhoeller Exp $
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class MethodMapTransactionAttributeSource implements TransactionAttributeSource {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Map from Method to TransactionAttribute */
	protected Map methodMap = new HashMap();

	/** Map from Method to name pattern used for registration */
	private Map nameMap = new HashMap();

	public TransactionAttribute getTransactionAttribute(MethodInvocation invocation) {
		return (TransactionAttribute) this.methodMap.get(invocation.getMethod());
	}

	/**
	 * Set a name/attribute map, consisting of "FQCN.method" method names
	 * (e.g. "com.mycompany.mycode.MyClass.myMethod") and TransactionAttribute
	 * instances.
	 */
	public void setMethodMap(Map methodMap) {
		Iterator it = methodMap.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			TransactionAttribute attr = (TransactionAttribute) methodMap.get(name);
			addTransactionalMethod(name, attr);
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * @param method the method
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(Method method, TransactionAttribute attr) {
		logger.info("Adding transactional method [" + method + "] with attribute [" + attr + "]");
		this.methodMap.put(method, attr);
	}

	/**
	 * Add an attribute for a transactional method.
	 * Method names can end with "*" for matching multiple methods.
	 * @param name class and method name, separated by a dot
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(String name, TransactionAttribute attr) {
		int lastDotIndex = name.lastIndexOf(".");
		if (lastDotIndex == -1)
			throw new TransactionUsageException("'" + name + "' is not a valid method name: format is FQN.methodName");
		String className = name.substring(0, lastDotIndex);
		String methodName = name.substring(lastDotIndex + 1);
		try {
			Class clazz = Class.forName(className);
			addTransactionalMethod(clazz, methodName, attr);
		}
		catch (ClassNotFoundException ex) {
			throw new TransactionUsageException("Class '" + className + "' not found");
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * Method names can end with "*" for matching multiple methods.
	 * @param clazz target interface or class
	 * @param methodName method name
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(Class clazz, String methodName, TransactionAttribute attr) {
		String name = clazz.getName() + '.'  + methodName;
		logger.debug("Adding transactional method [" + name + "] with attribute [" + attr + "]");

		// TODO address method overloading? At present this will
		// simply match all methods that have the given name.
		// Consider EJB syntax (int, String) etc.?
		Method[] methods = clazz.getDeclaredMethods();
		List matchingMethods = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			if (isMatch(methods[i].getName(), methodName)) {
				matchingMethods.add(methods[i]);
			}
		}
		if (matchingMethods.isEmpty())
			throw new TransactionUsageException("Couldn't find method '" + methodName + "' on " + clazz);

		// register all matching methods
		for (Iterator it = matchingMethods.iterator(); it.hasNext();) {
			Method method = (Method) it.next();
			String regMethodName = (String) this.nameMap.get(method);
			if (regMethodName == null || (!regMethodName.equals(name) && regMethodName.length() <= name.length())) {
				// no already registered method name, or more specific
				// method name specification now -> (re-)register method
				if (logger.isDebugEnabled() && regMethodName != null) {
					logger.debug("Replacing attribute for transactional method [" + method + "]: current name [" +
											 name + "] is more specific than [" + regMethodName + "]");
				}
				this.nameMap.put(method, name);
				addTransactionalMethod(method, attr);
			}
			else {
				if (logger.isDebugEnabled() && regMethodName != null) {
					logger.debug("Keeping attribute for transactional method [" + method + "]: current name [" +
											 name + "] is not more specific than [" + regMethodName + "]");
				}
			}
		}
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
