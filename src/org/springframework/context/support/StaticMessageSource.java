package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple implementation of NestingMessageSource that allows messages
 * to be held in a Java object, and added programmatically.
 * This class now supports internationalization.
 *
 * <p>Intended for testing, rather than use production systems.
 *
 * @author Rod Johnson
 */
public class StaticMessageSource extends AbstractNestingMessageSource {

	private final Log logger = LogFactory.getLog(getClass());

	private Map messages = new HashMap();

	protected MessageFormat resolve(String code, Locale locale) {
		return (MessageFormat) this.messages.get(code + "_" + locale.toString());
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
   * @param locale locale message should be found within
	 * @param message message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String message) {
		this.messages.put(code + "_" + locale.toString(), new MessageFormat(message));
		logger.info("Added message [" + message + "] for code [" + code + "] and Locale [" + locale + "]");
	}

	public String toString() {
		return getClass().getName() + ": " + this.messages;
	}

}

