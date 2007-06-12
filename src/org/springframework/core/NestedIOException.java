/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Subclass of IOException that properly handles a root cause,
 * exposing the root cause just like NestedChecked/RuntimeException does.
 *
 * <p>The similarity between this class and the NestedChecked/RuntimeException
 * class is unavoidable, as this class needs to derive from IOException
 * and cannot derive from NestedCheckedException.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getMessage
 * @see #printStackTrace
 * @see org.springframework.core.NestedCheckedException
 * @see org.springframework.core.NestedRuntimeException
 */
public class NestedIOException extends IOException {

	/** Root cause of this nested exception */
	private Throwable cause;


	/**
	 * Construct a <code>NestedIOException</code> with the specified detail message.
	 * @param msg the detail message
	 */
	public NestedIOException(String msg) {
		super(msg);
	}

	/**
	 * Construct a <code>NestedIOException</code> with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public NestedIOException(String msg, Throwable cause) {
		super(msg);
		this.cause = cause;
	}


	/**
	 * Return the nested cause, or <code>null</code> if none.
	 */
	public Throwable getCause() {
		// Even if you cannot set the cause of this exception other than through
		// the constructor, we check for the cause being "this" here, as the cause
		// could still be set to "this" via reflection: for example, by a remoting
		// deserializer like Hessian's.
		return (this.cause == this ? null : this.cause);
	}

	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
	public String getMessage() {
		return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
	}

	/**
	 * Print the composite message and the embedded stack trace to the specified stream.
	 * @param ps the print stream
	 */
	public void printStackTrace(PrintStream ps) {
		if (getCause() == null) {
			super.printStackTrace(ps);
		}
		else {
			ps.println(this);
			ps.print("Caused by: ");
			getCause().printStackTrace(ps);
		}
	}

	/**
	 * Print the composite message and the embedded stack trace to the specified print writer.
	 * @param pw the print writer
	 */
	public void printStackTrace(PrintWriter pw) {
		if (getCause() == null) {
			super.printStackTrace(pw);
		}
		else {
			pw.println(this);
			pw.print("Caused by: ");
			getCause().printStackTrace(pw);
		}
	}

}
