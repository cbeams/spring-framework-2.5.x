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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.util.StringUtils;

/**
 * MessageSource that accesses the ResourceBundles with the specified basenames.
 * This class relies on the underlying JDK's java.util.ResourceBundle implementation.
 *
 * <p>Unfortunately, java.util.ResourceBundle caches loaded bundles indefinitely.
 * Reloading a bundle during VM execution is <i>not</i> possible by any means.
 * As this MessageSource relies on ResourceBundle, it faces the same limitation.
 * Consider ReloadableResourceBundleMessageSource for an alternative.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setBasenames
 * @see ReloadableResourceBundleMessageSource
 * @see java.util.ResourceBundle
 */
public class ResourceBundleMessageSource extends AbstractMessageSource {

	private String[] basenames;

	private ClassLoader classLoader;

	/**
	 * Cache to hold already generated MessageFormats per message code.
	 * Note that this Map contains the actual code Map, keyed with the Locale.
	 * @see #getMessageFormat
	 */
	private final Map cachedMessageFormats = new HashMap();


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

	/**
	 * Set the ClassLoader to load resource bundles with,
	 * or null for using the thread context class loader on actual access
	 * (applying to the thread that does the "getMessage" call).
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	protected final MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat messageFormat = null;
		for (int i = 0; messageFormat == null && i < this.basenames.length; i++) {
			messageFormat = resolve(this.basenames[i], code, locale);
		}
		return messageFormat;
	}

	/**
	 * Return a MessageFormat for the given bundle basename, message code,
	 * and Locale.
	 * @param basename the basename of the bundle
	 * @param code the message code to retrieve
	 * @param locale the Locale to resolve for
	 * @return the resulting MessageFormat
	 */
	protected MessageFormat resolve(String basename, String code, Locale locale) {
		try {
			ClassLoader cl = this.classLoader;
			if (cl == null) {
				// no class loader specified -> use thread context class loader
				cl = Thread.currentThread().getContextClassLoader();
			}
			ResourceBundle bundle = ResourceBundle.getBundle(basename, locale, cl);
			try {
				return getMessageFormat(bundle, code, locale);
			}
			catch (MissingResourceException ex) {
				// assume key not found
				// -> do NOT throw the exception to allow for checking parent message source
				return null;
			}
		}
		catch (MissingResourceException ex) {
			logger.warn("ResourceBundle [" + basename + "] not found for MessageSource: " + ex.getMessage());
			// assume bundle not found
			// -> do NOT throw the exception to allow for checking parent message source
			return null;
		}
	}

	/**
	 * Return a MessageFormat for the given bundle and code,
	 * fetching already generated MessageFormats from the cache.
	 * @param bundle the ResourceBundle to work on
	 * @param code the message code to retrieve
	 * @param locale the Locale to use to build the MessageFormat
	 * @return the resulting MessageFormat
	 */
	protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
			throws MissingResourceException {
		synchronized (this.cachedMessageFormats) {
			Map codeMap = (Map) this.cachedMessageFormats.get(bundle);
			Map localeMap = null;
			if (codeMap != null) {
				localeMap = (Map) codeMap.get(code);
				if (localeMap != null) {
					MessageFormat result = (MessageFormat) localeMap.get(locale);
					if (result != null) {
						return result;
					}
				}
			}
			String msg = bundle.getString(code);
			if (msg != null) {
				if (codeMap == null) {
					codeMap = new HashMap();
					this.cachedMessageFormats.put(bundle, codeMap);
				}
				if (localeMap == null) {
					localeMap = new HashMap();
					codeMap.put(code, localeMap);
				}
				MessageFormat result = createMessageFormat(msg, locale);
				localeMap.put(locale, result);
				return result;
			}
			return null;
		}
	}

	/**
	 * Show the configuration of this MessageSource.
	 */
	public String toString() {
		return getClass().getName() + ": basenames=[" + StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}

}
