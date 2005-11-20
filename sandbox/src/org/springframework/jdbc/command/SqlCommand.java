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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.ParsedSql;

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
    private SqlParameterTypes sqlTypes = new SqlParameterTypes();
    private JdbcTemplate jdbcTemplate;

    public SqlCommand(String sql, DataSource dataSource) {
        this.sql = sql;
        this.parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Object executeScalar() {
        return jdbcTemplate.queryForObject(sql, Object.class);
    }

    public Object executeScalar(Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.queryForObject(substitutedSql, args, Object.class);
    }

    public Object executeScalar(SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.queryForObject(substitutedSql, args, argTypes, Object.class);
    }

    public Object executeObject(RowMapper rowMapper) {
        return jdbcTemplate.queryForObject(parsedSql.getNewSql(), rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.queryForObject(substitutedSql, args, rowMapper);
    }

    public Object executeObject(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.queryForObject(substitutedSql, args, argTypes, rowMapper);
    }

    public List executeQuery() {
        return jdbcTemplate.queryForList(parsedSql.getNewSql());
    }

    public List executeQuery(Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.queryForList(substitutedSql, args);
    }

    public List executeQuery(SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.queryForList(substitutedSql, args, argTypes);
    }

    public List executeQuery(RowMapper rowMapper) {
        return jdbcTemplate.query(parsedSql.getNewSql(), rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.query(substitutedSql, args, rowMapper);
    }

    public List executeQuery(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.query(substitutedSql, args, argTypes, rowMapper);
    }

    public SqlRowSet executeRowSet() {
        return jdbcTemplate.queryForRowSet(parsedSql.getNewSql());
    }

    public SqlRowSet executeRowSet(Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.queryForRowSet(substitutedSql, args);
    }

    public SqlRowSet executeRowSet(SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.queryForRowSet(substitutedSql, args, argTypes);
    }

    public int executeUpdate() {
        return jdbcTemplate.update(parsedSql.getNewSql());
    }

    public int executeUpdate(Map parameters) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameters, parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameters);
        return jdbcTemplate.update(substitutedSql, args);
    }

    public int executeUpdate(SqlNamedParameterHolder parameterHolder) {
        Object[] args = NamedParameterUtils.convertArgMapToArray(parameterHolder.getValues(), parsedSql);
        int[] argTypes = NamedParameterUtils.convertTypeMapToArray(sqlTypes.getTypes(), parsedSql);
        String substitutedSql = NamedParameterUtils.substituteNamedParameters(sql, parameterHolder.getValues());
        return jdbcTemplate.update(substitutedSql, args, argTypes);
    }

    public Map getSqlTypes() {
        return sqlTypes.getTypes();
    }

    public void setSqlTypes(Map types) {
    }
}
