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

package org.springframework.mail.cos;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.oreilly.servlet.MailMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * Simple implementation of SMTP mail sending on top of Jason Hunter's
 * MailMessage class that's included in
 * <a href="http://servlets.com/cos">COS (com.oreilly.servlet)</a>.
 *
 * <p>Does not support any richer functionality than MailSender and
 * SimpleMailMessage, therefore there's no optional richer interface like
 * the JavaMailSender interface for the JavaMailSenderImpl implementation.
 *
 * <p>Does not support "replyTo" and "sentDate" fields; will consequently
 * throw an exception when encountering either of those.
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
			SimpleMailMessage simpleMessage = simpleMessages[i];

			if (simpleMessage.getReplyTo() != null) {
				throw new MailParseException("CosMailSenderImpl does not support replyTo field - " + simpleMessage);
			}
			if (simpleMessage.getSentDate() != null) {
				throw new MailParseException("CosMailSenderImpl does not support sentDate field - " + simpleMessage);
			}

			try {
				MailMessage cosMessage = new MailMessage(this.host);
				cosMessage.from(simpleMessage.getFrom());
				if (simpleMessage.getTo() != null) {
					for (int j = 0; j < simpleMessage.getTo().length; j++) {
						cosMessage.to(simpleMessage.getTo()[j]);
					}
				}
				if (simpleMessage.getCc() != null) {
					for (int j = 0; j < simpleMessage.getCc().length; j++) {
						cosMessage.cc(simpleMessage.getCc()[j]);
					}
				}
				if (simpleMessage.getBcc() != null) {
					for (int j = 0; j < simpleMessage.getBcc().length; j++) {
						cosMessage.bcc(simpleMessage.getBcc()[j]);
					}
				}
				cosMessage.setSubject(simpleMessage.getSubject());
				PrintStream textStream = cosMessage.getPrintStream();
				textStream.print(simpleMessage.getText());
				cosMessage.sendAndClose();
			}
			catch (IOException ex) {
				failedMessages.put(simpleMessage, ex);
			}
		}

		if (!failedMessages.isEmpty()) {
			throw new MailSendException(failedMessages);
		}
	}

}
