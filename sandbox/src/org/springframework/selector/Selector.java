package org.springframework.selector;

import org.springframework.selector.parser.*;
import org.springframework.selector.parser.SelectorParser.SelectorParseState;

import java.util.Map;

/**
 * Thread safe Selector implementation.
 * @author Jawaid Hakim.
 */
public class Selector implements ISelector
{
    /**
     * Factory.
     * @param selector Selector.
     * @return  Selector instance.
     * @throws NullPointerException
     * @see #getInstance(String, boolean)
     */
    public static Selector getInstance(String selector) throws InvalidSelectorException
    {
        return getInstance(selector, false);
    }

    /**
     * Factory.
     * @param selector Selector.
     * @param trace Parser outputs trace if <tt>true</tt> .
     * @return Selector instance.
     * @throws NullPointerException
     * @throws org.springframework.selector.parser.InvalidSelectorException
     * @see #getInstance(String)
     */
    public static Selector getInstance(String selector, boolean trace) throws InvalidSelectorException
    {
        if (selector == null)
            throw new NullPointerException("NULL selector");

        SelectorParseState exp = SelectorParser.doParse(selector, trace);
        return new Selector(selector, exp.getRoot(), exp.getIdentifiers());
    }

    /**
     * Ctor.
     * @param selector Selector.
     * @param root Root expression of the parsed selector.
     * @param identifiers Identifiers used by the selector. The key
     * into the <tt>Map</tt> is name of the identifier and the value is an
     * instance of <tt>Identifier</tt>.
     */
    private Selector(String selector, IExpression root, Map identifiers)
    {
        selector_ = selector;
        root_ = root;
        identifiers_ = java.util.Collections.unmodifiableMap(identifiers);
    }

    /**
     * Evaluate the selector.
     * @param identifiers Value for each non-null identifier in the selector.
     * @return Result of evaluating the selector.
     * @see #getIdentifiers()
     */
    public Result eval(Map identifiers)
    {
        return (Result) root_.eval(identifiers);
    }

    /**
     * Evaluate the selector.
     * @param provider Value provider. During evaluation of the selector callbacks
     * are made on the value provider to get identifier values.
     * @param corr Correlation data. Passed as-is to the value provider.
     * @return Result of evaluating the selector.
     */
    public Result eval(IValueProvider provider, Object corr)
    {
        return (Result) root_.eval(provider, corr);
    }

    /**
     * Get identifiers used by the selector. The key into the <tt>Map</tt> is 
     * the name of the identifier and the value is an instance of <tt>Identifier</tt>.
     * @return Readonly <tt>Map</tt> of identifiers that are used within the selector.
     * @throws UnsupportedOperationException
     * @see #eval(Map)
     */
    public Map getIdentifiers()
    {
        return identifiers_;
    }

    /**
     * Get the selector.
     * @return Selector.
     */
    public String getSelector()
    {
        return selector_;
    }

    /**
     * Get selector parse tree.
     * @return Selector parse tree.
     */
    public String toString()
    {
        return selector_;
    }

    private final IExpression root_;
    private final Map identifiers_;
    private final String selector_;
}
