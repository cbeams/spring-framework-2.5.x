package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Class to represent the <tt>String</tt> equality operator. Immutable.
 * @author Jawaid Hakim.
 */
class OpStringEQ implements IExpressionString
{
    /**
     * Ctor.
     * @param lhs LHS.
     * @param rhs RHS.
     */
    public OpStringEQ(IExpression lhs, IExpression rhs)
    {
        lhs_ = lhs;
        rhs_ = rhs;
    }

    public Object eval(final Map identifiers)
    {
        Object oLhs = lhs_.eval(identifiers);
		Object oRhs = rhs_.eval(identifiers);

		return eval(oLhs, oRhs);
    }
    
    public Object eval(IValueProvider provider, Object corr)
    {
		Object oLhs = lhs_.eval(provider, corr);
		Object oRhs = rhs_.eval(provider, corr);

		return eval(oLhs, oRhs);
    }

	private Object eval(Object oLhs, Object oRhs)
	{
		if (!(oLhs instanceof String))
			return Result.RESULT_UNKNOWN;

		if (!(oRhs instanceof String))
			return Result.RESULT_UNKNOWN;

		return (oLhs.equals(oRhs)) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
	}

    private final IExpression lhs_;
    private final IExpression rhs_;


}
