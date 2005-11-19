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

package org.springframework.jdbc.command;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlNamedParameters;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Implementation providing a basic set of JDBC operations expressed as a command object.
 * This class is loosely modelled after the .NET SqlCommand class.
 * All database access is done via the JdbcTemplate.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.command.SqlCommand
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SqlCommand implements SqlCommandOperations {
    private String sql;
    private JdbcTemplate jdbcTemplate;

    public SqlCommand(String sql, DataSource dataSource) {
        this.sql = sql;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Object executeScalar() {
        return jdbcTemplate.queryForObject(sql, Object.class);
    }

    public Object executeScalar(Map parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, Object.class);
    }

    public Object executeScalar(SqlNamedParameters parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, Object.class);
    }

    public Object executeObject(RowMapper rowMapper) {
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, Map parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, SqlNamedParameters parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    public List executeQuery() {
        return jdbcTemplate.queryForList(sql);
    }

    public List executeQuery(Map parameters) {
        return jdbcTemplate.queryForList(sql, parameters);
    }

    public List executeQuery(SqlNamedParameters parameters) {
        return jdbcTemplate.queryForList(sql, parameters);
    }

    public List executeQuery(RowMapper rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, Map parameters) {
        return jdbcTemplate.query(sql, parameters, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, SqlNamedParameters parameters) {
        return jdbcTemplate.query(sql, parameters, rowMapper);
    }

    public SqlRowSet executeRowSet() {
        return jdbcTemplate.queryForRowSet(sql);
    }

    public SqlRowSet executeRowSet(Map parameters) {
        return jdbcTemplate.queryForRowSet(sql, parameters);
    }

    public SqlRowSet executeRowSet(SqlNamedParameters parameters) {
        return jdbcTemplate.queryForRowSet(sql, parameters);
    }

    public int executeUpdate() {
        return jdbcTemplate.update(sql);
    }

    public int executeUpdate(Map parameters) {
        return jdbcTemplate.update(sql, parameters);
    }

    public int executeUpdate(SqlNamedParameters parameters) {
        return jdbcTemplate.update(sql, parameters);
    }
}
