package org.springframework.selector.parser;

import java.util.Map;
import java.util.HashMap;

/**
 * Class to represent a <tt>String</tt> literal. Immutable.
 * @author Jawaid Hakim.
 */
class LiteralString implements IExpressionString
{
    /**
     * Factory.
     * @param literal Literal.
     * @return Instance.
     */
    public static synchronized LiteralString valueOf(String literal)
    {
        LiteralString instance = (LiteralString) idMap_.get(literal);
        if (instance == null)
        {
            instance = new LiteralString(literal);
            idMap_.put(literal, instance);
        }
        return instance;
    }

    /**
     * Ctor.
     * @param literal String literal.
     */
    public LiteralString(String literal)
    {
        literal_ = literal;
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
        return literal_;
    }

    private final String literal_;
    private static Map idMap_ = new HashMap();
}
