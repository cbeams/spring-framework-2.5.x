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

package org.springframework.jdbc.core;

import java.util.List;

/**
 * Extension of RowCallbackHandler interface that saves the accumulated results
 * as a List.
 *
 * <p>Allows to make a results list available in a uniform manner. JdbcTemplate's
 * query methods will return the results list in that case, else returning null
 * (-> result state is solely available from RowCallbackHandler object).
 *
 * <p>A convenient out-of-the-box implementation of ResultReader is the
 * RowMapperResultReader adapter which delegates row mapping to a RowMapper.
 * Note that a RowMapper object is typically stateless and thus reusable;
 * just the RowMapperResultReader adapter is stateful.
 *
 * @author Rod Johnson
 * @see RowMapperResultReader
 */
public interface ResultReader extends RowCallbackHandler {
	 
	/**
	 * Return all results, disconnected from the JDBC ResultSet.
	 * Never returns null; returns the empty collection if there
	 * were no results.
	 */
	List getResults();

}
