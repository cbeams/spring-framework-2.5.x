package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.IExpressionBool;

import java.util.Map;

/**
 * Class to represent the <tt>bool</tt> equality operator. Immutable.
 * @author Jawaid Hakim.
 */
class OpBoolEQ implements IExpressionBool
{
    /**
     * Ctor.
     * @param lhs LHS of the equality.
     * @param rhs RHS of the equality.
     */
    public OpBoolEQ(IExpression lhs, IExpression rhs)
    {
        lhs_ = lhs;
        rhs_ = rhs;
    }

    public Object eval(final Map identifiers)
    {
        Object oLhs = lhs_.eval(identifiers);
        if (!(oLhs instanceof Boolean))
            return Result.RESULT_UNKNOWN;
        boolean lhs = ((Boolean) oLhs).booleanValue();

        Object oRhs = rhs_.eval(identifiers);
        if (!(oRhs instanceof Boolean))
            return Result.RESULT_UNKNOWN;
        boolean rhs = ((Boolean) oRhs).booleanValue();

        if (lhs == rhs)
            return Result.RESULT_TRUE;
        else
            return Result.RESULT_FALSE;
    }

    public Object eval(IValueProvider provider, Object corr)
    {
        Object oLhs = lhs_.eval(provider, corr);
        if (!(oLhs instanceof Boolean))
            return Result.RESULT_UNKNOWN;
        boolean lhs = ((Boolean) oLhs).booleanValue();

        Object oRhs = rhs_.eval(provider, corr);
        if (!(oRhs instanceof Boolean))
            return Result.RESULT_UNKNOWN;
        boolean rhs = ((Boolean) oRhs).booleanValue();

        if (lhs == rhs)
            return Result.RESULT_TRUE;
        else
            return Result.RESULT_FALSE;
    }

    public String toString()
    {
        return lhs_.toString() + " = " + rhs_.toString();
    }

    private final IExpression lhs_;
    private final IExpression rhs_;


}
