package org.springframework.selector;

import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.Result;

import java.util.Map;

/**
 * Interface implemented by selectors.
 * @author Jawaid Hakim.
 */
public interface ISelector
{
	/**
	 * Evaluate the selector.
	 * @param identifiers Value for each non-null identifier in the selector.
	 * @return Returns <tt>true</tt> if the data passes the selector. Otherwise, returns
	 * <tt>false</tt>.
	 * @see #getIdentifiers()
	 */
    public Result eval(final Map identifiers);

	/**
	 * Evaluate the selector.
	 * @param provider Value provider. During evaluation of the selector callbacks
	 * are made on the value provider to get identifier values.
	 * @param corr Correlation data. Passed as-is to the value provider.
	 * @return Result evaluating the selector.
	 */
    public Result eval(IValueProvider provider, Object corr);

	/**
	 * Get identifiers used by the selector. The key into the <tt>Map</tt> is 
	 * the name of the identifier and the value is an instance of <tt>Identifier</tt>.
	 * @return Readonly <tt>Map</tt> of identifiers that are used within the selector.
	 * @throws UnsupportedOperationException
	 * @see #eval(Map)
	 */
    public Map getIdentifiers();

    /**
     * Get the selector.
     * @return Selector.
     */
    public String getSelector();
}
