package org.springframework.selector.parser;

/**
 * Class to represent typesafe enumeration of evaluating a selector or expression. The
 * enumerations are <tt>TRUE</tt>, <tt>FALSE</tt>. and <tt>UNKNOWN</tt>.
 * @author Jawaid Hakim.
 */
public class Result
{
    /**
     * Ctor.
     * @param result Result.
     */
    private Result(String result)
    {
        result_ = result;
    }

    /**
     * Override.
     * @return Value.
     */
    public String toString()
    {
        return result_;
    }

    private final String result_;

    /**
     * TRUE.
     */
    public static final Result RESULT_TRUE = new Result("true");

    /**
     * FALSE.
     */
    public static final Result RESULT_FALSE = new Result("false");

    /**
     * UNKNOWN.
     */
    public static final Result RESULT_UNKNOWN = new Result("unknown");
}