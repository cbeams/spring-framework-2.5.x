package org.springframework.selector.parser;

import org.springframework.selector.parser.BaseException;

/**
 * Exception thrown when a system error condition is trapped. This category of exceptions indicate system exceptions.
 * @see org.springframework.selector.parser.BusinessException
 * @author Jawaid Hakim.
 */
public class SystemException extends BaseException
{
    /**
     * Constructor.
     * @param desc Description of exception.
     */
    public SystemException(String desc)
    {
        super(desc);
    }

    /**
     * Constructor.
     * @param original Original throwable.
     */
    public SystemException(Throwable original)
    {
        super(original);
    }
}