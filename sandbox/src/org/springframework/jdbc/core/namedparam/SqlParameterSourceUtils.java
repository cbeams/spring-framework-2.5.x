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

package org.springframework.jdbc.core.namedparam;

import java.util.Map;

/**
 * Class that provides helper methods for the use of SqlParameterSource with SimpleJdbc classes.
 *
 * @author Thomas Risberg
 * @since 2.1
 * @see org.springframework.jdbc.core.simple.SimpleJdbcInsert
 */
public class SqlParameterSourceUtils {

	/**
	 * Create an array of MapSqlParameterSource objects populated with data from the values passed in.
	 * This will define what is included in a batch operation.
	 *
	 * @param valueMaps array of Maps containing the values to be used
	 * @return an array of SqlParameterSource
	 */
	public static SqlParameterSource[] createBatch(Map[] valueMaps) {
		MapSqlParameterSource[] batch = new MapSqlParameterSource[valueMaps.length];
		int i = 0;
		for (Map valueMap : valueMaps) {
			batch[i++] = new MapSqlParameterSource(valueMap);
		}
		return batch;
	}

	/**
	 * Create an array of BeanPropertySqlParameterSource objects populated with data from the values passed in.
	 * This will define what is included in a batch operation.
	 *
	 * @param beans object array of beans containing the values to be used
	 * @return an array of SqlParameterSource
	 */
	public static SqlParameterSource[] createBatch(Object[] beans) {
		BeanPropertySqlParameterSource[] batch = new BeanPropertySqlParameterSource[beans.length];
		int i = 0;
		for (Object bean : beans) {
			batch[i++] = new BeanPropertySqlParameterSource(bean);
		}
		return batch;
	}

}
