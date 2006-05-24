/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.orm.jpa.vendor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.platform.database.DB2Platform;
import oracle.toplink.essentials.platform.database.HSQLPlatform;
import oracle.toplink.essentials.platform.database.InformixPlatform;
import oracle.toplink.essentials.platform.database.MySQL4Platform;
import oracle.toplink.essentials.platform.database.PostgreSQLPlatform;
import oracle.toplink.essentials.platform.database.SQLServerPlatform;
import oracle.toplink.essentials.platform.database.SybasePlatform;
import oracle.toplink.essentials.platform.database.oracle.OraclePlatform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * TopLink-specific JpaVendorAdapter implementation.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class TopLinkJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final Log logger = LogFactory.getLog(getClass());

	private final JpaDialect jpaDialect = new TopLinkJpaDialect();

	private File dropDdlTempFile;

	private File createDdlTempFile;


	public Class getPersistenceProviderClass() {
		return EntityManagerFactoryProvider.class;
	}

	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();
		if (isShowSql()) {
			jpaProperties.setProperty(EntityManagerFactoryProvider.TOPLINK_LOGGING_LEVEL, "FINE");
		}
		if (isGenerateDdl()) {
			jpaProperties.setProperty(EntityManagerFactoryProvider.DDL_GENERATION,
					EntityManagerFactoryProvider.DROP_AND_CREATE);
			try {
				this.dropDdlTempFile = File.createTempFile("toplink-drop", ".ddl", new File(""));
				this.createDdlTempFile = File.createTempFile("toplink-create", ".ddl", new File(""));
				jpaProperties.setProperty(EntityManagerFactoryProvider.DROP_JDBC_DDL_FILE,
						this.dropDdlTempFile.getPath());
				jpaProperties.setProperty(EntityManagerFactoryProvider.CREATE_JDBC_DDL_FILE,
						this.createDdlTempFile.getPath());
				this.dropDdlTempFile = new File(new File("").getAbsolutePath(), this.dropDdlTempFile.getPath());
				this.createDdlTempFile = new File(new File("").getAbsolutePath(), this.createDdlTempFile.getPath());
				System.out.println(this.dropDdlTempFile.exists());
				System.out.println(this.createDdlTempFile.exists());
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		if (getDatabase() != null) {
			Class databasePlatformClass = getDatabasePlatformClass(getDatabase());
			if (databasePlatformClass != null) {
				jpaProperties.setProperty(EntityManagerFactoryProvider.TOPLINK_PLATFORM_PROPERTY,
						databasePlatformClass.getName());
			}
		}
		return jpaProperties;
	}

	protected Class getDatabasePlatformClass(Database database) {
		switch (database) {
			case DB2: return DB2Platform.class;
			case HSQL: return HSQLPlatform.class;
			case INFORMIX: return InformixPlatform.class;
			case MYSQL: return MySQL4Platform.class;
			case ORACLE: return OraclePlatform.class;
			case POSTGRESQL: return PostgreSQLPlatform.class;
			case SQL_SERVER: return SQLServerPlatform.class;
			case SYBASE: return SybasePlatform.class;
			default: return null;
		}
	}

	public Class getEntityManagerInterface() {
		return oracle.toplink.essentials.ejb.cmp3.EntityManager.class;
	}

	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}


	public void postProcessEntityManagerFactory(EntityManagerFactory emf) {
		if (isGenerateDdl()) {
			EntityManager em = emf.createEntityManager();
			try {
				this.jpaDialect.beginTransaction(em, new DefaultTransactionDefinition());
				ConnectionHandle conHandle = this.jpaDialect.getJdbcConnection(em, false);
				Connection con = conHandle.getConnection();
				try {
					executeDdl(con, this.dropDdlTempFile);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				executeDdl(con, this.createDdlTempFile);
				em.getTransaction().commit();
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				em.close();
			}
		}
	}

	private void executeDdl(Connection con, File ddlFile) throws SQLException {
		List<String> statements = new LinkedList<String>();
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(ddlFile)));
			String lastLine = lnr.readLine();
			while (lastLine != null) {
				statements.add(lastLine);
				lastLine = lnr.readLine();
			}
			executeSchemaScript(con, statements.toArray(new String[statements.size()]));
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Failed to read DDL script at resource location '" + ddlFile + "'", ex);
		}
		finally {
			try {
				if (lnr != null)
					lnr.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Execute the given schema script on the given JDBC Connection.
	 * <p>Note that the default implementation will log unsuccessful statements
	 * and continue to execute. Override the <code>executeSchemaStatement</code>
	 * method to treat failures differently.
	 * @param con the JDBC Connection to execute the script on
	 * @param sql the SQL statements to execute
	 * @throws SQLException if thrown by JDBC methods
	 * @see #executeSchemaStatement
	 */
	private void executeSchemaScript(Connection con, String[] sql) throws SQLException {
		if (sql != null && sql.length > 0) {
			boolean oldAutoCommit = con.getAutoCommit();
			if (!oldAutoCommit) {
				con.setAutoCommit(true);
			}
			try {
				Statement stmt = con.createStatement();
				try {
					for (int i = 0; i < sql.length; i++) {
						executeSchemaStatement(stmt, sql[i]);
					}
				}
				finally {
					JdbcUtils.closeStatement(stmt);
				}
			}
			finally {
				if (!oldAutoCommit) {
					con.setAutoCommit(false);
				}
			}
		}
	}

	/**
	 * Execute the given schema SQL on the given JDBC Statement.
	 * <p>Note that the default implementation will log unsuccessful statements
	 * and continue to execute. Override this method to treat failures differently.
	 * @param stmt the JDBC Statement to execute the SQL on
	 * @param sql the SQL statement to execute
	 */
	protected void executeSchemaStatement(Statement stmt, String sql) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing schema statement: " + sql);
		}
		try {
			stmt.executeUpdate(sql);
		}
		catch (SQLException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unsuccessful schema statement: " + sql, ex);
			}
		}
	}
}
