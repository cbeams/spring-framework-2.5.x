/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.Assert;

/**
 * A simple implementation of a prepared statement setter which sets variable
 * parameter values on a prepared statement.
 * 
 * @author Keith Donald
 */
public class SimplePreparedStatementSetter implements PreparedStatementSetter {
    /** List of SqlParameter objects. May not be null. */
    private final List declaredParameters;

    private Object[] parameters;

    /**
     * Creates a SimplePreparedStatementSetter
     * 
     * @param declaredParameters
     *            The list of declared parameters to be set by this setter.
     */
    public SimplePreparedStatementSetter(List declaredParameters) {
        Assert.notNull(declaredParameters);
        this.declaredParameters = declaredParameters;
    }

    /**
     * Configures the variable parameter values to set for this setter.
     * 
     * @param parameters
     *            The parameters.
     */
    public void setParameters(Object[] parameters) {
        Assert.notNull(parameters);
        this.parameters = parameters;
    }

    /**
     * @see org.springframework.jdbc.core.PreparedStatementSetter#setValues(java.sql.PreparedStatement)
     */
    public void setValues(PreparedStatement ps) throws SQLException {
        setPreparedStatementParameters(ps, this.parameters);
    }

    public void setPreparedStatementParameters(PreparedStatement ps,
            Object[] parameters) throws SQLException {
        Assert.isTrue(parameters.length == declaredParameters.size());
        int i = 0;
        for (Iterator it = declaredParameters.iterator(); it.hasNext(); i++) {
            SqlParameter declaredParameter = (SqlParameter)it.next();
            // We need SQL type to be able to set null
            if (i >= parameters.length || parameters[i] == null) {
                ps.setNull(i + 1, declaredParameter.getSqlType());
            }
            else {
                // Documentation?
                // PARAMETERIZE THIS TO A TYPE MAP INTERFACE?
                switch (declaredParameter.getSqlType()) {
                case Types.VARCHAR:
                    ps.setString(i + 1, (String)parameters[i]);
                    break;
                default:
                    ps.setObject(i + 1, parameters[i], declaredParameter
                            .getSqlType());
                    break;
                }
            }
        }
    }

}