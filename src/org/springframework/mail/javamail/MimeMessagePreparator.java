package org.springframework.mail.javamail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Callback interface for preparation of JavaMail MIME messages.
 *
 * <p>The respective send methods of JavaMailSender will take care of the
 * actual creation of a MimeMessage instance, and of proper exception conversion.
 *
 * @author Juergen Hoeller
 * @see JavaMailSender#send(MimeMessagePreparator)
 * @see JavaMailSender#send(MimeMessagePreparator[])
 */
public interface MimeMessagePreparator {

	/**
	 * Prepare the given new MimeMessage instance.
	 * @param mimeMessage the message to prepare
	 * @throws MessagingException passing any exceptions thrown by MimeMessage
	 * methods through for automatic conversion to the MailException hierarchy
	 */
	void prepare(MimeMessage mimeMessage) throws MessagingException;

}
