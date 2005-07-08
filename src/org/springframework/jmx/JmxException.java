package org.springframework.jmx;

import org.springframework.core.NestedRuntimeException;

/**
 * @author robh
 */
public class JmxException extends NestedRuntimeException {
	public JmxException(String msg) {
		super(msg);
	}

	public JmxException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
