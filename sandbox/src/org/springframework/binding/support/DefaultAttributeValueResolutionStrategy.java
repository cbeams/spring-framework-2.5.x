/**
 * 
 */
package org.springframework.binding.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.AttributeValueResolutionStrategy;
import org.springframework.binding.NoSuchAttributeValueException;
import org.springframework.util.StringUtils;

public class DefaultAttributeValueResolutionStrategy implements AttributeValueResolutionStrategy {

	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	public boolean isValuePlaceholder(String value) {
		if (!StringUtils.hasText(value)) {
			return false;
		}
		return value.startsWith(placeholderPrefix) && value.endsWith(placeholderSuffix);
	}

	public Object resolveAttributeValue(String valuePlaceholder, AttributeSource parameters)
			throws NoSuchAttributeValueException {
		return parseStringValue(valuePlaceholder, parameters, null);
	}

	protected String parseStringValue(String strVal, AttributeSource parameters, String originalPlaceholder) {
		StringBuffer buf = new StringBuffer(strVal);
		// The following code does not use JDK 1.4's StringBuffer.indexOf(String)
		// method to retain JDK 1.3 compatibility. The slight loss in performance
		// is not really relevant, as this code will typically just run on startup.
		int startIndex = strVal.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			int endIndex = buf.toString().indexOf(this.placeholderSuffix, startIndex + this.placeholderPrefix.length());
			if (endIndex != -1) {
				String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholderToUse = null;
				if (originalPlaceholder != null) {
					originalPlaceholderToUse = originalPlaceholder;
					if (placeholder.equals(originalPlaceholder)) {
						throw new BeanDefinitionStoreException("Circular placeholder reference '" + placeholder
								+ "' in property definitions [" + parameters + "]");
					}
				}
				else {
					originalPlaceholderToUse = placeholder;
				}
				String propVal = (String)parameters.getAttribute(placeholder);
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					propVal = parseStringValue(propVal, parameters, originalPlaceholderToUse);
					buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					startIndex = buf.toString().indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else {
					throw new NoSuchAttributeValueException(strVal);
				}
			}
			else {
				startIndex = -1;
			}
		}
		return buf.toString();
	}
}