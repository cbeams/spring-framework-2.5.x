/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail.javamail;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailSender;
import org.springframework.mail.MailException;

/**
 * Extended MailSender interface for JavaMail, supporting MIME messages both as
 * direct arguments and via preparation callbacks.
 *
 * <p>Not as easy to test as a plain MailSender, but still rather easy compared
 * to full JavaMail code: Just let createMimeMessage return a plain Session
 * created with a Session.getInstance (empty properties) call, and check
 * the given messages in your mock implementations of the various send methods.
 *
 * @author Juergen Hoeller
 * @see JavaMailSenderImpl
 */
public interface JavaMailSender extends MailSender {

	/**
	 * Create a new JavaMail MimeMessage for the underlying JavaMail Session
	 * of this sender. Needs to be called to create MimeMessage instances
	 * that can be prepared by the client and passed to send(MimeMessage).
	 * @return the new MimeMessage instance
	 * @see #send(MimeMessage)
	 * @see #send(MimeMessage[])
	 */
	public MimeMessage createMimeMessage();

	/**
	 * Send the given JavaMail MIME message.
	 * The message needs to have been created with createMimeMessage.
	 * @param mimeMessage message to send
	 * @throws MailException in case of message, authentication, or send errors
	 * @see #createMimeMessage
	 */
	public void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * Send the given array of JavaMail MIME messages in batch.
	 * The messages need to have been created with createMimeMessage.
	 * @param mimeMessages messages to send
	 * @throws MailException in case of message, authentication, or send errors
	 * @see #createMimeMessage
	 */
	public void send(MimeMessage[] mimeMessages) throws MailException;

	/**
	 * Send the JavaMail MIME message prepared by the given MimeMessagePreparator.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparator the preparator to use
	 * @throws MailException in case of message, authentication, or send errors
	 */
	public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException;

	/**
	 * Send the JavaMail MIME messages prepared by the given MimeMessagePreparators.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage[]) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparators the preparator to use
	 * @throws MailException in case of message, authentication, or send errors
	 */
	public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException;

}
