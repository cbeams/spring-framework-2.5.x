/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.TransactionUsageException;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of TransactionAttributeSource that
 * allows attributes to be stored per method in a map.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 24.04.2003
 * @see #isMatch
 * @see NameMatchTransactionAttributeSource
 */
public class MethodMapTransactionAttributeSource implements TransactionAttributeSource {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Map from Method to TransactionAttribute */
	private Map methodMap = new HashMap();

	/** Map from Method to name pattern used for registration */
	private Map nameMap = new HashMap();


	/**
	 * Set a name/attribute map, consisting of "FQCN.method" method names
	 * (e.g. "com.mycompany.mycode.MyClass.myMethod") and TransactionAttribute
	 * instances (or Strings to be converted to TransactionAttribute instances).
	 * @see TransactionAttribute
	 * @see TransactionAttributeEditor
	 */
	public void setMethodMap(Map methodMap) {
		Iterator it = methodMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String name = (String) entry.getKey();

			// Check whether we need to convert from String to TransactionAttribute.
			TransactionAttribute attr = null;
			if (entry.getValue() instanceof TransactionAttribute) {
				attr = (TransactionAttribute) entry.getValue();
			}
			else {
				TransactionAttributeEditor editor = new TransactionAttributeEditor();
				editor.setAsText(entry.getValue().toString());
				attr = (TransactionAttribute) editor.getValue();
			}

			addTransactionalMethod(name, attr);
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * Method names can end or start with "*" for matching multiple methods.
	 * @param name class and method name, separated by a dot
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(String name, TransactionAttribute attr) {
		int lastDotIndex = name.lastIndexOf(".");
		if (lastDotIndex == -1) {
			throw new TransactionUsageException("'" + name + "' is not a valid method name: format is FQN.methodName");
		}
		String className = name.substring(0, lastDotIndex);
		String methodName = name.substring(lastDotIndex + 1);
		try {
			Class clazz = ClassUtils.forName(className);
			addTransactionalMethod(clazz, methodName, attr);
		}
		catch (ClassNotFoundException ex) {
			throw new TransactionUsageException("Class '" + className + "' not found");
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * Method names can end or start with "*" for matching multiple methods.
	 * @param clazz target interface or class
	 * @param mappedName mapped method name
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(Class clazz, String mappedName, TransactionAttribute attr) {
		String name = clazz.getName() + '.'  + mappedName;

		// TODO address method overloading? At present this will
		// simply match all methods that have the given name.
		// Consider EJB syntax (int, String) etc.?
		Method[] methods = clazz.getDeclaredMethods();
		List matchingMethods = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(mappedName) || isMatch(methods[i].getName(), mappedName)) {
				matchingMethods.add(methods[i]);
			}
		}
		if (matchingMethods.isEmpty()) {
			throw new TransactionUsageException(
					"Couldn't find method '" + mappedName + "' on class [" + clazz.getName() + "]");
		}

		// register all matching methods
		for (Iterator it = matchingMethods.iterator(); it.hasNext();) {
			Method method = (Method) it.next();
			String regMethodName = (String) this.nameMap.get(method);
			if (regMethodName == null || (!regMethodName.equals(name) && regMethodName.length() <= name.length())) {
				// No already registered method name, or more specific
				// method name specification now -> (re-)register method.
				if (logger.isDebugEnabled() && regMethodName != null) {
					logger.debug("Replacing attribute for transactional method [" + method + "]: current name '" +
							name + "' is more specific than '" + regMethodName + "'");
				}
				this.nameMap.put(method, name);
				addTransactionalMethod(method, attr);
			}
			else {
				if (logger.isDebugEnabled() && regMethodName != null) {
					logger.debug("Keeping attribute for transactional method [" + method + "]: current name '" +
							name + "' is not more specific than '" + regMethodName + "'");
				}
			}
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * @param method the method
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(Method method, TransactionAttribute attr) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding transactional method [" + method + "] with attribute [" + attr + "]");
		}
		this.methodMap.put(method, attr);
	}

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


	public TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		return (TransactionAttribute) this.methodMap.get(method);
	}

}
