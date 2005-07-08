package org.springframework.jmx;

/**
 * @author Rob Harrop
 */
public class NotCompliantMBeanException extends JmxException {

	public NotCompliantMBeanException(javax.management.NotCompliantMBeanException cause) {
		super(cause);
	}
}
