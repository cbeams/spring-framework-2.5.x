package org.springframework.selector.parser;

import java.util.*;

/**
 * Class to represent <tt>set</tt> membership operator. Immutable.
 * @author Jawaid Hakim.
 */
class OpIN implements IExpressionString
{
    /**
     * Ctor.
     * @param lhs Data to check for.
     * @param rhs Membership set.
     */
    public OpIN(IExpression lhs, final Set rhs)
    {
        lhs_ = lhs;
        rhs_ = rhs;
    }

    public Object eval(final Map identifiers)
    {
        Object oLhs = lhs_.eval(identifiers);
        if (!(oLhs instanceof String))
            return Result.RESULT_UNKNOWN;

        return (rhs_.contains(oLhs)) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
    }

    public Object eval(IValueProvider provider, Object corr)
    {
        Object oLhs = lhs_.eval(provider, corr);
        if (!(oLhs instanceof String))
            return Result.RESULT_UNKNOWN;

		return (rhs_.contains(oLhs)) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer(lhs_.toString());
        buf.append(" IN (");
        boolean first = true;
        for (Iterator iter = rhs_.iterator(); iter.hasNext();)
        {
            if (!first)
                buf.append(',');
            buf.append('\'').append(iter.next()).append('\'');
            first = false;
        }
        buf.append(')');
        return buf.toString();
    }

    private final IExpression lhs_;
    private final Set rhs_;
}
