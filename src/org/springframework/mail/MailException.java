/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;

import org.springframework.core.NestedCheckedException;

/**
 * Base class for all mail exceptions.
 * @author Dmitriy Kopylenko
 * @version $Id: MailException.java,v 1.3 2003-11-06 13:22:07 dkopylenko Exp $
 */
public abstract class MailException extends NestedCheckedException {

	public MailException(String msg) {
		super(msg);
	}

	public MailException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
