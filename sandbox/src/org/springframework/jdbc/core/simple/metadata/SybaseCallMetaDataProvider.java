package org.springframework.jdbc.core.simple.metadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author trisberg
 */
public class SybaseCallMetaDataProvider extends AbstractCallMetaDataProvider {

	private static final String REMOVABLE_COLUMN_PREFIX = "@";
	private static final String RETURN_VALUE_NAME = "RETURN_VALUE";

	public SybaseCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	@Override
	protected String parameterNameToUse(String parameterName) {
		if (parameterName == null)
			return null;
		if (parameterName.length() > 1 && parameterName.startsWith(REMOVABLE_COLUMN_PREFIX))
			return super.parameterNameToUse(parameterName.substring(1));
		else
			return super.parameterNameToUse(parameterName);
	}

	@Override
	protected boolean byPassReturnParameter(String parameterName) {
		if (RETURN_VALUE_NAME.equals(parameterName))
			return true;
		else
			return super.byPassReturnParameter(parameterName);
	}
}
