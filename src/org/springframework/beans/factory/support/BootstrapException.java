package org.springframework.beans.factory.support;

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown if a bean factory could not be loaded by a bootstrap class.
 * @author Rod Johnson
 * @since 02-Dec-02
 */
public class BootstrapException extends FatalBeanException {

	public BootstrapException(String msg) {
		super(msg);
	}

	public BootstrapException(String msg, Throwable t) {
		super(msg, t);
	}

}
