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

package org.springframework.jms.support;

import java.lang.reflect.Constructor;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.JmsException;
import org.springframework.jms.JmsSecurityException;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.util.ClassUtils;

/**
 * Generic utility methods for working with JMS.
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class JmsUtils {

	private static final Log logger = LogFactory.getLog(JmsUtils.class);

	/**
	 * Close the given JMS Connection and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JMS code.
	 * @param con the JMS Connection to close
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			}
			catch (JMSException ex) {
				logger.warn("Failed to close the connection", ex);
			}
		}
	}

	/**
	 * Close the given JMS Session and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JMS code.
	 * @param session the JMS Session to close
	 */
	public static void closeSession(Session session) {
		if (session != null) {
			try {
				session.close();
			}
			catch (JMSException ex) {
				logger.warn("Failed to close the session", ex);
			}
		}
	}

	/**
	 * Close the given JMS MessageConsumer and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JMS code.
	 * @param consumer the JMS MessageConsumer to close
	 */
	public static void closeMessageConsumer(MessageConsumer consumer) {
		if (consumer != null) {
			try {
				consumer.close();
			}
			catch (JMSException ex) {
				logger.warn("Failed to close the consumer", ex);
			}
		}
	}

	/**
	 * Convert the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * @param ex the original checked JMSException to convert
	 * @return the Spring runtime JmsException wrapping <code>ex</code>.
	 */
	public static JmsException convertJmsAccessException(JMSException ex) {
		if (ex instanceof JMSSecurityException) {
			return new JmsSecurityException((JMSSecurityException) ex);
		}

		if (JMSException.class.equals(ex.getClass().getSuperclass())) {
			// All other exceptions in our Jms runtime exception hierarchy have the
			// same unqualified names as their javax.jms counterparts, so just
			// construct the converted exception dynamically based on name.
			String shortName = ClassUtils.getShortName(ex.getClass().getName());

			// all JmsException subclasses are in the same package:
			String longName = JmsException.class.getPackage().getName() + "." + shortName;

			try {
				Class clazz = Class.forName(longName);
				Constructor ctor = clazz.getConstructor(new Class[] {ex.getClass()});
				Object counterpart = ctor.newInstance(new Object[]{ex});
				return (JmsException) counterpart;
			}
			catch (Throwable ex2) {
				if (logger.isDebugEnabled()) {
					logger.debug("Couldn't resolve JmsException class [" + longName + "]", ex2);
				}
				return new UncategorizedJmsException(ex);
			}
		}

		// fallback: uncategorized
		return new UncategorizedJmsException(ex);
	}

}
