package org.springframework.selector.parser;

import java.util.Map;
import java.util.HashMap;

/**
 * Class to represent <tt>immutable</tt> <tt>Bool</tt> literals.
 * @author Jawaid Hakim.
 */
class LiteralBool implements IExpressionBool
{
    /**
     * Factory.
     * @param literal Literal. Must be either <tt>true</tt>, or <tt>TRUE</tt>, 
     * or <tt>True</tt>, or <tt>false</tt>, or <tt>FALSE</tt>, or <tt<False</tt>.
     * @return Instance.
     * @throws java.lang.IllegalArgumentException Invalid literal.
     */
    public static synchronized LiteralBool valueOf(String literal)
    {
        LiteralBool ret = (LiteralBool) idMap_.get(literal);
        if (ret == null)
            throw new IllegalArgumentException("Invalid literal: " + literal);
        return ret;
    }

    /**
     * Ctor.
     * @param literal Literal.
     */
    private LiteralBool(String literal)
    {
        literal_ = Boolean.valueOf(literal);
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

    private final Boolean literal_;
    private static Map idMap_ = new HashMap();

    static
    {
        LiteralBool literal = new LiteralBool("true");
        idMap_.put("true", literal);
		idMap_.put("TRUE", literal);
		idMap_.put("True", literal);

        literal = new LiteralBool("false");
        idMap_.put("false", literal);
		idMap_.put("FALSE", literal);
		idMap_.put("False", literal);
    }
}