package org.springframework.selector.parser;

/**
 * Class to encapsulate numeric values.<p>
 * This class allows easier conversion to <tt>C#</tt> using the JLCA tool from Microsoft since there 
 * is no <tt>Number</tt> class in <tt>C#</tt>.
 * @author jawaid.hakim
 */
public class NumericValue
{
	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Double value)
	{
		value_ = value;
	}
	
	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Float value)
	{
		value_ = value;
	}

	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Long value)
	{
		value_ = value;
	}

	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Integer value)
	{
		value_ = value;
	}

	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Byte value)
	{
		value_ = value;
	}

	/**
	 * Ctor.
	 * @param value Value.
	 */
	public NumericValue(Short value)
	{
		value_ = value;
	}

	/**
	 * Get <tt>double</tt> value.
	 * @return Double value.
	 */
	public double doubleValue()
	{
		return ((Number)value_).doubleValue();
	}
	
	private Object value_;
}
