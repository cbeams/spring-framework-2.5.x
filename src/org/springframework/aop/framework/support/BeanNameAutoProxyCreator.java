package org.springframework.aop.framework.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
	protected Object[] getInterceptorsAndPointcutsForBean(Object bean, String name) {
		if (this.beanNames != null) {
			if (this.beanNames.contains(name)) {
				return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
			}
			for (Iterator it = this.beanNames.iterator(); it.hasNext();) {
				String mappedName = (String) it.next();
				if (mappedName.endsWith("*") && name.startsWith(mappedName.substring(0, mappedName.length() - 1))) {
					return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
				}
			}
		}
		return DO_NOT_PROXY;
	}

}
