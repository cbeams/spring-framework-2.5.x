package org.springframework.selector.parser;

import java.util.Map;

/**
 * Class to implement arithmetic operators.
 * @author Jawaid Hakim.
 */
class ArithOp implements IExpression
{
    /**
     * Ctor.
     * @param op Arithmetic operator.
     * @param lhs LHS.
     * @param rhs RHS.
     */
    public ArithOp(ArithOpImpl op, IExpression lhs, IExpression rhs)
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
    private final ArithOpImpl op_;
}
