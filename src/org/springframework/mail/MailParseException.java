package org.springframework.mail;

/**
 * Exception thrown if illegal message properties are encountered.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class MailParseException extends MailException {

	public MailParseException(String msg) {
		super(msg);
	}

	public MailParseException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public MailParseException(Throwable ex) {
		super("Could not parse mail: " + ex.getMessage(), ex);
	}

}
