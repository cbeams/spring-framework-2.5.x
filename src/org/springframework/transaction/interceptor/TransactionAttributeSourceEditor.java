/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.beans.PropertyEditorSupport;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * Property editor that can convert String into TransactionAttributeSource.
 * The transaction attribute string must be parseable by the
 * TransactionAttributeEditor in this package.
 *
 * <p>Strings are in property syntax, with the form:<br>
 * <code>FQCN.methodName=&lt;transaction attribute string&gt;</code>
 *
 * <p>For example:<br>
 * <code>com.mycompany.mycode.MyClass.myMethod=PROPAGATION_MANDATORY,ISOLATION_DEFAULT</code>
 *
 * <p><b>NOTE:</b> The specified class must be the one where the methods are
 * defined; in case of implementing an interface, the interface class name.
 *
 * <p>Note: Will register all overloaded methods for a given name.
 * Does not support explicit registration of certain overloaded methods.
 * Supports "xxx*" mappings, e.g. "notify*" for "notify" and "notifyAll".
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 26-Apr-2003
 * @version $Id: TransactionAttributeSourceEditor.java,v 1.3 2004-02-27 16:40:14 jhoeller Exp $
 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
 */
public class TransactionAttributeSourceEditor extends PropertyEditorSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	public void setAsText(String s) throws IllegalArgumentException {
		MethodMapTransactionAttributeSource source = new MethodMapTransactionAttributeSource();
		if (s == null || "".equals(s)) {
			// Leave value in property editor null
		}
		else {
			// Use properties editor to tokenize the hold string
			PropertiesEditor propertiesEditor = new PropertiesEditor();
			propertiesEditor.setAsText(s);
			Properties props = (Properties) propertiesEditor.getValue();

			// Now we have properties, process each one individually
			TransactionAttributeEditor tae = new TransactionAttributeEditor();
			for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String value = props.getProperty(name);

				// Convert value to a transaction attribute
				tae.setAsText(value);
				TransactionAttribute attr = (TransactionAttribute) tae.getValue();

				// Register name and attribute
				source.addTransactionalMethod(name, attr);
			}
		}
		setValue(source);
	}

}
