/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.support;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Convenient superclass for application objects that want to be aware of
 * the application context, e.g. for custom lookup of collaborating beans
 * or for context-specific resource access. It saves the application
 * context reference and provides an initialization callback method.
 * Furthermore, it offers numerous convenience methods for message lookup.
 *
 * <p>There is no requirement to subclass this class: It just makes things
 * a little easier if you need access to the context, e.g. for access to
 * file resources or to the message source. Note that many application
 * objects do not need to be aware of the application context at all,
 * as they can receive collaborating beans via bean references.
 *
 * <p>Many framework classes are derived from this class, particularly
 * within the web support.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {
	
	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	/** ApplicationContext this object runs in */
	private ApplicationContext applicationContext;

	public final void setApplicationContext(ApplicationContext ctx) throws ApplicationContextException {
		// ignore reinitialization
		if (this.applicationContext == null) {
			if (!requiredContextClass().isInstance(ctx)) {
				throw new ApplicationContextException("Invalid application context: needs to be of type '" + requiredContextClass().getName() + "'");
			}
			this.applicationContext = ctx;
			initApplicationContext();
		}
	}
	
	/**
	 * Return the ApplicationContext instance used by this object.
	 */
	public final ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Determine the context class that any context passed to
	 * setApplicationContext must be an instance of.
	 * Can be overridden in subclasses.
	 */
	protected Class requiredContextClass() {
		return ApplicationContext.class;
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called by setApplicationContext() after setting the context instance.
	 * <p>Note: Does </i>not</i> get called on reinitialization of the context.
	 * @throws ApplicationContextException if initialization attempted
	 * by this object fails
	 */
	protected void initApplicationContext() throws ApplicationContextException {
	}

	/**
	 * Retrieve the message for the given code and the default Locale.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	protected final String getMessage(String code, String defaultMessage) {
		return getApplicationContext().getMessage(code, null, defaultMessage, Locale.getDefault());
	}

	/**
	 * Retrieve the message for the given code and the given Locale.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @param locale Locale in which to do lookup
	 * @return the message
	 */
	protected final String getMessage(String code, String defaultMessage, Locale locale) {
		return getApplicationContext().getMessage(code, null, defaultMessage, locale);
	}

	/**
	 * Retrieve the message for the given code and the default Locale.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	protected final String getMessage(String code, Object[] args, String defaultMessage) {
		return getApplicationContext().getMessage(code, args, defaultMessage, Locale.getDefault());
	}

	/**
	 * Retrieve the message for the given code and the given Locale.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @param locale Locale in which to do lookup
	 * @return the message
	 */
	protected final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return getApplicationContext().getMessage(code, args, defaultMessage, locale);
	}

	/**
	 * Retrieve the message for the given code and the default Locale.
	 * @param code code of the message
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(String code) throws NoSuchMessageException {
		return getApplicationContext().getMessage(code, null, Locale.getDefault());
	}

	/**
	 * Retrieve the message for the given code and the given Locale.
	 * @param code code of the message
	 * @param locale Locale in which to do lookup
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(String code, Locale locale) throws NoSuchMessageException {
		return getApplicationContext().getMessage(code, null, locale);
	}

	/**
	 * Retrieve the message for the given code and the default Locale.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(String code, Object[] args) throws NoSuchMessageException {
		return getApplicationContext().getMessage(code, args, Locale.getDefault());
	}

	/**
	 * Retrieve the message for the given code and the given Locale.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param locale Locale in which to do lookup
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return getApplicationContext().getMessage(code, args, locale);
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
	 * in the default Locale.
	 * @param resolvable the MessageSourceResolvable
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return getApplicationContext().getMessage(resolvable, Locale.getDefault());
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
	 * in the given Locale.
	 * @param resolvable the MessageSourceResolvable
	 * @param locale Locale in which to do lookup
	 * @return the message
	 * @throws NoSuchMessageException if not found
	 */
	protected final String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getApplicationContext().getMessage(resolvable, locale);
	}

}
