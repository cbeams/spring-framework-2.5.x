/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;

import org.springframework.core.NestedRuntimeException;

/**
 * Superclass for all mail exceptions.
 * @author Dmitriy Kopylenko
 * @version $Id: MailException.java,v 1.1 2003-09-10 00:14:54 dkopylenko Exp $
 */
public abstract class MailException extends NestedRuntimeException {

	public MailException(String msg) {
		super(msg);
	}

	public MailException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
