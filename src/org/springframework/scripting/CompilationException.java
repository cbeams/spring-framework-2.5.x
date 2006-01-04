package org.springframework.scripting;

import org.springframework.core.NestedRuntimeException;

/**
 * @author Rob Harrop
 */
public class CompilationException extends NestedRuntimeException {

	public CompilationException(String msg) {
		super(msg);
	}

	public CompilationException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
