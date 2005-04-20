/*
@license@
  */ 

package org.springframework.orm.toplink.exceptions;

import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * 
 * The <code>ToplinkObjectRetrievalFailureException</code> exception is a Toplink
 * specific object retrieval failure exception.
 * 
 * @author <a href="mailto:slavik@dbnet.co.il">Slavik Markovich</a>
 * @version $Revision: 1.1 $ $Date: 2005-04-20 23:21:23 $
 */
public class TopLinkObjectRetrievalFailureException extends ObjectRetrievalFailureException
{
	public TopLinkObjectRetrievalFailureException(Class persistantClass,
		Object identity, String message)
	{
		super(persistantClass, identity, message, null);
	}
}
