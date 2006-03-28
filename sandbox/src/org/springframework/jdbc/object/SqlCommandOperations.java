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

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlNamedParameterHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;
import java.util.Map;

/**
 * Interface specifying a basic set of JDBC operations expressed as a object object.
 * This interface is loosely modelled after the .NET SqlCommand class.
 * This interface is not often used directly, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.object.SqlCommand
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public interface SqlCommandOperations {

    Object executeScalar();

    Object executeScalar(Map parameters);

    Object executeScalar(SqlNamedParameterHolder parameters);

    Object executeObject(RowMapper rowMapper);

    Object executeObject(RowMapper rowMapper, Map parameters);

    Object executeObject(RowMapper rowMapper, SqlNamedParameterHolder parameters);

    List executeQuery();

    List executeQuery(Map parameters);

    List executeQuery(SqlNamedParameterHolder parameters);

    List executeQuery(RowMapper rowMapper);

    List executeQuery(RowMapper rowMapper, Map parameters);

    List executeQuery(RowMapper rowMapper, SqlNamedParameterHolder parameters);

    SqlRowSet executeRowSet();

    SqlRowSet executeRowSet(Map parameters);

    SqlRowSet executeRowSet(SqlNamedParameterHolder parameters);

    int executeUpdate();

    int executeUpdate(Map parameters);

    int executeUpdate(SqlNamedParameterHolder parameters);

}
