package org.springframework.jdbc.core.simple.metadata;

/**
 * @author trisberg
 */
public class CallParameterMetaData {
	private String parameterName;
	private int parameterType;
	private int sqlType;
	private String typeName;
	private boolean nullable;


	public CallParameterMetaData(String columnName, int columnType, int sqlType, String typeName, boolean nullable) {
		this.parameterName = columnName;
		this.parameterType = columnType;
		this.sqlType = sqlType;
		this.typeName = typeName;
		this.nullable = nullable;
	}


	public String getParameterName() {
		return parameterName;
	}

	public int getParameterType() {
		return parameterType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public String getTypeName() {
		return typeName;
	}

	public boolean isNullable() {
		return nullable;
	}
}