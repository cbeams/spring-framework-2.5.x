/*
@license@
  */ 
package org.springframework.orm.toplink.exceptions;

import org.springframework.dao.TypeMismatchDataAccessException;

/**
 * @author jclark	
 *
 */
public class TopLinkTypeMismatchDataAccessException extends
        TypeMismatchDataAccessException
{
   
    public TopLinkTypeMismatchDataAccessException(String msg)
    {
        super("TopLinkConversionManager:  "+msg);
    }

    public TopLinkTypeMismatchDataAccessException(String msg, Throwable ex)
    {
        super("TopLinkConversionManager:   "+msg, ex);
    }
}
