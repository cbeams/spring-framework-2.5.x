package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Typesafe enumeration of valid conditional operators.
 * @author Jawaid Hakim.
 */
abstract class ConditionalOpImpl
{
    /**
     * Ctor.
     * @param operator Operator.
     */
    private ConditionalOpImpl(String operator)
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
     * AND operator.
     */
    public static final ConditionalOpImpl AND = new ConditionalOpImpl(" AND ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Result bLhs = (Result) lhs.eval(identifiers);
            // Short circuit evaulation if LHS is FALSE or UNKNOWN
            if (bLhs == Result.RESULT_FALSE)
                return Result.RESULT_FALSE;
            else if (bLhs == Result.RESULT_UNKNOWN)
                return Result.RESULT_UNKNOWN;

            return (Result) rhs.eval(identifiers);
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Result bLhs = (Result) lhs.eval(provider, corr);
            // Short circuit evaulation if LHS is FALSE or UNKNOWN
            if (bLhs == Result.RESULT_FALSE)
                return Result.RESULT_FALSE;
            else if (bLhs == Result.RESULT_UNKNOWN)
                return Result.RESULT_UNKNOWN;

            return (Result) rhs.eval(provider, corr);
        }
    };

    /**
     * OR operator.
     */
    public static final ConditionalOpImpl OR = new ConditionalOpImpl(" OR ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Result bLhs = (Result) lhs.eval(identifiers);
            // Short circuit evaulation if LHS is TRUE
            if (bLhs == Result.RESULT_TRUE)
                return Result.RESULT_TRUE;

            Result bRhs = (Result) rhs.eval(identifiers);
            if (bRhs == Result.RESULT_TRUE)
                return Result.RESULT_TRUE;
            else if (bLhs == Result.RESULT_UNKNOWN || bRhs == Result.RESULT_UNKNOWN)
                return Result.RESULT_UNKNOWN;
            else
                return Result.RESULT_FALSE;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Result bLhs = (Result) lhs.eval(provider, corr);
            // Short circuit evaulation if LHS is TRUE
            if (bLhs == Result.RESULT_TRUE)
                return Result.RESULT_TRUE;

            Result bRhs = (Result) rhs.eval(provider, corr);
            if (bRhs == Result.RESULT_TRUE)
                return Result.RESULT_TRUE;
            else if (bLhs == Result.RESULT_UNKNOWN || bRhs == Result.RESULT_UNKNOWN)
                return Result.RESULT_UNKNOWN;
            else
                return Result.RESULT_FALSE;
        }
    };

    /**
     * NOT operator.
     */
    public static final ConditionalOpImpl NOT = new ConditionalOpImpl(" NOT ")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Result bRhs = (Result) rhs.eval(identifiers);
            if (bRhs == Result.RESULT_TRUE)
                return Result.RESULT_FALSE;
            else if (bRhs == Result.RESULT_FALSE)
                return Result.RESULT_TRUE;
            else
                return Result.RESULT_UNKNOWN;
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Result bRhs = (Result) rhs.eval(provider, corr);
            if (bRhs == Result.RESULT_TRUE)
                return Result.RESULT_FALSE;
            else if (bRhs == Result.RESULT_FALSE)
                return Result.RESULT_TRUE;
            else
                return Result.RESULT_UNKNOWN;
        }
    };

    private final String operator_;
}
