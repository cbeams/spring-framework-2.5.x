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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Exception thrown when a mail sending error is encountered.
 * Can register failed messages with their exceptions.
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 */
public class MailSendException extends MailException {

	private Map failedMessages = new HashMap();

	public MailSendException(String msg) {
		super(msg);
	}

	public MailSendException(String msg, Throwable ex) {
		super(msg, ex);
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
	 * invoked send method, i.e. SimpleMailMessages in case of using the
	 * generic MailSender interface.
	 * <p>In case of sending MimeMessage instances via JavaMailSender,
	 * the messages will be of type MimeMessage.
	 * @return the Map of failed messages as keys and thrown exceptions as
	 * values, or an empty Map if no failed messages
	 */
	public Map getFailedMessages() {
		return failedMessages;
	}

	public String getMessage() {
		StringBuffer msg = new StringBuffer();
		String superMsg = super.getMessage();
		msg.append(superMsg != null ? superMsg : "Could not send mails: ");
		for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
			Exception subEx = (Exception) subExs.next();
			msg.append(subEx.getMessage());
			if (subExs.hasNext()) {
				msg.append("; ");
			}
		}
		return msg.toString();
	}

	public void printStackTrace(PrintStream ps) {
		if (this.failedMessages.isEmpty()) {
			super.printStackTrace(ps);
		}
		else {
			ps.println(this);
			for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
				Exception subEx = (Exception) subExs.next();
				subEx.printStackTrace(ps);
				if (subExs.hasNext()) {
					ps.println();
				}
			}
		}
	}

	public void printStackTrace(PrintWriter pw) {
		if (this.failedMessages.isEmpty()) {
			super.printStackTrace(pw);
		}
		else {
			pw.println(this);
			for (Iterator subExs = this.failedMessages.values().iterator(); subExs.hasNext();) {
				Exception subEx = (Exception) subExs.next();
				subEx.printStackTrace(pw);
				if (subExs.hasNext()) {
					pw.println();
				}
			}
		}
	}

}
