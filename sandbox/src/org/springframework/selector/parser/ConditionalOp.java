package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Class to implement conditional operators.
 * @author Jawaid Hakim.
 */
class ConditionalOp implements IExpression
{
    /**
     * Ctor.
     * @param op Conditional operator.
     * @param lhs LHS.
     * @param rhs RHS.
     */
    public ConditionalOp(ConditionalOpImpl op, IExpression lhs, IExpression rhs)
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
    private final ConditionalOpImpl op_;
}
