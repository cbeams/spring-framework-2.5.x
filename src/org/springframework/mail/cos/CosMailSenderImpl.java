package org.springframework.mail.cos;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;

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
		send(new SimpleMailMessage[] {simpleMessage});
	}

	public void send(SimpleMailMessage[] simpleMessages) throws MailException {
		Map failedMessages = new HashMap();
		for (int i = 0; i < simpleMessages.length; i++) {
			try {
				MailMessage cosMessage = new MailMessage(this.host);
				cosMessage.from(simpleMessages[i].getFrom());
				cosMessage.to(simpleMessages[i].getTo());
				if (simpleMessages[i].getCc() != null) {
					for (int j = 0; j < simpleMessages[j].getCc().length; j++) {
						cosMessage.cc(simpleMessages[j].getCc()[j]);
					}
				}
				if (simpleMessages[i].getBcc() != null) {
					for (int j = 0; j < simpleMessages[j].getBcc().length; j++) {
						cosMessage.bcc(simpleMessages[j].getBcc()[j]);
					}
				}
				cosMessage.setSubject(simpleMessages[i].getSubject());
				PrintStream textStream = cosMessage.getPrintStream();
				textStream.print(simpleMessages[i].getText());
				cosMessage.sendAndClose();
			}
			catch (IOException ex) {
				failedMessages.put(simpleMessages[i], ex);
			}
		}
		if (!failedMessages.isEmpty()) {
			throw new MailSendException(failedMessages);
		}
	}

}
