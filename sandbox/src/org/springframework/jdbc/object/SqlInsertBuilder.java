package org.springframework.jdbc.object;

import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.SqlNamedParameterHolder;
import org.springframework.jdbc.core.SqlNamedParameterValues;
import org.springframework.jdbc.core.SqlNamedParameterTypes;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class SqlInsertBuilder {
    private Map dataValues = new HashMap();
    private Map sqlTypes = new HashMap();
    private KeyHolder keyHolder;
    String[] keyColumnNames;

    public KeyHolder getKeyHolder() {
        return keyHolder;
    }

    public SqlInsertBuilder setKeyHolder(KeyHolder keyHolder) {
        this.keyHolder = keyHolder;
        return this;
    }

    public String[] getKeyColumnNames() {
        return keyColumnNames;
    }

    public SqlInsertBuilder setKeyColumnNames(String[] keyColumnNames) {
        this.keyColumnNames = keyColumnNames;
        return this;
    }

    public SqlInsertBuilder setKeyColumnName(String keyColumnName) {
        this.keyColumnNames = new String[] {keyColumnName};
        return this;
    }

    public String buildSqlToUse(String tableName) {
        StringBuffer sqlToUse = new StringBuffer("insert into " + tableName);
        StringBuffer sqlColumns = new StringBuffer();
        StringBuffer sqlParameters = new StringBuffer();
        Iterator colIter = getValues().keySet().iterator();
        int colCount = 0;
        while (colIter.hasNext()) {
            colCount++;
            String col = (String)colIter.next();
            if (colCount > 1) {
                sqlColumns.append(", ");
                sqlParameters.append(", ");
            }
            sqlColumns.append(col);
            sqlParameters.append(":" + col);
        }
        sqlToUse.append(" (");
        sqlToUse.append(sqlColumns);
        sqlToUse.append(")");
        sqlToUse.append(" values (");
        sqlToUse.append(sqlParameters);
        sqlToUse.append(")");
        return sqlToUse.toString();
    }

    public SqlInsertBuilder addColumnValue(String columnName, Object value) {
        this.dataValues.put(columnName, value);
        //this.sqlTypes.put(columnName, value);
        return this;
    }

    public SqlInsertBuilder addColumnValue(String columnName, Object value, int sqlType) {
        this.dataValues.put(columnName, value);
        this.sqlTypes.put(columnName, new Integer(sqlType));
        return this;
    }

    public void setColumnValues(Map valueMap) {
        this.dataValues.putAll(valueMap);
    }

    public void setTypes(Map sqlTypes) {
        this.sqlTypes.putAll(sqlTypes);
    }

    public Map getValues() {
        return this.dataValues;
    }

    public Map getTypes() {
        return this.sqlTypes;
    }

    public SqlNamedParameterHolder getNamedParameterHolder() {
            return new SqlNamedParameterValues(this.dataValues, this.sqlTypes);
    }

    public SqlNamedParameterTypes getNamedParameterTypes() {
            return new SqlNamedParameterTypes(this.sqlTypes);
    }

}
