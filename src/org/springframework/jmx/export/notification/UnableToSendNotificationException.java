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

package org.springframework.jmx.export.notification;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown when we are unable to send a JMX notification.
 *
 * @author Rob Harrop
 * @since 1.3
 * @see NotificationPublisher
 */
public class UnableToSendNotificationException extends NestedRuntimeException {

	/**
	 * Create a new <code>UnableToSendNotificationException</code> with the
	 * specified error message.
	 * @param msg the error message
	 */
	public UnableToSendNotificationException(String msg) {
		super(msg);
	}

	/**
	 * Create a new <code>UnableToSendNotificationException</code> with the
	 * specified error message and root cause.
	 * @param msg the error message
	 * @param ex the root cause
	 */
	public UnableToSendNotificationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
