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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapter implementation of the ResultReader interface that delegates to
 * a RowMapper which is supposed to create an object for each row.
 * Each object is added to the results list of this ResultReader.
 *
 * <p>Useful for the typical case of one object per row in the database table.
 * The number of entries in the results list will match the number of rows.
 *
 * <p>Note that a RowMapper object is typically stateless and thus reusable;
 * just the RowMapperResultReader adapter is stateful.
 *
 * <p>A usage example with JdbcTemplate:
 *
 * <pre>
 * JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object
 * RowMapper rowMapper = new UserRowMapper();  // reusable object
 *
 * List allUsers = jdbcTemplate.query("select * from user", new RowMapperResultReader(rowMapper, 10));
 *
 * List userResults = jdbcTemplate.query("select * from user where id=?", new Object[] {id},
 *                                       new RowMapperResultReader(rowMapper, 1));
 * User user = (User) DataAccessUtils.uniqueResult(userResults);</pre>
 *
 * <p>Alternatively, consider subclassing MappingSqlQuery from the jdbc.object
 * package: Instead of working with separate JdbcTemplate and RowMapper objects,
 * you can have executable query objects (containing row-mapping logic) there.
 *
 * @author Juergen Hoeller
 * @since 25.05.2004
 * @see RowMapper
 * @see org.springframework.dao.support.DataAccessUtils#uniqueResult
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public class RowMapperResultReader implements ResultReader {

	/** List to save results in */
	private final List results;

	/** The RowMapper implementation that will be used to map rows */
	private final RowMapper rowMapper;

	/** The counter used to count rows */
	private int rowNum = 0;

	/**
	 * Create a new RowMapperResultReader.
	 * @param rowMapper the RowMapper which creates an object for each row
	 */
	public RowMapperResultReader(RowMapper rowMapper) {
		this(rowMapper, 0);
	}

	/**
	 * Create a new RowMapperResultReader.
	 * @param rowMapper the RowMapper which creates an object for each row
	 * @param rowsExpected the number of expected rows
	 * (just used for optimized collection handling)
	 */
	public RowMapperResultReader(RowMapper rowMapper, int rowsExpected) {
		// Use the more efficient collection if we know how many rows to expect:
		// ArrayList in case of a known row count, LinkedList if unknown
		this.results = (rowsExpected > 0) ? (List) new ArrayList(rowsExpected) : (List) new LinkedList();
		this.rowMapper = rowMapper;
	}

	public void processRow(ResultSet rs) throws SQLException {
		this.results.add(this.rowMapper.mapRow(rs, this.rowNum++));
	}

	public List getResults() {
		return this.results;
	}

}
