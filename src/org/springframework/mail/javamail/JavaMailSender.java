/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail.javamail;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailSender;
import org.springframework.mail.MailException;

/**
 * Extended MailSender interface for JavaMail, supporting MIME messages.
 *
 * <p>Not as easy to test as a plain MailSender, but still rather easy
 * compared to full JavaMail code: Just implement createMimeMessage
 * with a plain Session.getInstance(new Properties()) call, and
 * check the given messages in your mock send implementations.
 *
 * @author Juergen Hoeller
 * @see JavaMailSenderImpl
 */
public interface JavaMailSender extends MailSender {

	/**
	 * Send the given JavaMail MIME message.
	 * @param mimeMessage message to send
	 * @throws MailException in case of system errors
	 */
	public void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * Send the given array of JavaMail MIME messages in batch.
	 * @param mimeMessages messages to send
	 * @throws MailException in case of system errors
	 */
	public void send(MimeMessage[] mimeMessages) throws MailException;

	/**
	 * Create a new JavaMail MimeMessage, assumably for the
	 * underlying JavaMail Session of this sender.
	 * @return the new MimeMessage instance
	 */
	public MimeMessage createMimeMessage();

}
