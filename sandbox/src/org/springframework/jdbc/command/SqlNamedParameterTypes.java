package org.springframework.jdbc.command;

import java.util.Map;
import java.util.HashMap;

public class SqlNamedParameterTypes {
    Map parameterTypes = new HashMap();

    public SqlNamedParameterTypes() {
    }

    public SqlNamedParameterTypes(Map sqlTypes) {
        this.parameterTypes.putAll(sqlTypes);
    }

    public SqlNamedParameterTypes(String parameterName, int sqlType) {
        this.parameterTypes.put(parameterName, new Integer(sqlType));
    }

    public SqlNamedParameterTypes addType(String parameterName, int sqlType) {
        parameterTypes.put(parameterName, new Integer(sqlType));
        return this;
    }

    public Map getTypes() {
        return parameterTypes;
    }
}
