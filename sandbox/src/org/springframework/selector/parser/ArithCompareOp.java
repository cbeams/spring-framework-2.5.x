package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Class to implement arithmetic comparison operators.
 * @author Jawaid Hakim.
 */
class ArithCompareOp implements IExpressionNumeric
{
    /**
     * Ctor.
     * @param op Arithmetic comparison operator.
     * @param lhs LHS.
     * @param rhs RHS.
     */
    public ArithCompareOp(ArithCompareOpImpl op, IExpression lhs, IExpression rhs)
    {
        op_ = op;
        lhs_ = lhs;
        rhs_ = rhs;
    }

    public Object eval(final Map identifiers)
    {
        return op_.eval(identifiers, lhs_, rhs_);
    }

    public Object eval(IValueProvider provider, Object corr)
    {
        return op_.eval(provider, corr, lhs_, rhs_);
    }

    public String toString()
    {
        return lhs_.toString() + op_.toString() + rhs_.toString();
    }

    private final IExpression lhs_;
    private final IExpression rhs_;
    private final ArithCompareOpImpl op_;
}
