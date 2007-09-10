/*
 * Copyright 2002-2007 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.Assert;

import java.sql.*;

/**
 * {@link org.springframework.jdbc.support.SQLExceptionTranslator} implementation that analyzes the {@link java.sql.SQLException}
 * subclass that was returned.  This is only available with JDBC 4.0 and later drivers when using Java 6 or later.
 *
 * @author Thomas Risberg
 * @since 2.5
 * @see java.sql.SQLException
 */
public class SQLExceptionSubclassTranslator implements SQLExceptionTranslator {


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public DataAccessException translate(String task, String sql, SQLException ex) {
		Assert.notNull(ex, "Cannot translate a null SQLException.");
		if (task == null) {
			task = "";
		}
		if (sql == null) {
			sql = "";
		}
		if (ex instanceof SQLTransientException) {
			if (ex instanceof SQLTransactionRollbackException) {
				return new ConcurrencyFailureException(buildMessage(task, sql, ex), ex);
			}
			if (ex instanceof SQLTransientConnectionException) {
				return new TransientDataAccessResourceException(buildMessage(task, sql, ex), ex);
			}
			if (ex instanceof SQLTimeoutException) {
				return new TransientDataAccessResourceException(buildMessage(task, sql, ex), ex);
			}
		}
		else if (ex instanceof SQLNonTransientException) {
			if (ex instanceof SQLDataException) {
				return new DataIntegrityViolationException(buildMessage(task, sql, ex), ex);
			}
			else if (ex instanceof SQLFeatureNotSupportedException) {
				return new BadSqlGrammarException(task, sql, ex);
			}
			else if (ex instanceof SQLIntegrityConstraintViolationException) {
				return new DataIntegrityViolationException(buildMessage(task, sql, ex), ex);
			}
			else if (ex instanceof SQLInvalidAuthorizationSpecException) {
				return new PermissionDeniedDataAccessException(buildMessage(task, sql, ex), ex);
			}
			else if (ex instanceof SQLNonTransientConnectionException) {
				return new DataAccessResourceFailureException(buildMessage(task, sql, ex), ex);
			}
			else if (ex instanceof SQLSyntaxErrorException) {
				return new BadSqlGrammarException(task, sql, ex);
			}
		}
		else if (ex instanceof SQLRecoverableException) {
			return new RecoverableDataAccessException(buildMessage(task, sql, ex), ex);
		}
		// We couldn't identify it more precisely - let's allow other translation strategies to kick in.
		return null;
	}


	/**
	 * Build a message <code>String</code> for the given {@link java.sql.SQLException}.
	 * <p>Called when creating an instance of a generic
	 * {@link org.springframework.dao.DataAccessException} class.
	 * @param task readable text describing the task being attempted
	 * @param sql  the SQL statement that caused the problem. May be <code>null</code>.
	 * @param ex   the offending <code>SQLException</code>
	 * @return the message <code>String</code> to use
	 */
	protected String buildMessage(String task, String sql, SQLException ex) {
		return task + "; SQL [" + sql + "]; " + ex.getMessage();
	}

}
