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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * Implementation of SQLExceptionTranslator that uses specific vendor codes.
 * More precise than SQLState implementation, but vendor-specific.
 *
 * <p>This class applies the following matching rules:
 * <ul>
 * <li>Try custom translation implemented by any subclass. Note that this class is
 * concrete and is typically used itself, in which case this rule doesn't apply.
 * <li>Apply error code matching. Error codes are obtained from the SQLErrorCodesFactory
 * by default. This looks up error codes from the classpath and keys into them from the
 * database name from the database metadata.
 * <li>Fallback to fallback translator. SQLStateSQLExceptionTranslator is the default
 * fallback translator.
 * </ul>
 * 
 * @author Rod Johnson
 * @author Thomas Risberg
 * @see org.springframework.jdbc.support.SQLErrorCodesFactory
 */
public class SQLErrorCodeSQLExceptionTranslator implements SQLExceptionTranslator {

	private static final int MESSAGE_ONLY_CONSTRUCTOR = 1;
	private static final int MESSAGE_THROWABLE_CONSTRUCTOR = 2;
	private static final int MESSAGE_SQLEX_CONSTRUCTOR = 3;
	private static final int MESSAGE_SQL_THROWABLE_CONSTRUCTOR = 4;
	private static final int MESSAGE_SQL_SQLEX_CONSTRUCTOR = 5;
	protected final Log logger = LogFactory.getLog(getClass());

	/** Error codes available to subclasses */
	protected SQLErrorCodes sqlErrorCodes;
	
	/** Fallback translator to use if SQLError code matching doesn't work */
	private SQLExceptionTranslator fallback = new SQLStateSQLExceptionTranslator();


	/**
	 * Constructor for use as a JavaBean.
	 * The SqlErrorCodes or DataSource property must be set.
	 */
	public SQLErrorCodeSQLExceptionTranslator() {
	}

	/**
	 * Create a SQLErrorCode translator given these error codes.
	 * Does not require a database metadata lookup to be performed using a connection.
	 * @param sec error codes
	 */
	public SQLErrorCodeSQLExceptionTranslator(SQLErrorCodes sec) {
		this.sqlErrorCodes = sec;		
	}
	
	/**
	 * Create a SQLErrorCode translator for the given DataSource.
	 * Invoking this constructor will cause a connection to be obtained from the 
	 * DataSource to get the metadata
	 * @param ds DataSource to use to find metadata and establish which error
	 * codes are usable
	 */
	public SQLErrorCodeSQLExceptionTranslator(DataSource ds) {
		setDataSource(ds);
	}
	
	/**
	 * Set custom error codes to be used for translation
	 * @param sec custom error codes to use
	 */
	public void setSqlErrorCodes(SQLErrorCodes sec) {
		this.sqlErrorCodes = sec;
	}

	/**
	 * Set the DataSource.
	 * <p>Setting this property will cause a connection to be obtained
	 * from the DataSource to get the metadata.
	 * @param ds DataSource to use to find metadata and establish which error
	 * codes are usable
	 */
	public void setDataSource(DataSource ds) {
		this.sqlErrorCodes = SQLErrorCodesFactory.getInstance().getErrorCodes(ds);
	}
	
	/**
	 * Override the default SQLState fallback translator
	 * @param fallback custom fallback exception translator to use if error code
	 * translation fails
	 */
	public void setFallbackTranslator(SQLExceptionTranslator fallback) {
		this.fallback = fallback;
	}


	public DataAccessException translate(String task, String sql, SQLException sqlex) {
		if (task == null) {
			task = "";
		}
		if (sql == null) {
			sql = "";
		}
		
		// first, try custom translation from overridden method
		DataAccessException dex = customTranslate(task, sql, sqlex);
		if (dex != null) {
			return dex;
		}
		
		// now try error code
		String errorCode;
		if (this.sqlErrorCodes != null && this.sqlErrorCodes.isUseSqlStateForTranslation()) {
			errorCode = sqlex.getSQLState();
		}
		else {
			errorCode = Integer.toString(sqlex.getErrorCode());
		}

		if (this.sqlErrorCodes != null && errorCode != null) {
			// look for defined custom translations first
			if (!this.sqlErrorCodes.getCustomTranslations().isEmpty()) {
				Iterator customIter = this.sqlErrorCodes.getCustomTranslations().iterator();
				while (customIter.hasNext()) {
					CustomSQLErrorCodesTranslation customCode = (CustomSQLErrorCodesTranslation) customIter.next();
					if (Arrays.binarySearch(customCode.getErrorCodes(), errorCode) >= 0) {
						Class exceptionClass = null;
						DataAccessException customException = null;
						try {
							ClassLoader cl = Thread.currentThread().getContextClassLoader();
							exceptionClass = cl.loadClass(customCode.getExceptionClass());
						}
						catch (ClassNotFoundException ex) {
							logger.warn("Unable to load custom exception class: " + customCode.getExceptionClass());
						}
						if (exceptionClass != null) {
							try {
								int constructorType = 0;
								Constructor[] constructors = exceptionClass.getConstructors();
								for (int i = 0; i < constructors.length; i++) {
									Class[] parameterTypes = constructors[i].getParameterTypes();
									if (parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
										if (constructorType < MESSAGE_ONLY_CONSTRUCTOR)
											constructorType = MESSAGE_ONLY_CONSTRUCTOR;
									}
									if (parameterTypes.length == 2 && parameterTypes[0].equals(String.class) &&
											parameterTypes[1].equals(Throwable.class)) {
										if (constructorType < MESSAGE_THROWABLE_CONSTRUCTOR)
											constructorType = MESSAGE_THROWABLE_CONSTRUCTOR;
									}
									if (parameterTypes.length == 2 && parameterTypes[0].equals(String.class) &&
											parameterTypes[1].equals(SQLException.class)) {
										if (constructorType < MESSAGE_SQLEX_CONSTRUCTOR)
											constructorType = MESSAGE_SQLEX_CONSTRUCTOR;
									}
									if (parameterTypes.length == 3 && parameterTypes[0].equals(String.class) &&
											parameterTypes[1].equals(String.class) && parameterTypes[2].equals(Throwable.class)) {
										if (constructorType < MESSAGE_SQL_THROWABLE_CONSTRUCTOR)
											constructorType = MESSAGE_SQL_THROWABLE_CONSTRUCTOR;
									}
									if (parameterTypes.length == 3 && parameterTypes[0].equals(String.class) &&
											parameterTypes[1].equals(String.class) && parameterTypes[2].equals(SQLException.class)) {
										if (constructorType < MESSAGE_SQL_SQLEX_CONSTRUCTOR)
											constructorType = MESSAGE_SQL_SQLEX_CONSTRUCTOR;
									}
								}
								Constructor exceptionConstructor = null;
								switch(constructorType) {
									case MESSAGE_SQL_SQLEX_CONSTRUCTOR:
										Class[] messageAndSqlAndSqlExArgsClass = new Class[] {String.class, String.class, SQLException.class};
										Object[] messageAndSqlAndSqlExArgs = new Object[] {task, sql, sqlex};
										exceptionConstructor = exceptionClass.getConstructor(messageAndSqlAndSqlExArgsClass);
										customException = (DataAccessException)exceptionConstructor.newInstance(messageAndSqlAndSqlExArgs);
										break;
									case MESSAGE_SQL_THROWABLE_CONSTRUCTOR:
										Class[] messageAndSqlAndThrowableArgsClass = new Class[] {String.class, String.class, Throwable.class};
										Object[] messageAndSqlAndThrowableArgs = new Object[] {task, sql, sqlex};
										exceptionConstructor = exceptionClass.getConstructor(messageAndSqlAndThrowableArgsClass);
										customException = (DataAccessException)exceptionConstructor.newInstance(messageAndSqlAndThrowableArgs);
										break;
									case MESSAGE_SQLEX_CONSTRUCTOR:
										Class[] messageAndSqlExArgsClass = new Class[] {String.class, SQLException.class};
										Object[] messageAndSqlExArgs = new Object[] {task + ": " + sqlex.getMessage(), sqlex};
										exceptionConstructor = exceptionClass.getConstructor(messageAndSqlExArgsClass);
										customException = (DataAccessException)exceptionConstructor.newInstance(messageAndSqlExArgs);
										break;
									case MESSAGE_THROWABLE_CONSTRUCTOR:
										Class[] messageAndThrowableArgsClass = new Class[] {String.class, Throwable.class};
										Object[] messageAndThrowableArgs = new Object[] {task + ": " + sqlex.getMessage(), sqlex};
										exceptionConstructor = exceptionClass.getConstructor(messageAndThrowableArgsClass);
										customException = (DataAccessException)exceptionConstructor.newInstance(messageAndThrowableArgs);
										break;
									case MESSAGE_ONLY_CONSTRUCTOR:
										Class[] messageOnlyArgsClass = new Class[] {String.class};
										Object[] messageOnlyArgs = new Object[] {task + ": " + sqlex.getMessage()};
										exceptionConstructor = exceptionClass.getConstructor(messageOnlyArgsClass);
										customException = (DataAccessException)exceptionConstructor.newInstance(messageOnlyArgs);
										break;
									default:
										logger.warn("Unable to find necessary constructor for custom exception class [" +
																customCode.getExceptionClass() + "]");
									}
							}
							catch (ClassCastException ex) {
								logger.warn("Unable to instantiate custom exception class [" + customCode.getExceptionClass() +
														"]. It is not a subclass of [" + DataAccessException.class + "].");
							}
							catch (Exception ex) {
								logger.warn("Unable to instantiate custom exception class [" + customCode.getExceptionClass() + "]", ex);
							}
							if (customException != null) {
								logTranslation(task, sql, sqlex, true);
								return customException;
							}
						}
					}
				}
			}
				
			//next, look for grouped exceptions
			if (Arrays.binarySearch(this.sqlErrorCodes.getBadSqlGrammarCodes(), errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new BadSqlGrammarException(task, sql, sqlex);
			}
			else if (Arrays.binarySearch(this.sqlErrorCodes.getDataIntegrityViolationCodes() , errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
			}
			else if (Arrays.binarySearch(this.sqlErrorCodes.getDataRetrievalFailureCodes() , errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new DataRetrievalFailureException(task + ": " + sqlex.getMessage(), sqlex);
			}
			else if (Arrays.binarySearch(this.sqlErrorCodes.getOptimisticLockingFailureCodes() , errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new OptimisticLockingFailureException(task + ": " + sqlex.getMessage(), sqlex);
			}
			else if (Arrays.binarySearch(this.sqlErrorCodes.getCannotAcquireLockCodes() , errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new CannotAcquireLockException(task + ": " + sqlex.getMessage(), sqlex);
			}
			else if (Arrays.binarySearch(this.sqlErrorCodes.getDataAccessResourceFailureCodes() , errorCode) >= 0) {
				logTranslation(task, sql, sqlex, false);
				return new DataAccessResourceFailureException(task + ": " + sqlex.getMessage(), sqlex);
			}
		}

		// we couldn't identify it more precisely - let's hand it over to the SQLState fallback translator
		logger.warn("Unable to translate SQLException with errorCode '" + sqlex.getErrorCode() +
		            "', will now try the fallback translator");
		return this.fallback.translate(task, sql, sqlex);
	}

	/**
	 * Subclasses can override this method to attempt a custom mapping from SQLException
	 * to DataAccessException.
	 * @param task task being attempted
	 * @param sql SQL that caused the problem
	 * @param sqlex offending SQLException
	 * @return null if no custom translation was possible, otherwise a DataAccessException
	 * resulting from custom translation. This exception should include the sqlex parameter
	 * as a nested root cause. This implementation always returns null, meaning that
	 * the translator always falls back to the default error codes.
	 */
	protected DataAccessException customTranslate(String task, String sql, SQLException sqlex) {
		return null;
	}

	private void logTranslation(String task, String sql, SQLException sqlex, boolean custom) {
		if (logger.isInfoEnabled()) {
			String intro = custom ? "Custom translation of" : "Translating";
			logger.info(intro + " SQLException with SQLState '" + sqlex.getSQLState() +
									"' and errorCode '" + sqlex.getErrorCode() + "' and message [" + sqlex.getMessage() +
									"]; SQL was [" + sql + "] for task [" + task + "]");
		}
	}

}
