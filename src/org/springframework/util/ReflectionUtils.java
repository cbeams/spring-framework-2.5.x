/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.util;

import java.lang.reflect.InvocationTargetException;

/**
 * Simple utility class for handling reflection exceptions.
 * Only intended for internal use.
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public abstract class ReflectionUtils {

	/**
	 * Handle the given reflection exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of an InvocationTargetException with such a root cause. Throws
	 * an IllegalStateException with an appropriate message else.
	 * @param ex the reflection exception to handle
	 */
	public static void handleReflectionException(Exception ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if (ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		throw new IllegalStateException(
				"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
	}

	/**
	 * Handle the given invocation target exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of such a root cause. Throws an IllegalStateException else.
	 * @param ex the invocation target exception to handle
	 */
	public static void handleInvocationTargetException(InvocationTargetException ex) {
		if (ex.getTargetException() instanceof RuntimeException) {
			throw (RuntimeException) ex.getTargetException();
		}
		if (ex.getTargetException() instanceof Error) {
			throw (Error) ex.getTargetException();
		}
		throw new IllegalStateException(
				"Unexpected exception thrown by method - " + ex.getTargetException().getClass().getName() +
				": " + ex.getTargetException().getMessage());
	}

}
