package org.springframework.selector.parser;

import java.util.Map;

/**
 * Interface implemented by expression implementations.
 * @author Jawaid Hakim.
 */
public interface IExpression
{
    /**
     * Evaluate the expression.
     * @param identifiers Identifier values.
     * @return Result of the expression evaluation.
     */
    public Object eval(final Map identifiers);

	/**
	 * Evaluate the expression.
	 * @param provider Value provider. During evaluation of the expression callbacks
	 * are made on the value provider to get identifier values.
	 * @param corr Correlation data. Passed as-is to the value provider.
	 * @return Result evaluating the expression.
	 */
    public Object eval(IValueProvider provider, Object corr);
}
