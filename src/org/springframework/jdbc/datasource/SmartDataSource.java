
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

package org.springframework.jdbc.datasource;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * Subinterface of javax.sql.DataSource, to be implemented by special
 * DataSources that return pooled JDBC Connection in an unwrapped fashion.
 *
 * <p>Classes using this interface can query whether or not the connection
 * should be closed after an operation. Spring's DataSourceUtils and
 * JdbcTemplate classes automatically perform such a check.
 *
 * @author Rod Johnson
 * @see SingleConnectionDataSource#shouldClose
 * @see DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public interface SmartDataSource extends DataSource {
		
	/** 
	 * Should we close this connection, obtained from this DataSource?
	 * <p>Code that uses connections from a SmartDataSource should always
	 * perform a check via this method before invoking <code>close()</code>.
	 * <p>However, the JdbcTemplate class in the core package should take care of
	 * closing JDBC connections, freeing application code of this responsibility.
	 * @param con connection, which should have been obtained
	 * from this data source, to check closure status of
	 * @return whether the given connection should be closed
	 */
	boolean shouldClose(Connection con);

}
