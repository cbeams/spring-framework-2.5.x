package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.IExpressionNumeric;

import java.util.Map;

/**
 * Class to represent the <tt>BETWEEN</tt> operator. Immutable.
 * @author Jawaid Hakim.
 */
class OpBETWEEN implements IExpressionNumeric
{
    /**
     * Ctor.
     * @param val Test value.
     * @param lower Lower limit (inclusive).
     * @param upper Upper limit (inclusive).
     */
    public OpBETWEEN(IExpression val, IExpression lower, IExpression upper)
    {
        val_ = val;
        lower_ = lower;
        upper_ = upper;
    }

    public Object eval(final Map identifiers)
    {
        Object oVal = val_.eval(identifiers);
		Object oLower = lower_.eval(identifiers);
		Object oUpper = lower_.eval(identifiers);
		
		return eval(oVal, oLower, oUpper);
    }
    
    /**
     * Evaluate the expression.
     * @param provider Value provider. During the evaluation of the selector a callback
     * is made to the value provider to get identifier values.
     * @param corr Correlation data. This data is as-is passed to the provider.
     * @return Returns the result of the expression evaluation.
     */
    public Object eval(IValueProvider provider, Object corr)
    {
        Object oVal = val_.eval(provider, corr);
        Object oLower = lower_.eval(provider, corr);
        Object oUpper = lower_.eval(provider, corr);

		return eval(oVal, oLower, oUpper);
    }

	private Object eval(Object oVal, Object oLower, Object oUpper)
	{
        
		if (!(oVal instanceof NumericValue) || !(oLower instanceof NumericValue) || !(oUpper instanceof NumericValue))
			return Result.RESULT_UNKNOWN;

		double val = ((NumericValue) oVal).doubleValue();

		double lower = ((NumericValue) oLower).doubleValue();
		if (val < lower)
			return Result.RESULT_FALSE;
			
		double upper = ((NumericValue) oUpper).doubleValue();
		return (val <= upper) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
	}

    public String toString()
    {
        StringBuffer buf = new StringBuffer(val_.toString() + " BETWEEN ");
        buf.append(lower_.toString()).append(" AND ").append(upper_.toString());
        return buf.toString();
    }

    private final IExpression val_;
    private final IExpression lower_;
    private final IExpression upper_;
}
