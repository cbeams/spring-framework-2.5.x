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

package org.springframework.remoting.support;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Encapsulates a remote invocation result, holding a result value or an exception.
 * Used for HTTP-based serialization invokers.
 *
 * <p>This is an SPI class, typically not used directly by applications.
 * Can be subclassed for additional invocation parameters.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public class RemoteInvocationResult implements Serializable {

	private Object value;

	private Exception exception;


	/**
	 * Create a new RemoteInvocationResult for the given result value.
	 * @param value the result value returned by a successful invocation
	 * of the target method
	 */
	public RemoteInvocationResult(Object value) {
		this.value = value;
	}

	/**
	 * Create a new RemoteInvocationResult for the given exception.
	 * @param exception the exception thrown by an unsuccessful invocation
	 * of the target method
	 */
	public RemoteInvocationResult(Exception exception) {
		this.exception = exception;
	}

	public Object getValue() {
		return value;
	}

	public Exception getException() {
		return exception;
	}

	public boolean hasException() {
		return (exception != null);
	}


	/**
	 * Recreate the invocation result, either returning the result value
	 * in case of a successful invocation of the target method, or
	 * rethrowing the exception thrown by the target method.
	 * @return the result value, if any
	 * @throws Throwable the exception, if any
	 */
	public Object recreate() throws Throwable {
		if (this.exception != null) {
			if (this.exception instanceof InvocationTargetException) {
				throw ((InvocationTargetException) this.exception).getTargetException();
			}
			throw this.exception;
		}
		else {
			return this.value;
		}
	}

}
