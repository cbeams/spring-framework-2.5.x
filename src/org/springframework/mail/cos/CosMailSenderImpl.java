package org.springframework.mail.cos;

import java.io.IOException;
import java.io.PrintStream;

import com.oreilly.servlet.MailMessage;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

/**
 * Simple implementation of SMPT mail sending on top of Jason Hunter's
 * MailMessage class that's included in COS (com.oreilly.servlet).
 *
 * <p>Does not support any richer functionality than MailSender and
 * SimpleMailMessage, therefore there's no optional richer interface like
 * the JavaMailSender interface for the JavaMailSenderImpl implementation.
 *
 * @author Juergen Hoeller
 * @since 09.10.2003
 * @see com.oreilly.servlet.MailMessage
 * @see org.springframework.mail.javamail.JavaMailSenderImpl
 */
public class CosMailSenderImpl implements MailSender {

	private String host;

	/**
	 * Set the SMTP mail host.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	public void send(SimpleMailMessage simpleMessage) throws MailException {
		try {
			MailMessage cosMessage = new MailMessage(this.host);
			cosMessage.from(simpleMessage.getFrom());
			cosMessage.to(simpleMessage.getTo());
			if (simpleMessage.getCc() != null) {
				for (int i = 0; i < simpleMessage.getCc().length; i++) {
					cosMessage.cc(simpleMessage.getCc()[i]);
				}
			}
			if (simpleMessage.getBcc() != null) {
				for (int i = 0; i < simpleMessage.getBcc().length; i++) {
					cosMessage.bcc(simpleMessage.getBcc()[i]);
				}
			}
			cosMessage.setSubject(simpleMessage.getSubject());
			PrintStream textStream = cosMessage.getPrintStream();
			textStream.print(simpleMessage.getText());
			cosMessage.sendAndClose();
		}
		catch (IOException ex) {
			throw new MailSendException(ex);
		}
	}

	public void send(SimpleMailMessage[] simpleMessages) throws MailException {
		for (int i = 0; i < simpleMessages.length; i++) {
			send(simpleMessages[i]);
		}
	}
}
