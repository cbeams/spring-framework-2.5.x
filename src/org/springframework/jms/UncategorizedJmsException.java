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

/**
 * JmsException to be thrown when no other matching subclass found.
 * @author Juergen Hoeller
 * @since 1.1
 */
public class UncategorizedJmsException extends JmsException {

	public UncategorizedJmsException(String msg) {
		super(msg);
	}

	public UncategorizedJmsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UncategorizedJmsException(Throwable cause) {
		super("Uncategorized exception occured during JMS processing", cause);
	}

}
