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
import java.util.Date;

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
import org.springframework.core.io.Resource;

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
 *
 * <pre>
 * mailSender.send(new MimeMessagePreparator() {
 *   public void prepare(MimeMessage mimeMessage) throws MessagingException {
 *     MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
 *     message.setFrom("me@mail.com");
 *     message.setTo("you@mail.com");
 *     message.setSubject("my subject");
 *     message.setText("my text");
 *     message.addAttachment("logo.gif", new ClassPathResource("images/mylogo.gif"));
 *   }
 * });</pre>
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
	 * Create a new MimeMessageHelper for the given MimeMessage,
	 * assuming a simple text message (no multipart content).
	 * @param mimeMessage MimeMessage to work on
	 * @see #MimeMessageHelper(javax.mail.internet.MimeMessage, boolean)
	 */
	public MimeMessageHelper(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}

	/**
	 * Create a new MimeMessageHelper for the given MimeMessage,
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
	 * Create a new MimeMessageHelper for the given MimeMessage,
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
	 * Create a new MimeMessageHelper for the given MimeMessage,
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
	public final MimeMessage getMimeMessage() {
		return mimeMessage;
	}

	/**
	 * Return whether this helper is in multipart mode,
	 * i.e. holds a multipart message.
	 * @see MimeMessageHelper(MimeMessage, boolean)
	 */
	public final boolean isMultipart() {
		return (this.mimeMultipart != null);
	}

	/**
	 * Return the underlying MIME multipart object, if any
	 * @throws IllegalStateException if this helper is not in multipart mode
	 * @see #isMultipart
	 */
	public final MimeMultipart getMimeMultipart() throws IllegalStateException {
		if (this.mimeMultipart == null) {
			throw new IllegalStateException("Cannot access root multipart object - not in multipart mode");
		}
		return this.mimeMultipart;
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
		this.mimeMessage.setFrom(getEncoding() != null ?
		                         new InternetAddress(from, personal, getEncoding()) :
		                         new InternetAddress(from, personal));
	}

	public void setReplyTo(InternetAddress replyTo) throws MessagingException {
		this.mimeMessage.setReplyTo(new InternetAddress[] {replyTo});
	}

	public void setReplyTo(String replyTo) throws MessagingException {
		this.mimeMessage.setReplyTo(new InternetAddress[] {new InternetAddress(replyTo)});
	}

	public void setReplyTo(String replyTo, String personal) throws MessagingException, UnsupportedEncodingException {
		InternetAddress replyToAddress = (getEncoding() != null) ?
				new InternetAddress(replyTo, personal, getEncoding()) : new InternetAddress(replyTo, personal);
		this.mimeMessage.setReplyTo(new InternetAddress[] {replyToAddress});
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
		                              getEncoding() != null ?
		                              new InternetAddress(to, personal, getEncoding()) :
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
		                              getEncoding() != null ?
		                              new InternetAddress(cc, personal, getEncoding()) :
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
		                              getEncoding() != null ?
		                              new InternetAddress(bcc, personal, getEncoding()) :
		                              new InternetAddress(bcc, personal));
	}


	public void setSentDate(Date sentDate) throws MessagingException {
		this.mimeMessage.setSentDate(sentDate);
	}

	public void setSubject(String subject) throws MessagingException {
		if (getEncoding() != null) {
			this.mimeMessage.setSubject(subject, getEncoding());
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
			partToUse = getMainPart();
		}
		else {
			partToUse = this.mimeMessage;
		}
		setTextToMimePart(partToUse, text, html);
	}

	private MimeBodyPart getMainPart() throws MessagingException {
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
		return bodyPart;
	}

	private void setTextToMimePart(MimePart mimePart, final String text, boolean html) throws MessagingException {
		if (html) {
			// Need to use a javax.activation.DataSource to set a text
			// with content type "text/html"!
			InputStreamSource isSource = new InputStreamSource() {
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(getEncoding() != null ? text.getBytes(getEncoding()) : text.getBytes());
				}
			};
			mimePart.setDataHandler(new DataHandler(createDataSource(isSource, "text/html", "text")));
		}
		else {
			if (getEncoding() != null) {
				mimePart.setText(text, getEncoding());
			}
			else {
				mimePart.setText(text);
			}
		}
	}


	/**
	 * Add an attachment to the MimeMessage, taking the content from a
	 * <code>javax.activation.DataSource</code>.
	 * <p>Note that the InputStream returned by the DataSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * getInputStream() multiple times.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail (the content type will be determined by this)
	 * @param dataSource the <code>javax.activation.DataSource</code> to take
	 * the content from, determining the InputStream and the content type
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
	 * @see #addAttachment(String, java.io.File)
	 */
	public void addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setFileName(attachmentFilename);
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		getMimeMultipart().addBodyPart(mimeBodyPart);
	}

	/**
	 * Add an attachment to the MimeMessage, taking the content from a
	 * <code>java.io.File</code>.
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
	 * Add an attachment to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.io.InputStreamResource</code>.
	 * <p>The content type will be determined by the given filename for
	 * the attachment. Thus, any content source will be fine, including
	 * temporary files with arbitrary filenames.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail
	 * @param inputStreamSource the resource to take the content from
	 * @see #addAttachment(String, java.io.File)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 */
	public void addAttachment(String attachmentFilename, InputStreamSource inputStreamSource)
	    throws MessagingException {
		String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(attachmentFilename);
		DataSource dataSource = createDataSource(inputStreamSource, contentType, attachmentFilename);
		addAttachment(attachmentFilename, dataSource);
	}


	/**
	 * Add an inline element to the MimeMessage, taking the content from a
	 * <code>javax.activation.DataSource</code>.
	 * <p>Note that the InputStream returned by the DataSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * getInputStream() multiple times.
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param dataSource the <code>javax.activation.DataSource</code> to take
	 * the content from, determining the InputStream and the content type
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
	 */
	public void addInline(String contentId, DataSource dataSource) throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		mimeBodyPart.setHeader("Content-ID", "<" + contentId + ">");
		mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
		getMimeMultipart().addBodyPart(mimeBodyPart);
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from a
	 * <code>java.io.File</code>.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param file the File resource to take the content from
	 * @throws MessagingException
	 * @see #addAttachment(String, org.springframework.core.io.InputStreamSource)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 */
	public void addInline(String contentId, File file) throws MessagingException {
		addInline(contentId, new FileDataSource(file));
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.io.InputStreamResource</code>.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param resource the resource to take the content from
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 */
	public void addInline(String contentId, Resource resource) throws MessagingException {
		String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(resource.getFilename());
		addInline(contentId, createDataSource(resource, contentType, "inline"));
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.InputStreamResource</code>.
	 * <p>Note that you can determine the content type for any given filename
	 * via the Activation Framework's FileTypeMap utility:<br>
	 * <code>FileTypeMap.getDefaultFileTypeMap().getContentType(myFilename)</code>
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param inputStreamSource the resource to take the content from
	 * @param contentType the content type to use for the element
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, javax.activation.DataSource)
	 * @see javax.activation.FileTypeMap#getDefaultFileTypeMap
	 * @see javax.activation.FileTypeMap#getContentType
	 */
	public void addInline(String contentId, InputStreamSource inputStreamSource, String contentType)
	    throws MessagingException {
		addInline(contentId, createDataSource(inputStreamSource, contentType, "inline"));
	}


	/**
	 * Create an Activation Framework DataSource for the given InputStreamSource.
	 * @param inputStreamSource the InputStreamSource (typically a Spring Resource)
	 * @param contentType the content type
	 * @param name the name of the DataSource
	 * @return the Activation Framework DataSource
	 */
	private static DataSource createDataSource(
	    final InputStreamSource inputStreamSource, final String contentType, final String name) {
		return new DataSource() {
			public InputStream getInputStream() throws IOException {
				return inputStreamSource.getInputStream();
			}
			public OutputStream getOutputStream() {
				throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
			}
			public String getContentType() {
				return contentType;
			}
			public String getName() {
				return name;
			}
		};
	}

}
