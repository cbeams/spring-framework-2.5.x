/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for all mail exceptions.
 * @author Dmitriy Kopylenko
 * @version $Id: MailException.java,v 1.4 2004-02-02 12:01:06 jhoeller Exp $
 */
public abstract class MailException extends NestedRuntimeException {

	public MailException(String msg) {
		super(msg);
	}

	public MailException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
