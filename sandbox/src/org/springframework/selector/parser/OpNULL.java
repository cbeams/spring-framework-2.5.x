package org.springframework.selector.parser;

import java.util.Map;

/**
 * Class to represent the <tt>null</tt> operator. This operator checks whether
 * the value of an identifier is <tt>null</tt>. Immutable.
 * @author Jawaids Hakim.
 */
class OpNULL implements IExpression
{
    /**
     * Ctor.
     * @param id Identifer.
     */
    public OpNULL(IExpression id)
    {
        id_ = id;
    }

    public Object eval(final Map identifiers)
    {
		return (id_.eval(identifiers) == null) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
    }

    public Object eval(IValueProvider provider, Object corr)
    {
        return (id_.eval(provider, corr) == null) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
    }

    public String toString()
    {
        return id_.toString() + " IS NULL";
    }

    private final IExpression id_;
}
