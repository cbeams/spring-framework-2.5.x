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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Callback interface used by the JdbcTemplate class.
 *
 * <p>This interface sets values on a PreparedStatement provided by the
 * JdbcTemplate class. Implementations are responsible for setting any
 * necessary parameters. SQL with placeholders will already have been supplied.
 *
 * <p>It's easier to use this interface than PreparedStatementCreator,
 * as the JdbcTemplate will create the prepared statement.
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @author Rod Johnson
 * @since March 2, 2003
 * @see JdbcTemplate#update(String, PreparedStatementSetter)
 */
public interface PreparedStatementSetter {

	/** 
	* Set values on the given PreparedStatement.
	* @param ps PreparedStatement we'll invoke setter methods on
	* @throws SQLException there is no need to catch SQLExceptions
	* that may be thrown in the implementation of this method.
	* The JdbcTemplate class will handle them.
	*/
	void setValues(PreparedStatement ps) throws SQLException;

}
