/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;


/**
 * This interface defines a strategy to send electronic mail.
 * It is used by Spring's mail support, {@link MailTemplate} in particular
 * @author Dmitriy Kopylenko
 * @version $Id: MailSender.java,v 1.1 2003-09-10 00:14:54 dkopylenko Exp $
 */
public interface MailSender {
	
	/**
	 * Send electronic mail with provided mail properties
	 * @param mailSettings encapsulated mail properties needed to send electronic mail
	 * @throws MailException in case of system errors
	 */
	public void send(MailSettings mailSettings) throws MailException;

}
