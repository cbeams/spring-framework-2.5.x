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

package org.springframework.orm.ibatis.support;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibatis.sqlmap.engine.type.BaseTypeHandler;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 * @since 1.2
 */
public abstract class AbstractLobTypeHandler extends BaseTypeHandler {

	private LobHandler lobHandler;

	public AbstractLobTypeHandler() {
		// this.lobHandler = SqlMapClientFactoryBean.getConfigTimeLobHandler();
	}

	public final void setParameter(PreparedStatement ps, int i, Object parameter, String jdbcType)
			throws SQLException {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("Spring transaction synchronization needs to be active for " +
					"setting values in iBATIS TypeHandlers that delegate to a Spring LobHandler");
		}
		final LobCreator lobCreator = this.lobHandler.getLobCreator();
		try {
			setParameterInternal(ps, i, parameter, jdbcType, lobCreator);
		}
		catch (IOException ex) {
			throw new SQLException("I/O errors during LOB access: " + ex.getMessage());
		}
		TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronizationAdapter() {
					public void beforeCompletion() {
						lobCreator.close();
					}
				}
		);
	}

	protected abstract void setParameterInternal(
			PreparedStatement ps, int index, Object parameter, String jdbcType, LobCreator lobCreator)
			throws SQLException, IOException;

	public final Object getResult(ResultSet rs, String columnName)
			throws SQLException {
		return getResult(rs, rs.findColumn(columnName));
	}

	public final Object getResult(ResultSet rs, int columnIndex)
			throws SQLException {
		try {
			return getResultInternal(rs, columnIndex, this.lobHandler);
		}
		catch (IOException ex) {
			throw new SQLException("I/O errors during LOB access: " + ex.getMessage());
		}
	}

	protected abstract Object getResultInternal(ResultSet rs, int columnIndex, LobHandler lobHandler)
			throws SQLException, IOException;

	public Object getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		throw new SQLException("Retrieving LOBs from a CallableStatement is not supported");
	}

}
