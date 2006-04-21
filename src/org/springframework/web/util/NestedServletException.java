/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.util;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;

/**
 * Subclass of ServletException that properly handles a root cause,
 * exposing the root cause just like NestedChecked/RuntimeException does.
 * Note that the plain ServletException doesn't expose its root cause at all,
 * neither in the exception message nor in printed stack traces!
 *
 * <p>The similarity between this class and the NestedChecked/RuntimeException
 * class is unavoidable, as this class needs to derive from ServletException
 * and cannot derive from NestedCheckedException.
 *
 * @author Juergen Hoeller
 * @since 1.2.5
 * @see #getMessage
 * @see #printStackTrace
 * @see org.springframework.core.NestedCheckedException
 * @see org.springframework.core.NestedRuntimeException
 */
public class NestedServletException extends ServletException {

	/**
	 * Construct a <code>NestedServletException</code> with the specified detail message.
	 * @param msg the detail message
	 */
	public NestedServletException(String msg) {
		super(msg);
	}

	/**
	 * Construct a <code>NestedServletException</code> with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param ex the nested exception
	 */
	public NestedServletException(String msg, Throwable ex) {
		super(msg, ex);
	}


	/**
	 * Return the nested cause, or <code>null</code> if none.
	 */
	public Throwable getCause() {
		// Even if you cannot set the cause of this exception other than through
		// the constructor, we check for the cause being "this" here, as the cause
		// could still be set to "this" via reflection: for example, by a remoting
		// deserializer like Hessian's.
		return (getRootCause() == this ? null : getRootCause());
	}

	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
	public String getMessage() {
		String message = super.getMessage();
		Throwable cause = getCause();
		if (cause != null) {
			return message + "; nested exception is " + cause;
		}
		return message;
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
			getCause().printStackTrace(pw);
		}
	}

}
