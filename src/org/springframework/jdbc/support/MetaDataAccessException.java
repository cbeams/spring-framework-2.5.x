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

package org.springframework.jdbc.support;

import org.springframework.core.NestedCheckedException;

/**
 * Exception indicating that something went wrong during metadata lookup.
 * This is a checked exception since we want it to be caught, logged and
 * handled rather than cause the application to fail.  This is usually not 
 * a fatal problem. 
 *
 * @author Thomas Risberg
 */
public class MetaDataAccessException extends NestedCheckedException {
	/**
	 * Constructor for MetaDataAccessException.
	 * @param msg message
	 */
	public MetaDataAccessException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for MetaDataAccessException.
	 * @param msg message
	 * @param ex root cause from data access API in use
	 */
	public MetaDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
