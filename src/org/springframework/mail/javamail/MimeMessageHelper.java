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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import org.springframework.core.io.InputStreamSource;

/**
 * Helper class for easy population of a <code>javax.mail.internet.MimeMessage</code>.
 *
 * <p>Mirrors the simple setters of SimpleMailMessage, directly applying the values
 * to the underlying MimeMessage. Allows to define a character encoding for the
 * entire message, automatically applied by all methods of this helper.
 *
 * <p>Also offers support for typical mail attachments, and for personal names
 * that accompany mail addresses. Note that advanced settings can still be applied
 * directly to underlying MimeMessage!
 *
 * <p>Typically used in MimeMessagePreparator implementations or JavaMailSender
 * client code: simply instantiating it as a MimeMessage wrapper, invoking
 * setters on the wrapper, using the underlying MimeMessage for mail sending.
 * Also used internally by JavaMailSenderImpl.
 *
 * <p>Sample code:
 * <p><code>
 * mailSender.send(new MimeMessagePreparator() {<br>
 * &nbsp;&nbsp;public void prepare(MimeMessage mimeMessage) throws MessagingException {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;message.setFrom("me@mail.com");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;message.setTo("you@mail.com");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;message.setSubject("my subject");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;message.setText("my text");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;message.addAttachment("logo.gif", new ClassPathResource("images/mylogo.gif"));<br>
 * &nbsp;&nbsp;}<br>
 * });
 * </code>
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see javax.mail.internet.MimeMessage
 * @see #getMimeMessage
 * @see MimeMessagePreparator
 * @see JavaMailSender
 * @see JavaMailSenderImpl
 * @see org.springframework.mail.SimpleMailMessage
 */
public class MimeMessageHelper {

	private final MimeMessage mimeMessage;

	private MimeMultipart mimeMultipart;

	private String encoding;


	/**
	 * Create new MimeMessageHelper for the given MimeMessage,
	 * assuming a simple text message (no multipart content).
	 * @param mimeMessage MimeMessage to work on
	 * @see #MimeMessageHelper(javax.mail.internet.MimeMessage, boolean)
	 */
	public MimeMessageHelper(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}

	/**
	 * Create new MimeMessageHelper for the given MimeMessage,
	 * in multipart mode (supporting attachments) if requested.
	 * @param mimeMessage MimeMessage to work on
	 * @param multipart whether to create a multipart message that
	 * supports attachments
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
		this.mimeMessage = mimeMessage;
		if (multipart) {
			this.mimeMultipart = new MimeMultipart();
			this.mimeMessage.setContent(this.mimeMultipart);
		}
	}

	/**
	 * Create new MimeMessageHelper for the given MimeMessage,
	 * assuming a simple text message (no multipart content).
	 * @param mimeMessage MimeMessage to work on
	 * @param encoding the character encoding to use for the message
	 * @see #MimeMessageHelper(javax.mail.internet.MimeMessage, boolean)
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, String encoding) {
		this(mimeMessage);
		this.encoding = encoding;
	}

	/**
	 * Create new MimeMessageHelper for the given MimeMessage,
	 * in multipart mode (supporting attachments) if requested.
	 * @param mimeMessage MimeMessage to work on
	 * @param multipart whether to create a multipart message that
	 * supports attachments
	 * @param encoding the character encoding to use for the message
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart, String encoding)
	    throws MessagingException {
		this(mimeMessage, multipart);
		this.encoding = encoding;
	}

	/**
	 * Return the underlying MimeMessage.
	 */
	public MimeMessage getMimeMessage() {
		return mimeMessage;
	}

	/**
	 * Return the character encoding used for this message.
	 */
	public String getEncoding() {
		return encoding;
	}


	public void setFrom(InternetAddress from) throws MessagingException {
		this.mimeMessage.setFrom(from);
	}

	public void setFrom(String from) throws MessagingException {
		this.mimeMessage.setFrom(new InternetAddress(from));
	}

	public void setFrom(String from, String personal) throws MessagingException, UnsupportedEncodingException {
		this.mimeMessage.setFrom(this.encoding != null ?
		                         new InternetAddress(from, personal, this.encoding) :
		                         new InternetAddress(from, personal));
	}


	public void setTo(InternetAddress to) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.TO, to);
	}

	public void setTo(InternetAddress[] to) throws MessagingException {
		this.mimeMessage.setRecipients(Message.RecipientType.TO, to);
	}

	public void setTo(String to) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
	}

	public void setTo(String[] to) throws MessagingException {
		InternetAddress[] addresses = new InternetAddress[to.length];
		for (int i = 0; i < to.length; i++) {
			addresses[i] = new InternetAddress(to[i]);
		}
		this.mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
	}

	public void addTo(InternetAddress to) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.TO, to);
	}

	public void addTo(String to) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	}

	public void addTo(String to, String personal) throws MessagingException, UnsupportedEncodingException {
		this.mimeMessage.addRecipient(Message.RecipientType.TO,
		                              this.encoding != null ?
		                              new InternetAddress(to, personal, this.encoding) :
		                              new InternetAddress(to, personal));
	}


	public void setCc(InternetAddress cc) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.CC, cc);
	}

	public void setCc(InternetAddress[] cc) throws MessagingException {
		this.mimeMessage.setRecipients(Message.RecipientType.CC, cc);
	}

	public void setCc(String cc) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.CC, new InternetAddress(cc));
	}

	public void setCc(String[] cc) throws MessagingException {
		InternetAddress[] addresses = new InternetAddress[cc.length];
		for (int i = 0; i < cc.length; i++) {
			addresses[i] = new InternetAddress(cc[i]);
		}
		this.mimeMessage.setRecipients(Message.RecipientType.CC, addresses);
	}

	public void addCc(InternetAddress cc) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.CC, cc);
	}

	public void addCc(String cc) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
	}

	public void addCc(String cc, String personal) throws MessagingException, UnsupportedEncodingException {
		this.mimeMessage.addRecipient(Message.RecipientType.CC,
		                              this.encoding != null ?
		                              new InternetAddress(cc, personal, this.encoding) :
		                              new InternetAddress(cc, personal));
	}


	public void setBcc(InternetAddress bcc) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(InternetAddress[] bcc) throws MessagingException {
		this.mimeMessage.setRecipients(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(String bcc) throws MessagingException {
		this.mimeMessage.setRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
	}

	public void setBcc(String[] bcc) throws MessagingException {
		InternetAddress[] addresses = new InternetAddress[bcc.length];
		for (int i = 0; i < bcc.length; i++) {
			addresses[i] = new InternetAddress(bcc[i]);
		}
		this.mimeMessage.setRecipients(Message.RecipientType.BCC, addresses);
	}

	public void addBcc(InternetAddress bcc) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.BCC, bcc);
	}

	public void addBcc(String bcc) throws MessagingException {
		this.mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
	}

	public void addBcc(String bcc, String personal) throws MessagingException, UnsupportedEncodingException {
		this.mimeMessage.addRecipient(Message.RecipientType.BCC,
		                              this.encoding != null ?
		                              new InternetAddress(bcc, personal, this.encoding) :
		                              new InternetAddress(bcc, personal));
	}


	public void setSubject(String subject) throws MessagingException {
		if (this.encoding != null) {
			this.mimeMessage.setSubject(subject, this.encoding);
		}
		else {
			this.mimeMessage.setSubject(subject);
		}
	}

	public void setText(String text) throws MessagingException {
		setText(text, false);
	}

	/**
	 * Set the given text directly as content in non-multipart mode
	 * respectively as default body part in multipart mode.
	 * @param text text to set
	 * @param html whether to apply content type "text/html" for an
	 * HTML mail, using default content type ("text/plain") else
	 */
	public void setText(final String text, boolean html) throws MessagingException {
		MimePart partToUse = null;
		if (this.mimeMultipart != null) {
			MimeBodyPart bodyPart = null;
			for (int i = 0; i < this.mimeMultipart.getCount(); i++) {
				BodyPart bp = this.mimeMultipart.getBodyPart(i);
				if (bp.getFileName() == null) {
					bodyPart = (MimeBodyPart) bp;
				}
			}
			if (bodyPart == null) {
				MimeBodyPart mimeBodyPart = new MimeBodyPart();
				this.mimeMultipart.addBodyPart(mimeBodyPart);
				bodyPart = mimeBodyPart;
			}
			partToUse = bodyPart;
		}
		else {
			partToUse = this.mimeMessage;
		}
		
		if (html) {
			// need to use a javax.activation.DataSource (!) to set a text
			// with content type "text/html"
			partToUse.setDataHandler(new DataHandler(
			    new DataSource() {
						public InputStream getInputStream() throws IOException {
							return new ByteArrayInputStream(encoding != null ? text.getBytes(encoding) : text.getBytes());
						}
						public OutputStream getOutputStream() throws IOException {
							throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
						}
						public String getContentType() {
							return "text/html";
						}
						public String getName() {
							return "text";
						}
			    }
			));
		}
		else {
			if (this.encoding != null) {
				partToUse.setText(text, this.encoding);
			}
			else {
				partToUse.setText(text);
			}
		}
	}


	/**
	 * Add an attachment to the given MimeMessage, taking the content
	 * from a java.io.File.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail
	 * @param file the File resource to take the content from
	 * @throws MessagingException
	 * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 */
	public void addAttachment(String attachmentFilename, File file) throws MessagingException {
		addAttachment(attachmentFilename, new FileDataSource(file));
	}

	/**
	 * Add an attachment to the given MimeMessage, taking the content
	 * from an org.springframework.core.InputStreamResource.
	 * <p>The content type will be determined by the given filename for
	 * the attachment. Thus, any content source will be fine, including
	 * temporary files with arbitrary filenames.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail
	 * @param inputStreamSource the resource to take the content from
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 */
	public void addAttachment(final String attachmentFilename, final InputStreamSource inputStreamSource)
	    throws MessagingException {
		addAttachment(attachmentFilename,
		              new DataSource() {
			              public InputStream getInputStream() throws IOException {
											return inputStreamSource.getInputStream();
										}
										public OutputStream getOutputStream() {
											throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
										}
										public String getContentType() {
											return FileTypeMap.getDefaultFileTypeMap().getContentType(attachmentFilename);
										}
										public String getName() {
											return attachmentFilename;
										}
									});
	}

	/**
	 * Add an attachment to the given MimeMessage,
	 * taking the content from a <code>javax.activation.DataSource</code>.
	 * <p>Note that the InputStream returned by the DataSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * getInputStream() multiple times.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail (the content type will be determined by this)
	 * @param dataSource the <code>javax.activation.DataSource</code> to take
	 * the content from, determining the InputStream and the content type
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
	 */
	public void addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
		if (this.mimeMultipart == null) {
			throw new IllegalStateException("Cannot add attachment - not in multipart mode");
		}
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setFileName(attachmentFilename);
		bodyPart.setDataHandler(new DataHandler(dataSource));
		this.mimeMultipart.addBodyPart(bodyPart);
	}

}
