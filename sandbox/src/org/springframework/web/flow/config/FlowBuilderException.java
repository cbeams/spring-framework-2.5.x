package org.springframework.web.flow.config;

/**
 * Exception thrown to indicate a problem while building a flow.
 * 
 * @author Erwin Vervaet
 */
public class FlowBuilderException extends RuntimeException {

	public FlowBuilderException() {
	}

	public FlowBuilderException(String message) {
		super(message);
	}

	public FlowBuilderException(Throwable cause) {
		super(cause);
	}

	public FlowBuilderException(String message, Throwable cause) {
		super(message, cause);
	}
}
