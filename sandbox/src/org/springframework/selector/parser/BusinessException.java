package org.springframework.selector.parser;

import org.springframework.selector.parser.BaseException;

/**
 * Exception thrown when a normal usage error condition is trapped.
 * @see org.springframework.selector.parser.SystemException
 * @author Jawaid Hakim.
 */
public class BusinessException extends BaseException
{
    /**
     * Ctor.
     * @param desc Description of exception.
     */
    public BusinessException(String desc)
    {
        super(desc);
    }

    /**
     * Ctor.
     * @param original Original throwable.
     */
    public BusinessException(Throwable original)
    {
        super(original);
    }
}