/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;

/**
 * This interface defines a strategy for sending simple mails. Can be
 * implemented for a variety of mailing systems due to the simple requirements.
 * For richer functionality like MIME messages, consider JavaMailSender.
 *
 * <p>Allows for easy testing of clients, as it does not depend on JavaMail's
 * infrastructure classes: no mocking of JavaMail Session or Transport necessary.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 10.09.2003
 * @see org.springframework.mail.javamail.JavaMailSender
 * @version $Id: MailSender.java,v 1.5 2004-03-17 17:16:10 jhoeller Exp $
 */
public interface MailSender {
	
	/**
	 * Send the given simple mail message.
	 * @param simpleMessage message to send
	 * @throws MailException in case of message, authentication or send errors
	 */
	void send(SimpleMailMessage simpleMessage) throws MailException;

	/**
	 * Send the given array of simple mail messages in batch.
	 * @param simpleMessages messages to send
	 * @throws MailException in case of message, authentication or send errors
	 */
	void send(SimpleMailMessage[] simpleMessages) throws MailException;

}
