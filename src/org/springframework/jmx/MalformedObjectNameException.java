package org.springframework.jmx;

/**
 * @author robh
 */
public class MalformedObjectNameException extends JmxException {
	public MalformedObjectNameException(String msg) {
		super(msg);
	}

	public MalformedObjectNameException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
