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

package org.springframework.jdbc;

import java.io.IOException;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Exception thrown when a BLOB/CLOB could not be read.
 * @author Juergen Hoeller
 * @since 28.04.2004
 */
public class LobRetrievalFailureException extends DataRetrievalFailureException {

	/**
	 * Constructor for LobRetrievalFailureException.
	 * @param msg message
	 */
	public LobRetrievalFailureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for LobRetrievalFailureException.
	 * @param msg message
	 * @param ex IOException root cause
	 */
	public LobRetrievalFailureException(String msg, IOException ex) {
		super(msg, ex);
	}

}
