
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
 * Interface to be implemented by classes that can provide a connection to
 * a relational database. Extends the javax.sql.DataSource interface
 * to allow classes using it to query whether or not the connection should
 * be closed after a given operation. This can sometimes be
 * useful for efficiency, if we know that we want to reuse
 * a connection.
 * @author  Rod Johnson
 * @version $Id: SmartDataSource.java,v 1.2 2004-03-18 02:46:05 trisberg Exp $
 */
public interface SmartDataSource extends DataSource {
		
	/** 
	 * Should we close this connection, obtained from this factory?
	 * Code that uses connections from the factory should always
	 * use code like 
	 * <code>
	 * if (factory.shouldClose(conn)) 
	 * 	con.close()
	 * </code>
	 * in a finally block.
	 * However, the JdbcTemplate class in this package should
	 * take care of closing JDBC connections, freeing
	 * application code of this responsibility.
	 * @param conn connection, which should have been obtained
	 * from this data source, to check closure status of
	 */
	boolean shouldClose(Connection conn);

}
