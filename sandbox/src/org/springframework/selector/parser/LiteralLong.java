package org.springframework.selector.parser;

import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.IExpressionNumeric;

import java.util.Map;
import java.util.HashMap;

/**
 * Class to represent a <tt>Long</tt> literal. Immutable.
 * @author Jawaid Hakim.
 */
class LiteralLong implements IExpressionNumeric

{
    /**
     * Factory.
     * @param literal Literal.
     * @return Instance.
     */
    public static synchronized LiteralLong valueOf(String literal)
    {
        LiteralLong instance = (LiteralLong) idMap_.get(literal);
        if (instance == null)
        {
            instance = new LiteralLong(literal);
            idMap_.put(literal, instance);
        }
        return instance;
    }

    /**
     * Ctor.
     * @param literal Long literal.
     */
    public LiteralLong(String literal)
    {
        literal_ = new NumericValue(Long.valueOf(literal));
    }

    public Object eval(final Map identifiers)
    {
        return literal_;
    }

    public Object eval(IValueProvider provider, Object corr)
    {
        return literal_;
    }

    public String toString()
    {
        return literal_.toString();
    }

    private final NumericValue literal_;
    private static Map idMap_ = new HashMap();
}
