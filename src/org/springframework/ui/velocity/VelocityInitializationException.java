package org.springframework.ui.velocity;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown on Velocity initialization failure.
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class VelocityInitializationException extends NestedRuntimeException {

	public VelocityInitializationException(String msg) {
		super(msg);
	}

	public VelocityInitializationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
