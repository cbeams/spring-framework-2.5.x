package org.springframework.mail;

/**
 * Exception thrown on failed authentication.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
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
