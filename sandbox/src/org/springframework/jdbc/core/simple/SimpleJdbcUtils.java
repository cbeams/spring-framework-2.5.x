package org.springframework.jdbc.core.simple;

/**
 * @author trisberg
 */
public class SimpleJdbcUtils {

	public static String commonDatabaseName(String source) {
		String name = source;
		if (source != null && source.startsWith("DB2")) {
			name = "DB2";
		}
		else if ("Sybase SQL Server".equals(source) || "Adaptive Server Enterprise".equals(source)) {
			name = "Sybase";
		}
		return name; 
	}

	public static String convertUnderscoreNameToPropertyName(String name) {
		StringBuffer result = new StringBuffer();
		boolean nextIsUpper = false;
		if (name != null && name.length() > 0) {
			result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals("_")) {
					nextIsUpper = true;
				}
				else {
					if (nextIsUpper) {
						result.append(s.toUpperCase());
						nextIsUpper = false;
					}
					else {
						result.append(s.toLowerCase());
					}
				}
			}
		}
		return result.toString();
	}
}
