package org.springframework.jdbc.command;

import java.util.Map;
import java.util.HashMap;

public class SqlParameterTypes {
    Map parameterTypes = new HashMap();

    public SqlParameterTypes() {
    }

    public SqlParameterTypes(String parameterName, int sqlType) {
        parameterTypes.put(parameterName, new Integer(sqlType));
    }

    public SqlParameterTypes addType(String parameterName, int sqlType) {
        parameterTypes.put(parameterName, new Integer(sqlType));
        return this;
    }

    public Map getTypes() {
        return parameterTypes;
    }
}
