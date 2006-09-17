/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Exception thrown when a mail sending error is encountered.
 * Can register failed messages with their exceptions.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class MailSendException extends MailException {

	private Map failedMessages = new HashMap();


	/**
	 * Constructor for MailSendException.
	 * @param msg the detail message
	 */
	public MailSendException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for MailSendException.
	 * @param msg the detail message
	 * @param cause the root cause from the mail API in use
	 */
	public MailSendException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for registration of failed messages, with the
	 * messages that failed as keys, and the thrown exceptions as values.
	 * <p>The messages should be the same that were originally passed
	 * to the invoked send method.
	 */
	public MailSendException(Map failedMessages) {
		super(null);
		this.failedMessages.putAll(failedMessages);
	}


	/**
	 * Return a Map with the failed messages as keys, and the thrown exceptions
	 * as values. Note that a general mail server connection failure will not
	 * result in failed messages being returned here: A message will only be
	 * contained here if actually sending it was attempted but failed.
	 * <p>The messages will be the same that were originally passed to the
	 * invoked send method, that is, SimpleMailMessages in case of using
	 * the generic MailSender interface.
	 * <p>In case of sending MimeMessage instances via JavaMailSender,
	 * the messages will be of type MimeMessage.
	 * @return the Map of failed messages as keys and thrown exceptions as
	 * values, or an empty Map if no failed messages
	 * @see SimpleMailMessage
	 * @see javax.mail.internet.MimeMessage
	 */
	public final Map getFailedMessages() {
		return failedMessages;
	}


	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		String superMsg = super.getMessage();
		sb.append(superMsg != null ? superMsg : "Failed messages: ");
		for (Iterator subExs = getFailedMessages().values().iterator(); subExs.hasNext();) {
			Throwable subEx = (Throwable) subExs.next();
			sb.append(subEx.toString());
			if (subExs.hasNext()) {
				sb.append("; ");
			}
		}
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append("; nested exceptions (");
		sb.append(getFailedMessages().size()).append(") are:");
		int i = 0;
		for (Iterator subExs = getFailedMessages().values().iterator(); subExs.hasNext();) {
			Throwable subEx = (Throwable) subExs.next();
			i++;
			sb.append('\n').append("Failed message ").append(i).append(": ");
			sb.append(subEx);
		}
		return sb.toString();
	}

	public void printStackTrace(PrintStream ps) {
		if (getFailedMessages().isEmpty()) {
			super.printStackTrace(ps);
		}
		else {
			ps.println(getClass().getName() + "; nested exception details (" +
					getFailedMessages().size() + ") are:");
			int i = 0;
			for (Iterator subExs = getFailedMessages().values().iterator(); subExs.hasNext();) {
				Throwable subEx = (Throwable) subExs.next();
				i++;
				ps.println("Failed message " + i + ":");
				subEx.printStackTrace(ps);
			}
		}
	}

	public void printStackTrace(PrintWriter pw) {
		if (getFailedMessages().isEmpty()) {
			super.printStackTrace(pw);
		}
		else {
			pw.println(getClass().getName() + "; nested exception details (" +
					getFailedMessages().size() + ") are:");
			int i = 0;
			for (Iterator subExs = getFailedMessages().values().iterator(); subExs.hasNext();) {
				Throwable subEx = (Throwable) subExs.next();
				i++;
				pw.println("Failed message " + i + ":");
				subEx.printStackTrace(pw);
			}
		}
	}

}
