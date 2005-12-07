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

package org.springframework.aop.target.scope;

/**
 * Exception thrown when a user tries to reconnect
 * from a null handle or a handle that is not persistent.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class HandleNotPersistentException extends BadHandleException {

	/**
	 * Constructor for use when handle is null.
	 */
	public HandleNotPersistentException() {
		super("Cannot reconnect from a null handle");
	}

	public HandleNotPersistentException(Handle handle) {
		super("Handle [" + handle + "] is not persistent");
	}
	
}
