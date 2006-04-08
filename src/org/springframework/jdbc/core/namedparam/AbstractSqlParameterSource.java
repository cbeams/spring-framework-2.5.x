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

package org.springframework.jdbc.core.namedparam;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for SqlParameterSource implementations.
 * Provides registration of SQL types per parameter.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractSqlParameterSource implements SqlParameterSource {

	private final Map sqlTypes = new HashMap();


	/**
	 * Register a SQL type for the given parameter.
	 * @param paramName the name of the parameter
	 * @param sqlType the SQL type of the parameter
	 */
	public void registerSqlType(String paramName, int sqlType) {
		this.sqlTypes.put(paramName, new Integer(sqlType));
	}

	/**
	 * Return the SQL type for the given parameter, if registered.
	 * @param paramName the name of the parameter
	 * @return the SQL type of the parameter,
	 * or <code>TYPE_UNKNOWN</code> if not registered
	 */
	public int getSqlType(String paramName) {
		Integer sqlType = (Integer) this.sqlTypes.get(paramName);
		if (sqlType != null) {
			return sqlType.intValue();
		}
		else {
			return TYPE_UNKNOWN;
		}
	}

}
