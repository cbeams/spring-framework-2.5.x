package org.springframework.jdbc.core.simple.metadata;

/**
 * @author trisberg
 */
public class TableParameterMetaData {
	private String parameterName;
	private int sqlType;
	private boolean generated;
	private boolean nullable;


	public TableParameterMetaData(String columnName, int sqlType, boolean generated, boolean nullable) {
		this.parameterName = columnName;
		this.sqlType = sqlType;
		this.nullable = nullable;
	}


	public String getParameterName() {
		return parameterName;
	}

	public int getSqlType() {
		return sqlType;
	}

	public boolean isGenerated() {
		return generated;
	}

	public boolean isNullable() {
		return nullable;
	}
}
