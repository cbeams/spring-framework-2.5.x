package org.springframework.core.style;

/**
 * Simple utility class to allow for convenient access to value 
 * styling logic, mainly to support descriptive logging messages.
 * @author Keith
 */
public class StylerUtils {
	
	/**
	 * Style the specified value according to default conventions.
	 * @param value The value
	 * @return the styled string
	 */
	public static String style(Object value) {
		return ToStringCreator.DEFAULT_VALUE_STYLER.style(value);
	}
}
