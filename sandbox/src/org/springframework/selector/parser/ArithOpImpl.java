package org.springframework.selector.parser;

import org.springframework.selector.parser.IExpression;
import org.springframework.selector.parser.IValueProvider;

import java.util.Map;

/**
 * Typesafe enumeration of valid arithmetic operators.
 * @author Jawaid Hakim.
 */
abstract class ArithOpImpl
{
    /**
     * Ctor.
     * @param operator Operator.
     */
    private ArithOpImpl(String operator)
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
     * + operator.
     */
    public static final ArithOpImpl PLUS = new ArithOpImpl("+")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (!(oLhs instanceof Number))
                return Result.RESULT_UNKNOWN;
            Number lhsVal = (Number) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (!(oRhs instanceof Number))
                return Result.RESULT_UNKNOWN;
            Number rhsVal = (Number) oRhs;

            return new Double(lhsVal.doubleValue() + rhsVal.doubleValue());
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() + rhsVal.doubleValue());
        }
    };

    /**
     * - operator.
     */
    public static final ArithOpImpl MINUS = new ArithOpImpl("-")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() - rhsVal.doubleValue());
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() - rhsVal.doubleValue());
        }
    };

    /**
     * * operator.
     */
    public static final ArithOpImpl MULT = new ArithOpImpl("*")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() * rhsVal.doubleValue());
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() * rhsVal.doubleValue());
        }
    };

    /**
     * / operator.
     */
    public static final ArithOpImpl DIV = new ArithOpImpl("/")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(identifiers);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(identifiers);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() / rhsVal.doubleValue());
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oLhs = lhs.eval(provider, corr);
            if (!(oLhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue lhsVal = (NumericValue) oLhs;

            Object oRhs = rhs.eval(provider, corr);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(lhsVal.doubleValue() / rhsVal.doubleValue());
        }
    };

    /**
     * unary - operator.
     */
    public static final ArithOpImpl NEG = new ArithOpImpl("-")
    {
        public Object eval(final Map identifiers, IExpression lhs, IExpression rhs)
        {
            Object oRhs = rhs.eval(identifiers);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(-rhsVal.doubleValue());
        }

        public Object eval(IValueProvider provider, Object corr, IExpression lhs, IExpression rhs)
        {
            Object oRhs = rhs.eval(provider, corr);
            if (!(oRhs instanceof NumericValue))
                return Result.RESULT_UNKNOWN;
            NumericValue rhsVal = (NumericValue) oRhs;

            return new Double(-rhsVal.doubleValue());
        }
    };

    private final String operator_;
}
