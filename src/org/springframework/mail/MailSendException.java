package org.springframework.mail;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Exception thrown when a mail sending error is encountered.
 * Can register failed messages with their exceptions.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class MailSendException extends MailException {

	private Map failedMessages;

	public MailSendException(String msg) {
		super(msg);
	}

	public MailSendException(String msg, Throwable ex) {
		super(msg, ex);
	}

	/**
	 * Constructor for registration of failed messages, with the
	 * messages that failed as keys, and the thrown exceptions as values.
	 * <p>The messages should be the same that were originally passed
	 * to the invoked send method.
	 */
	public MailSendException(Map failedMessages) {
		super(null);
		this.failedMessages = failedMessages;
	}

	/**
	 * Return a Map with the failed messages as keys, and the thrown
	 * exceptions as values.
	 * <p>The messages will be the same that were originally passed to the
	 * invoked send method, i.e. SimpleMailMessages in case of using the
	 * generic MailSender interface.
	 * <p>In case of sending MimeMessage instances via JavaMailSender,
	 * the messages will be of type MimeMessage.
	 */
	public Map getFailedMessages() {
		return failedMessages;
	}

	public String getMessage() {
		StringBuffer msg = new StringBuffer("Could not send mail: ");
		for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
			Exception subEx = (Exception) subExs.next();
			msg.append(subEx.getMessage());
			msg.append("; ");
		}
		return msg.toString();
	}

	public void printStackTrace(PrintStream ps) {
		if (this.failedMessages == null) {
			super.printStackTrace(ps);
		}
		else {
			for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
				Exception subEx = (Exception) subExs.next();
				subEx.printStackTrace(ps);
			}
		}
	}

	public void printStackTrace(PrintWriter pw) {
		if (this.failedMessages == null) {
			super.printStackTrace(pw);
		}
		else {
			for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
				Exception subEx = (Exception) subExs.next();
				subEx.printStackTrace(pw);
			}
		}
	}

}
