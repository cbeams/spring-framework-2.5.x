package org.springframework.mail;

/**
 * Exception thrown if illegal message properties are encountered.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @version $Id: MailParseException.java,v 1.2 2003-11-06 13:22:07 dkopylenko Exp $
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
