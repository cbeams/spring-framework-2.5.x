package org.springframework.selector.parser;

import org.springframework.selector.parser.BusinessException;

/**
 * Exception thrown selector is invalid.
 * @see com.codestreet.parser.BusinessException
 * @author Jawaid Hakim.
 */
public class InvalidSelectorException extends BusinessException
{
    /**
     * Ctor.
     * @param desc Description of exception.
     */
    public InvalidSelectorException(String desc)
    {
        super(desc);
    }

    /**
     * Ctor.
     * @param root Root throwable.
     */
    public InvalidSelectorException(Throwable root)
    {
        super(root);
    }
}