/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.mail.javamail;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;

/**
 * Extended MailSender interface for JavaMail, supporting MIME messages both
 * as direct arguments and through preparation callbacks. Typically used in
 * conjunction with the MimeMessageHelper class for convenient creation of
 * JavaMail MimeMessages, including attachments etc.
 *
 * <p>Clients should talk to the mail sender through this interface if
 * they need mail functionality beyond SimpleMailMessage. The production
 * implementation is JavaMailSenderImpl. Clients will typically receive the
 * mail sender reference through dependency injection.
 *
 * <p>The recommended way of using this interface is the MimeMessagePreparator
 * mechanism, possibly using a MimeMessageHelper for populating the message.
 * See MimeMessageHelper's javadoc for an example.
 *
 * <p>Not as easy to test as a plain MailSender, but still rather easy compared to
 * traditional JavaMail code: Just let <code>createMimeMessage</code> return a plain
 * MimeMessage created with a <code>Session.getInstance(new Properties())</code>
 * call, and check the given messages in your mock implementations of the various
 * send methods.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @see JavaMailSenderImpl
 * @see #createMimeMessage
 * @see MimeMessagePreparator
 * @see MimeMessageHelper
 * @see org.springframework.mail.SimpleMailMessage
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
	MimeMessage createMimeMessage();

	/**
	 * Create a new JavaMail MimeMessage for the underlying JavaMail Session
	 * of this sender, using the given input stream as the message source.
	 * @param contentStream the raw MIME input stream for the message
	 * @return the new MimeMessage instance
	 * @throws org.springframework.mail.MailParseException
	 * in case of message creation failure
	*/
	MimeMessage createMimeMessage(InputStream contentStream) throws MailException;

	/**
	 * Send the given JavaMail MIME message.
	 * The message needs to have been created with createMimeMessage.
	 * @param mimeMessage message to send
	 * @throws org.springframework.mail.MailAuthenticationException
	 * in case of authentication failure
	 * @throws org.springframework.mail.MailSendException
	 * in case of failure when sending the message
	 * @see #createMimeMessage
	 */
	void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * Send the given array of JavaMail MIME messages in batch.
	 * The messages need to have been created with createMimeMessage.
	 * @param mimeMessages messages to send
	 * @throws org.springframework.mail.MailAuthenticationException
	 * in case of authentication failure
	 * @throws org.springframework.mail.MailSendException
	 * in case of failure when sending a message
	 * @see #createMimeMessage
	 */
	void send(MimeMessage[] mimeMessages) throws MailException;

	/**
	 * Send the JavaMail MIME message prepared by the given MimeMessagePreparator.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparator the preparator to use
	 * @throws org.springframework.mail.MailPreparationException
	 * in case of failure when preparing the message
	 * @throws org.springframework.mail.MailParseException
	 * in case of failure when parsing the message
	 * @throws org.springframework.mail.MailAuthenticationException
	 * in case of authentication failure
	 * @throws org.springframework.mail.MailSendException
	 * in case of failure when sending the message
	 */
	void send(MimeMessagePreparator mimeMessagePreparator) throws MailException;

	/**
	 * Send the JavaMail MIME messages prepared by the given MimeMessagePreparators.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage[]) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparators the preparator to use
	 * @throws org.springframework.mail.MailPreparationException
	 * in case of failure when preparing a message
	 * @throws org.springframework.mail.MailParseException
	 * in case of failure when parsing a message
	 * @throws org.springframework.mail.MailAuthenticationException
	 * in case of authentication failure
	 * @throws org.springframework.mail.MailSendException
	 * in case of failure when sending a message
	 */
	void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException;

}
