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

package org.springframework.orm.toplink.support;

import java.io.StringWriter;
import java.io.Writer;

import oracle.toplink.sessions.DefaultSessionLog;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.SessionLogEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;

/**
 * @author Juergen Hoeller
 * @since 1.2
 */
public class CommonsLoggingSessionLog extends DefaultSessionLog {

	private final Log logger = LogFactory.getLog(Session.class);

	public void log(SessionLogEntry entry) {
		if (entry.hasException() && (!shouldLogExceptions() || !logger.isWarnEnabled())) {
			return;
		}
		if (!entry.hasException() && !logger.isDebugEnabled()) {
			return;
		}
		if (entry.isDebug() && !shouldLogDebug()) {
			return;
		}

		String message = null;

		synchronized (this) {
			StringWriter writer = new StringWriter();
			setWriter(writer);
			super.log(entry);
			message = StringUtils.trimTrailingWhitespace(writer.toString());
			setWriter((Writer) null);
		}

		if (entry.hasException()) {
			this.logger.warn(message, entry.getException());
		}
		else {
			this.logger.debug(message);
		}
	}

}
