package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown if a FactoryBean is involved in a circular reference.
 *
 * <p>A circular reference with a FactoryBean cannot be solved by eagerly
 * caching singleton instances like with normal beans. The reason is that
 * <i>every</i> FactoryBean needs to be fully initialized before it can
 * return the created bean, while only <i>specific</i> normal beans need
 * to be initialized - that is, if a collaborating bean actually invokes
 * them on initialization instead of just storing the reference.
 *
 * @author Juergen Hoeller
 * @since 30.10.2003
 */
public class FactoryBeanCircularReferenceException extends FatalBeanException {

	public FactoryBeanCircularReferenceException(String msg) {
		super(msg);
	}

}
