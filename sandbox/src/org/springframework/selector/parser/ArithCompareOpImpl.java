package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Abstract base class for arithmetic expression evaluation operators.
 * @author Jawaid Hakim.
 */
abstract class ArithCompareOpImpl
{
    /**
     * Ctor.
     * @param operator Operator.
     */
    private ArithCompareOpImpl(String operator)
    {
        operator_ = operator;
    }

    public String toString()
    {
        return operator_;
    }

    public abstract Object eval(final Map identifiers, IExpression lhs, IExpression rhs);

    public abstract Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs);

	/**
	 * = operator.
	 */
	public static final ArithCompareOpImpl EQ = new ArithCompareOpImpl(" = ")
	{
		public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
		{
			Object oLhs = lhs.eval(identifiers);
			if (oLhs == null)
				return Result.RESULT_UNKNOWN;
			else if (!(oLhs instanceof NumericValue))
				return Result.RESULT_FALSE;
			NumericValue nLhs = (NumericValue) oLhs;

			Object oRhs = rhs.eval(identifiers);
			if (oRhs == null)
				return Result.RESULT_UNKNOWN;
			else if (!(oRhs instanceof NumericValue))
				return Result.RESULT_FALSE;
			NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() == nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
		}

		public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
		{
			Object oLhs = lhs.eval(provider, corr);
			if (oLhs == null)
				return Result.RESULT_UNKNOWN;
			else if (!(oLhs instanceof NumericValue))
				return Result.RESULT_FALSE;
			NumericValue nLhs = (NumericValue) oLhs;

			Object oRhs = rhs.eval(provider, corr);
			if (oRhs == null)
				return Result.RESULT_UNKNOWN;
			else if (!(oRhs instanceof NumericValue))
				return Result.RESULT_FALSE;
			NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() == nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
		}
	};

    /**
     * > operator.
     */
    public static final ArithCompareOpImpl GT = new ArithCompareOpImpl(" > ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() > nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() > nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }
    };

    /**
     * >= operator.
     */
    public static final ArithCompareOpImpl GE = new ArithCompareOpImpl(" >= ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() >= nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() >= nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }
    };

    /**
     * < operator.
     */
    public static final ArithCompareOpImpl LT = new ArithCompareOpImpl(" < ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() < nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() < nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }
    };

    /**
     * <= operator.
     */
    public static final ArithCompareOpImpl LE = new ArithCompareOpImpl(" <= ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() <= nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (oLhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oLhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nLhs = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (oRhs == null)
                return Result.RESULT_UNKNOWN;
            else if (!(oRhs instanceof NumericValue))
                return Result.RESULT_FALSE;
            NumericValue nRhs = (NumericValue) oRhs;

			return (nLhs.doubleValue() <= nRhs.doubleValue()) ? Result.RESULT_TRUE : Result.RESULT_FALSE;
        }
    };

    private final String operator_;
}
