package org.springframework.context.support;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.util.StringUtils;

/**
 * MessageSource that accesses the ResourceBundle with the specified basename.
 * This class relies on the caching of the underlying JDK's ResourceBundle
 * implementation.
 *
 * <p>Unfortunately, java.util.ResourceBundle caches loaded bundles indefinitely.
 * Reloading a bundle during VM execution is <i>not</i> possible by any means.
 * As this MessageSource relies on ResourceBundle, it faces the same limitation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setBasenames
 * @see java.util.ResourceBundle
 */
public class ResourceBundleMessageSource extends AbstractNestingMessageSource {

	private String[] basenames;

	/**
	 * Set a single basename, following ResourceBundle conventions:
	 * It is a fully-qualified classname. If it doesn't contain a package qualifier
	 * (such as org.mypackage), it will be resolved from the classpath root.
	 * <p>Messages will normally be held in the /lib or /classes directory of a WAR.
	 * They can also be held in Jars on the class path. For example, a Jar in an
	 * application's manifest classpath could contain messages for the application.
	 * @param basename the single basename
	 * @see #setBasenames
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(new String[] {basename});
	}

	/**
	 * Set an array of basenames, each following ResourceBundle conventions.
	 * The associated resource bundles will be checked sequentially when
	 * resolving a message code.
	 * <p>Note that message definitions in a <i>previous</i> resource bundle
	 * will override ones in a later bundle, due to the sequential lookup.
	 * @param basenames an array of basenames
	 * @see #setBasename
	 * @see java.util.ResourceBundle
	 */
	public void setBasenames(String[] basenames)  {
		this.basenames = basenames;
	}

	protected String resolve(String code, Locale locale) {
		String msg = null;
		for (int i = 0; msg == null && i < this.basenames.length; i++) {
			String basename = this.basenames[i];
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(basename, locale,
				                                                 Thread.currentThread().getContextClassLoader());
				try {
					msg = bundle.getString(code);
				}
				catch (MissingResourceException ex) {
					// assume key not found
					// -> do NOT throw the exception to allow for checking parent message source
					msg = null;
				}
			}
			catch (MissingResourceException ex) {
				logger.warn("No ResourceBundle found for MessageSource: " + ex.getMessage());
				// assume bundle not found
				// -> do NOT throw the exception to allow for checking parent message source
				msg = null;
			}
		}
		return msg;
	}
	
	/**
	 * Show the configuration of this MessageSource.
	 */
	public String toString() {
		return getClass().getName() + " with basenames [" + StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}

}
