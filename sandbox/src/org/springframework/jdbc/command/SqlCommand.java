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

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcNamedParameterTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.ParsedSql;
import org.springframework.jdbc.support.KeyHolder;

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
public class SqlCommand implements org.springframework.jdbc.command.SqlCommandOperations {
    private String sql;
    private ParsedSql parsedSql;
    private SqlNamedParameterTypes sqlTypes = new SqlNamedParameterTypes();
    private JdbcNamedParameterTemplate jdbcTemplate;

    public SqlCommand(String sql, DataSource dataSource) {
        this.sql = sql;
        this.parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        this.jdbcTemplate = new JdbcNamedParameterTemplate(dataSource);
    }

    public Object executeScalar() {
        return jdbcTemplate.queryForObject(sql, Object.class);
    }

    public Object executeScalar(Map parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, Object.class);
    }

    public Object executeScalar(SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.queryForObject(sql, parameterHolder, sqlTypes, Object.class);
    }

    public Object executeObject(RowMapper rowMapper) {
        return jdbcTemplate.queryForObject(parsedSql.getNewSql(), rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, Map parameters) {
        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.queryForObject(sql, parameterHolder, sqlTypes, rowMapper);
    }

    public List executeQuery() {
        return jdbcTemplate.queryForList(parsedSql.getNewSql());
    }

    public List executeQuery(Map parameters) {
        return jdbcTemplate.queryForList(sql, parameters);
    }

    public List executeQuery(SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.queryForList(sql, parameterHolder, sqlTypes);
    }

    public List executeQuery(RowMapper rowMapper) {
        return jdbcTemplate.query(parsedSql.getNewSql(), rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, Map parameters) {
        return jdbcTemplate.query(sql, parameters, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.query(sql, parameterHolder, sqlTypes, rowMapper);
    }

    public SqlRowSet executeRowSet() {
        return jdbcTemplate.queryForRowSet(parsedSql.getNewSql());
    }

    public SqlRowSet executeRowSet(Map parameters) {
        return jdbcTemplate.queryForRowSet(sql, parameters);
    }

    public SqlRowSet executeRowSet(SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.queryForRowSet(sql, parameterHolder, sqlTypes);
    }

    public int executeUpdate() {
        return jdbcTemplate.update(parsedSql.getNewSql());
    }

    public int executeUpdate(Map parameters) {
        return jdbcTemplate.update(sql, parameters);
    }

    public int executeUpdate(SqlNamedParameterHolder parameterHolder) {
        return jdbcTemplate.update(sql, parameterHolder, sqlTypes);
    }

    public int executeUpdate(SqlNamedParameterHolder parameterHolder, KeyHolder keyHolder) {
        return executeUpdate(parameterHolder, keyHolder, null);
    }

    public int executeUpdate(SqlNamedParameterHolder parameterHolder, KeyHolder keyHolder, String[] keyColumnNames) {
        return jdbcTemplate.update(sql, parameterHolder, sqlTypes, keyHolder, keyColumnNames);
    }

    public Map getSqlTypes() {
        return sqlTypes.getTypes();
    }

    public void setSqlTypes(Map types) {
        this.sqlTypes = new SqlNamedParameterTypes(types);
    }

    public void setSqlTypes(SqlNamedParameterTypes namedTypes) {
        this.sqlTypes = namedTypes;
    }
}
