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

package org.springframework.mail;

import org.springframework.util.StringUtils;

/**
 * Encapsulates properties of a simple mail such as from, to, cc,
 * subject, text. To be sent with a MailSender implementation.
 *
 * <p>Consider JavaMailSender and JavaMail MimeMessages for creating
 * more sophisticated messages, for example with attachments, special
 * character encodings, or personal names that accompany mail addresses.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 10.09.2003
 * @version $Id: SimpleMailMessage.java,v 1.7 2004-03-18 02:46:05 trisberg Exp $
 * @see MailSender
 * @see org.springframework.mail.javamail.JavaMailSender
 * @see org.springframework.mail.javamail.MimeMessagePreparator
 * @see org.springframework.mail.javamail.MimeMessageHelper
 */
public class SimpleMailMessage {

	private String from;

	private String[] to;

	private String[] cc;

	private String[] bcc;

	private String subject;

	private String text;

	/**
	 * Create new SimpleMailMessage.
	 */
	public SimpleMailMessage() {
	}

	/**
	 * Copy constructor.
	 */
	public SimpleMailMessage(SimpleMailMessage original) {
		this.from = original.getFrom();
		if (original.getTo() != null) {
			this.to = new String[original.getTo().length];
			System.arraycopy(original.getTo(), 0, this.to, 0, original.getTo().length);
		}
		if (original.getCc() != null) {
			this.cc = new String[original.getCc().length];
			System.arraycopy(original.getCc(), 0, this.cc, 0, original.getCc().length);
		}
		if (original.getBcc() != null) {
			this.bcc = new String[original.getBcc().length];
			System.arraycopy(original.getBcc(), 0, this.bcc, 0, original.getBcc().length);
		}
		this.subject = original.getSubject();
		this.text = original.getText();
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return this.from;
	}

	public void setTo(String to) {
		this.to = new String[] {to};
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String[] getTo() {
		return this.to;
	}

	public void setCc(String cc) {
		this.cc = new String[] {cc};
	}

	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String[] getCc() {
		return cc;
	}

	public void setBcc(String bcc) {
		this.bcc = new String[] {bcc};
	}

	public void setBcc(String[] bcc) {
		this.bcc = bcc;
	}

	public String[] getBcc() {
		return bcc;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SimpleMailMessage: ");
		sb.append("from: " + this.getFrom()+ "; ");
		sb.append("to: " +  StringUtils.arrayToCommaDelimitedString(this.getTo()) + "; ");
		sb.append("cc: " + StringUtils.arrayToCommaDelimitedString(this.getCc()) + "; ");
		sb.append("bcc: " + StringUtils.arrayToCommaDelimitedString(this.getBcc()) + "; ");
		sb.append("subject: " + this.getSubject()+ "; ");
		sb.append("text: " + this.getText());
		return sb.toString();
	}

}
