/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail;

import org.springframework.util.StringUtils;

/**
 * Encapsulates properties of a simple mail such as from, to, cc, subject, text.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class SimpleMailMessage {

	private String from;

	private String to;

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
		this.to = original.getTo();
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
		this.to = to;
	}

	public String getTo() {
		return this.to;
	}

	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String[] getCc() {
		return cc;
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
		StringBuffer sb = new StringBuffer("SimpleMailMessage [");
		sb.append("from: " + this.getFrom()+ "; ");
		sb.append("to: " + this.getTo() + "; ");
		sb.append("cc: " + StringUtils.arrayToCommaDelimitedString(this.getCc()) + "; ");
		sb.append("subject: " + this.getSubject()+ "; ");
		sb.append("text: " + this.getText() + "]");
		return sb.toString();
	}

}
