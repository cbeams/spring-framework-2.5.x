/*
 * Copyright 2002-2004 the original author or authors.
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

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;

/**
 * Extended MailSender interface for JavaMail, supporting MIME messages both as
 * direct arguments and via preparation callbacks.
 *
 * <p>Not as easy to test as a plain MailSender, but still rather easy compared
 * to full JavaMail code: Just let createMimeMessage return a plain MimeMessage
 * created with a Session.getInstance (empty properties) call, and check
 * the given messages in your mock implementations of the various send methods.
 *
 * <p>The recommended way of using this class is the MimeMessagePreparator
 * mechanism, possibly using a MimeMessageHelper for populating the message.
 * See MimeMessageHelper's javadoc for an example.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @see JavaMailSenderImpl
 * @see MimeMessagePreparator
 * @see MimeMessageHelper
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
	 * Send the given JavaMail MIME message.
	 * The message needs to have been created with createMimeMessage.
	 * @param mimeMessage message to send
	 * @throws MailException in case of message, authentication or send errors
	 * @see #createMimeMessage
	 */
	void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * Send the given array of JavaMail MIME messages in batch.
	 * The messages need to have been created with createMimeMessage.
	 * @param mimeMessages messages to send
	 * @throws MailException in case of message, authentication or send errors
	 * @see #createMimeMessage
	 */
	void send(MimeMessage[] mimeMessages) throws MailException;

	/**
	 * Send the JavaMail MIME message prepared by the given MimeMessagePreparator.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparator the preparator to use
	 * @throws MailException in case of message, authentication or send errors
	 */
	void send(MimeMessagePreparator mimeMessagePreparator) throws MailException;

	/**
	 * Send the JavaMail MIME messages prepared by the given MimeMessagePreparators.
	 * Alternative way to prepare MimeMessage instances, instead of createMimeMessage
	 * and send(MimeMessage[]) calls. Takes care of proper exception conversion.
	 * @param mimeMessagePreparators the preparator to use
	 * @throws MailException in case of message, authentication or send errors
	 */
	void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException;

}
