package org.springframework.selector.parser;

/**
 * Value provider interface. A value provider allows an application to provide identifier
 * values on demand. As a selector is evaluated, callbacks are made on the value provider 
 * when an identifier's value is needed. 
 * @author Jawaid Hakim.
 * @see org.springframework.selector.Selector
 */
public interface IValueProvider
{
    /**
     * Get value of specified identifier.
     * @param identifier Identifier.
     * @param correlation Correlation data. May be <tt>null</tt>.
     * @return Value of specified identifier. Returns <tt>null</tt> if identifier is not found.
     * Numeric values - <tt>Integer</tt>, <tt>Short</tt>, <tt>Long</tt>, <tt>Float</tt>, 
     * <tt>Double</tt>, and <tt>Byte</tt> - MUST be wrapped in an instance of <tt>NumericValue</tt>.
     * @see NumericValue
     */
    public Object getValue(Object identifier, Object correlation);
}
