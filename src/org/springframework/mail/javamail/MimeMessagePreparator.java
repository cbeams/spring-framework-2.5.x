package org.springframework.mail.javamail;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Callback interface for preparation of JavaMail MIME messages.
 *
 * <p>The corresponding send methods of JavaMailSender will take care
 * of the actual creation of a MimeMessage instance, and of proper
 * exception conversion.
 *
 * <p>It is often convenient to use a MimeMessageHelper for populating
 * the passed-in MimeMessage, particularly when working with attachments
 * or special character sets.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @version $Id: MimeMessagePreparator.java,v 1.3 2004-03-17 17:16:47 jhoeller Exp $
 * @see JavaMailSender#send(MimeMessagePreparator)
 * @see JavaMailSender#send(MimeMessagePreparator[])
 * @see MimeMessageHelper
 */
public interface MimeMessagePreparator {

	/**
	 * Prepare the given new MimeMessage instance.
	 * @param mimeMessage the message to prepare
	 * @throws MessagingException passing any exceptions thrown by MimeMessage
	 * methods through for automatic conversion to the MailException hierarchy
	 * @throws IOException passing any exceptions thrown by MimeMessage methods
	 * through for automatic conversion to the MailException hierarchy
	 */
	void prepare(MimeMessage mimeMessage) throws MessagingException, IOException;

}
