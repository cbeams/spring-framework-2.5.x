package org.springframework.mail;

/**
 * Exception thrown when a mail sending error is encountered.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class MailSendException extends MailException {

	public MailSendException(String msg) {
		super(msg);
	}

	public MailSendException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public MailSendException(Throwable ex) {
		super("Send failed: " + ex.getMessage(), ex);
	}

}
