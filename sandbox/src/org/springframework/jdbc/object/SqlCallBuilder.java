package org.springframework.jdbc.object;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.object.SqlCall;

import java.util.Map;
import java.util.HashMap;

public class SqlCallBuilder {
    private Map sqlParameters = new HashMap();
    private Map dataValues = new HashMap();
    private String procedureName;

    public SqlCall buildSqlToUse(String procedureName) {
        SqlCall call = new SqlCall() {

        };

        return null;
    }

    public SqlCallBuilder addParameterValue(String parameterName, Object value) {
        this.dataValues.put(parameterName, value);
        //this.sqlTypes.put(parameterName, value);
        return this;
    }

    public SqlCallBuilder addParameterValue(String parameterName, Object value, int sqlType) {
        this.dataValues.put(parameterName, value);
        this.sqlParameters.put(parameterName, new SqlParameter(parameterName, sqlType));
        return this;
    }

    public SqlCallBuilder addOutParameter(String parameterName, int sqlType) {
        //this.dataValues.put(parameterName, value);
        this.sqlParameters.put(parameterName, new SqlOutParameter(parameterName, sqlType));
        return this;
    }

    public SqlCallBuilder addOutParameter(String parameterName, Object value, int sqlType) {
        this.dataValues.put(parameterName, value);
        this.sqlParameters.put(parameterName, new SqlOutParameter(parameterName, sqlType));
        return this;
    }

    public Map getValues() {
        return this.dataValues;
    }

    public Map getParameters() {
        return this.sqlParameters;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
