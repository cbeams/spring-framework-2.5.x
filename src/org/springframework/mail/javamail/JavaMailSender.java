/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail.javamail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.MailSettings;
import org.springframework.mail.MailSystemException;

/**
 * MailSender implementation for JavaMail API
 * @author Dmitriy Kopylenko
 * @version $Id: JavaMailSender.java,v 1.2 2003-09-15 13:04:00 dkopylenko Exp $
 */
public class JavaMailSender implements MailSender {

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Send electronic mail using JavaMail API
	 * @see org.springframework.mail.MailSender#send(org.springframework.mail.support.MailSettings)
	 */
	public void send(MailSettings mailSettings) throws MailException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailSettings.getMailHost());
		Session session = Session.getInstance(props, null);

		MimeMessage message = new MimeMessage(session);
		try {
			if (logger.isDebugEnabled())
				logger.debug("Sending email using the following mail properties [" + mailSettings + "]");

			message.setSubject(mailSettings.getMailSubject());
			message.setText(mailSettings.getMailText());
			message.setFrom(new InternetAddress(mailSettings.getMailFrom()));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailSettings.getMailTo()));
			if (mailSettings.getMailCc() != null) {
				for (int i = 0; i < mailSettings.getMailCc().length; i++)
					message.addRecipient(Message.RecipientType.CC, new InternetAddress(mailSettings.getMailCc()[i]));
			}

			Transport.send(message);
		}
		catch (Throwable t) {
			throw new MailSystemException("Encountered Mail System Exception", t);
		}
	}
}
