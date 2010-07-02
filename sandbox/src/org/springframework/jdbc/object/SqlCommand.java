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

package org.springframework.jdbc.object;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Implementation providing a basic set of JDBC operations expressed as a command object.
 * This class is loosely modelled after the .NET SqlCommand class.
 * All database access is done via the JdbcTemplate.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.object.SqlCommand
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SqlCommand implements SqlCommandOperations {

    private final String sql;

    private final String parsedSql;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    /**
     * Convenient constructor with DataSource and SQL String.
     * @param dataSource DataSource to use to get Connections
     * @param sql to execute
     */
    public SqlCommand(DataSource dataSource, String sql) {
        this.sql = sql;
        this.parsedSql = NamedParameterUtils.parseSqlStatementIntoString(sql);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    public Object executeScalar() {
        return this.namedParameterJdbcTemplate.getJdbcOperations().queryForObject(this.sql, Object.class);
    }

    public Object executeScalar(Map parameters) {
        return this.namedParameterJdbcTemplate.queryForObject(this.sql, parameters, Object.class);
    }

    public Object executeScalar(SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.queryForObject(this.sql, parameterSource, Object.class);
    }

    public Object executeObject(RowMapper rowMapper) {
        return this.namedParameterJdbcTemplate.getJdbcOperations().queryForObject(this.parsedSql, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, Map parameters) {
        return this.namedParameterJdbcTemplate.queryForObject(this.sql, parameters, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.queryForObject(this.sql, parameterSource, rowMapper);
    }

    public List executeQuery() {
        return this.namedParameterJdbcTemplate.getJdbcOperations().queryForList(this.parsedSql);
    }

    public List executeQuery(Map parameters) {
        return this.namedParameterJdbcTemplate.queryForList(this.sql, parameters);
    }

    public List executeQuery(SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.queryForList(this.sql, parameterSource);
    }

    public List executeQuery(RowMapper rowMapper) {
        return this.namedParameterJdbcTemplate.getJdbcOperations().query(this.parsedSql, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, Map parameters) {
        return this.namedParameterJdbcTemplate.query(this.sql, parameters, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.query(this.sql, parameterSource, rowMapper);
    }

    public SqlRowSet executeRowSet() {
        return this.namedParameterJdbcTemplate.getJdbcOperations().queryForRowSet(this.parsedSql);
    }

    public SqlRowSet executeRowSet(Map parameters) {
        return this.namedParameterJdbcTemplate.queryForRowSet(this.sql, parameters);
    }

    public SqlRowSet executeRowSet(SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.queryForRowSet(this.sql, parameterSource);
    }

    public int executeUpdate() {
        return this.namedParameterJdbcTemplate.getJdbcOperations().update(this.parsedSql);
    }

    public int executeUpdate(Map parameters) {
        return this.namedParameterJdbcTemplate.update(this.sql, parameters);
    }

    public int executeUpdate(SqlParameterSource parameterSource) {
        return this.namedParameterJdbcTemplate.update(this.sql, parameterSource);
    }

    public int executeUpdate(SqlParameterSource parameterSource, KeyHolder keyHolder) {
        return executeUpdate(parameterSource, keyHolder, null);
    }

    public int executeUpdate(SqlParameterSource parameterSource, KeyHolder keyHolder, String[] keyColumnNames) {
        return this.namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder, keyColumnNames);
    }

}
