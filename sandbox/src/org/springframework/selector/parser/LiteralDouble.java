package org.springframework.selector.parser;

import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.IExpressionNumeric;

import java.util.Map;
import java.util.HashMap;

/**
 * Class to represent a <tt>Double</tt> literal. Immutable.
 * @author Jawaid Hakim.
 */
class LiteralDouble implements IExpressionNumeric
{
    /**
     * Factory.
     * @param literal Literal.
     * @return Instance.
     */
    public static synchronized LiteralDouble valueOf(String literal)
    {
        LiteralDouble instance = (LiteralDouble) idMap_.get(literal);
        if (instance == null)
        {
            instance = new LiteralDouble(literal);
            idMap_.put(literal, instance);
        }
        return instance;
    }

    /**
     * Ctor.
     * @param literal Double literal.
     */
    private LiteralDouble(String literal)
    {
        literal_ = new NumericValue(Double.valueOf(literal));
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
