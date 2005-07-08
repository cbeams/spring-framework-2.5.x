package org.springframework.jmx;

import javax.management.JMException;

/**
 * @author robh
 */
public class UncategorizedJmxException extends JmxException {
	public UncategorizedJmxException(String msg) {
		super(msg);
	}

	public UncategorizedJmxException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
