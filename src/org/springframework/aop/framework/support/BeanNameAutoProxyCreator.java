package org.springframework.aop.framework.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Auto proxy creator that identifies beans to proxy via a list of names.
 * A name can specify a prefix to match by ending with "*".
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see #setBeanNames
 */
public class BeanNameAutoProxyCreator extends AbstractAutoProxyCreator {

	private List beanNames;

	/**
	 * Set the names of the beans that should automatically get wrapped with proxies.
	 * A name can specify a prefix to match by ending with "*", e.g. "myBean,tx*"
	 * will match the bean named "myBean" and all beans whose name start with "tx".
	 */
	public void setBeanNames(String[] beanNames) {
		this.beanNames = Arrays.asList(beanNames);
	}

	/**
	 * Identify as bean to proxy if the name is in the configured list of names.
	 */
	protected boolean isBeanToProxy(Object bean, String name, RootBeanDefinition definition) {
		if (this.beanNames != null) {
			if (this.beanNames.contains(name)) {
				return true;
			}
			for (Iterator it = this.beanNames.iterator(); it.hasNext();) {
				String mappedName = (String) it.next();
				if (mappedName.endsWith("*") && name.startsWith(mappedName.substring(0, mappedName.length() - 1))) {
					return true;
				}
			}
		}
		return false;
	}

}
