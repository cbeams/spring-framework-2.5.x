package org.springframework.mail;

/**
 * Exception thrown on failed authentication.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @version $Id: MailAuthenticationException.java,v 1.2 2003-11-06 13:22:07 dkopylenko Exp $
 */
public class MailAuthenticationException extends MailException {

	public MailAuthenticationException(String msg) {
		super(msg);
	}

	public MailAuthenticationException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public MailAuthenticationException(Throwable ex) {
		super("Authentication failed: " + ex.getMessage(), ex);
	}

}
