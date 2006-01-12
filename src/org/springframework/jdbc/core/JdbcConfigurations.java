/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

/**
 * Interface specifying a basic set of JDBC configuration options that can be set.
 * Implemented by JdbcTemplate and any Template class wrapping the JdbcTemplate.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see JdbcTemplate
 */
public interface JdbcConfigurations {
	/**
	 * Set a NativeJdbcExtractor to extract native JDBC objects from wrapped handles.
	 * Useful if native Statement and/or ResultSet handles are expected for casting
	 * to database-specific implementation classes, but a connection pool that wraps
	 * JDBC objects is used (note: <i>any</i> pool will return wrapped Connections).
	 */
	void setNativeJdbcExtractor(NativeJdbcExtractor extractor);

	/**
	 * Return the current NativeJdbcExtractor implementation.
	 */
	NativeJdbcExtractor getNativeJdbcExtractor();

	/**
	 * Set whether or not we want to ignore SQLWarnings.
	 * Default is "true".
	 */
	void setIgnoreWarnings(boolean ignoreWarnings);

	/**
	 * Return whether or not we ignore SQLWarnings. Default is "true".
	 */
	boolean isIgnoreWarnings();

	/**
	 * Set the fetch size for this JdbcTemplate. This is important for processing
	 * large result sets: Setting this higher than the default value will increase
	 * processing speed at the cost of memory consumption; setting this lower can
	 * avoid transferring row data that will never be read by the application.
	 * <p>Default is 0, indicating to use the JDBC driver's default.
	 */
	void setFetchSize(int fetchSize);

	/**
	 * Return the fetch size specified for this JdbcTemplate.
	 */
	int getFetchSize();

	/**
	 * Set the maximum number of rows for this JdbcTemplate. This is important
	 * for processing subsets of large result sets, avoiding to read and hold
	 * the entire result set in the database or in the JDBC driver if we're
	 * never interested in the entire result in the first place (for example,
	 * when performing searches that might return a large number of matches).
	 * <p>Default is 0, indicating to use the JDBC driver's default.
	 */
	void setMaxRows(int maxRows);

	/**
	 * Return the maximum number of rows specified for this JdbcTemplate.
	 */
	int getMaxRows();
}
