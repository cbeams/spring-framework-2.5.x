/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jms;

import javax.jms.JMSException;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for exception thrown by the framework whenever it
 * encounters a problem related to JMS.
 * @author Les Hazlewood
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class JmsException extends NestedRuntimeException {

	/**
	 * Constructor that takes a message.
	 * @param msg the detail message
	 */
	public JmsException(String msg) {
		super(msg);
	}

	/**
	 * Constructor that allows a message and a root cause.
	 * @param msg the detail message
	 * @param cause the cause of the exception. This argument is generally
	 * expected to be a proper subclass of {@link javax.jms.JMSException},
	 * but can also be a JNDI NamingException or the like.
	 */
	public JmsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor that allows a plain root cause, intended for
	 * subclasses mirroring respective javax.jms exceptions.
	 * @param cause the cause of the exception. This argument is generally
	 * expected to be a proper subclass of {@link javax.jms.JMSException}.
	 */
	protected JmsException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/**
	 * Convenience method to get the vendor specific error code if
	 * the root cause was an instance of JMSException.
	 * @return a string specifying the vendor-specific error code if
	 * the root cause is an instance of JMSException.  Otherwise return
	 * text indicating the root cause was not an instance of JMSException.
	 */
	public String getErrorCode() {
		if (getCause() instanceof JMSException) {
			return ((JMSException) getCause()).getErrorCode();
		}
		else {
			return "Root cause not a JMSException";
		}
	}
	
	/**
	 * Return the detail message, including the message from the linked exception
	 * if there is one.
	 * @see javax.jms.JMSException#getLinkedException
	 */
	public String getMessage() {
		// Even if you cannot set the cause of this exception other than through
		// the constructor, we check for the cause being "this" here, as the cause
		// could still be set to "this" via reflection: for example, by a remoting
		// deserializer like Hessian's.
		if (getCause() == null || getCause() == this) {
			return super.getMessage();
		}
		else {
			if (getCause().getClass().isAssignableFrom(JMSException.class) &&
				  ((JMSException)getCause()).getLinkedException() != null) {
				Exception le = ((JMSException) getCause()).getLinkedException();
					return super.getMessage() + "; nested exception is " + le.getClass().getName() +
							": " + le.getMessage();
			}
			else {
				return super.getMessage() + "; nested exception is " + getCause().getClass().getName() +
						": " + getCause().getMessage();
			}
		}
	}

}
