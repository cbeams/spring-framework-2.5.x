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

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;

/**
 * Exception thrown when a JDBC update affects an unexpected
 * number of rows. Typically we expect an update to affect a
 * single row, meaning it's an error if it affects multiple rows.
 * @author Rod Johnson
 */
public class JdbcUpdateAffectedIncorrectNumberOfRowsException extends IncorrectUpdateSemanticsDataAccessException {
	
	/** Number of rows that should have been affected */
	private final int expected;
	
	/** Number of rows that actually were affected */
	private final int actual;

	public JdbcUpdateAffectedIncorrectNumberOfRowsException(String sql, int expected, int actual) {
		super("SQL update '" + sql + "' affected " + actual + " rows, not " + expected + ", as expected");
		this.expected = expected;
		this.actual = actual;
	}

	public int getExpectedRowsAffected() {
		return expected;
	}
	
	public int getActualRowsAffected() {
		return actual;
	}

	public boolean getDataWasUpdated() {
		return getActualRowsAffected() > 0;
	}

}
