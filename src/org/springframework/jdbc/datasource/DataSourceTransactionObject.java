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

/**
 * DataSource transaction object, representing a ConnectionHolder.
 * Used as transaction object by DataSourceTransactionManager.
 *
 * <p>Derives from JdbcTransactionObjectSupport to inherit the capability
 * to manage JDBC 3.0 Savepoints.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see DataSourceTransactionManager
 * @see ConnectionHolder
 * @see org.springframework.transaction.support.DefaultTransactionStatus
 * @version $Id: DataSourceTransactionObject.java,v 1.7 2004-06-19 10:02:23 jhoeller Exp $
 */
public class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

	private Integer previousIsolationLevel;
	
	private boolean mustRestoreAutoCommit;

	protected void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return previousIsolationLevel;
	}

	protected void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
		this.mustRestoreAutoCommit = mustRestoreAutoCommit;
	}

	public boolean getMustRestoreAutoCommit() {
		return mustRestoreAutoCommit;
	}

}
