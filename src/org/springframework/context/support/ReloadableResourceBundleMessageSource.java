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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * MessageSource that accesses the ResourceBundles with the specified basenames.
 * This class uses java.util.Properties instances as its internal data structure
 * for messages, loading them via a PropertiesPersister strategy: The default
 * strategy can load properties files with a specific charset.
 *
 * <p>In contrast to ResourceBundleMessageSource, this class supports reloading
 * of properties files through the "cacheSeconds" setting, and also through
 * programmatically clearing the properties cache. Since application servers do
 * typically cache all files loaded from the classpath, it is necessary to store
 * resources somewhere else (for example, in the "WEB-INF" directory of a web app).
 * Otherwise changes of files in the classpath are not reflected in the application.
 *
 * <p>Note that the "basename" respectively "basenames" property has a different
 * convention here: It follows the basic ResourceBundle rule of not specifying
 * file extension or language codes, but can refer to any Spring resource location
 * (instead of being restricted to classpath resources). With a "classpath:" prefix,
 * resources can still be loaded from the classpath, but "cacheSeconds" values
 * other than "-1" (caching forever) will not work in this case.
 *
 * <p>This MessageSource can easily be used outside an ApplicationContext: It uses
 * a DefaultResourceLoader as default, getting overridden with the ApplicationContext
 * if running in a context. It does not have any other specific dependencies.
 * 
 * @author Thomas Achleitner
 * @author Juergen Hoeller
 * @see #setCacheSeconds
 * @see #setBasenames
 * @see #setDefaultEncoding
 * @see #setFileEncodings
 * @see #setPropertiesPersister
 * @see #setResourceLoader
 * @see org.springframework.util.DefaultPropertiesPersister
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see ResourceBundleMessageSource
 * @see java.util.ResourceBundle
 */
public class ReloadableResourceBundleMessageSource extends AbstractMessageSource
    implements ResourceLoaderAware {

	private static final String PROPERTIES_SUFFIX = ".properties";
	

	private String[] basenames;

	private String defaultEncoding;

	private Properties fileEncodings;

	private boolean fallbackToSystemLocale = true;

	private long cacheMillis = -1;

	/** Cache to hold filename lists per Locale */
	private final Map cachedFilenames = new HashMap();

	/** Cache to hold already loaded properties per filename */
	private final Map cachedProperties = new HashMap();

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();


	/**
	 * Set a single basename, following the basic ResourceBundle convention of
	 * not specifying file extension or language codes, but in contrast to
	 * ResourceBundleMessageSource referring to a Spring resource location:
	 * e.g. "WEB-INF/messages" for "WEB-INF/messages.properties",
	 * "WEB-INF/messages_en.properties", etc.
	 * @param basename the single basename
	 * @see #setBasenames
	 * @see org.springframework.core.io.ResourceEditor
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(new String[]{basename});
	}

	/**
	 * Set an array of basenames, each following the above-mentioned special
	 * convention. The associated resource bundles will be checked sequentially
	 * when resolving a message code.
	 * <p>Note that message definitions in a <i>previous</i> resource bundle
	 * will override ones in a later bundle, due to the sequential lookup.
	 * @param basenames an array of basenames
	 * @see #setBasename
	 * @see java.util.ResourceBundle
	 */
	public void setBasenames(String[] basenames) {
		this.basenames = basenames;
	}

	/**
	 * Set the default charset to use for parsing properties files.
	 * Used if no file-specific charset is specified for a file.
	 * <p>Default is none, using java.util.Properties' default charset.
	 * @see #setFileEncodings
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Set per-file charsets to use for parsing properties files.
	 * @param fileEncodings Properties with filenames as keys and charset
	 * names as values. Filenames have to match the basename syntax,
	 * with optional locale-specific appendices: e.g. "WEB-INF/messages"
	 * or "WEB-INF/messages_en".
	 * @see #setBasenames
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncodings(Properties fileEncodings) {
		this.fileEncodings = fileEncodings;
	}

	/**
	 * Set whether to fall back to the system Locale if no files for a specific
	 * Locale have been found. Default is true; if this is turned off, the only
	 * fallback will be the default file (e.g. "messages.properties" for
	 * basename "messages").
	 * <p>Falling back to the system Locale is the default behavior of
	 * java.util.ResourceBundle. However, this is often not desirable in an
	 * application server environment, where the system Locale is not relevant
	 * to the application at all: Set this flag to "false" in such a scenario.
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	/**
	 * Set the number of seconds to cache loaded properties files.
	 * <ul>
	 * <li>Default is "-1", indicating to cache forever (just like
	 * java.util.ResourceBundle).
	 * <li>A positive number will cache loaded properties files for the given
	 * number of seconds. This is essentially the interval between refresh attempts.
	 * Note that a refresh attempt will first check the last-modified timestamp
	 * of the file before actually reloading it; so if files don't change, this
	 * interval can be set rather low, as refresh attempts will not actually reload.
	 * <li>A value of "0" will check the last-modified timestamp of the file on
	 * every message access. <b>Do not use this in a production environment!</b>
	 * </ul>
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMillis = cacheSeconds * 1000;
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * The default is DefaultPropertiesPersister.
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister = propertiesPersister;
	}

	/**
	 * Set the ResourceLoader to use for loading bundle properties files.
	 * The default is DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	protected MessageFormat resolveCode(String code, Locale locale) {
		for (int i = 0; i < this.basenames.length; i++) {
			List filenames = calculateAllFilenames(this.basenames[i], locale);
			for (int j = 0; j < filenames.size(); j++) {
				String filename = (String) filenames.get(j);
				PropertiesHolder propHolder = getProperties(filename);
				if (propHolder.getProperties() != null) {
					MessageFormat result = propHolder.getMessageFormat(code, locale);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Calculate all filenames for the given bundle basename and Locale.
	 * Will calculate filenames for the given Locale, the system Locale
	 * (if applicable), and the default file.
	 * @param basename the basename of the bundle
	 * @param locale the locale
	 * @return the List of filenames to check
	 * @see #setFallbackToSystemLocale
	 * @see #calculateFilenamesForLocale
	 */
	protected List calculateAllFilenames(String basename, Locale locale) {
		synchronized (this.cachedFilenames) {
			Map localeMap = (Map) this.cachedFilenames.get(basename);
			if (localeMap != null) {
				List filenames = (List) localeMap.get(locale);
				if (filenames != null) {
					return filenames;
				}
			}
			List filenames = new ArrayList(7);
			filenames.addAll(calculateFilenamesForLocale(basename, locale));
			if (this.fallbackToSystemLocale && !locale.equals(Locale.getDefault())) {
				filenames.addAll(calculateFilenamesForLocale(basename, Locale.getDefault()));
			}
			filenames.add(basename);
			if (localeMap != null) {
				localeMap.put(locale, filenames);
			}
			else {
				localeMap = new HashMap();
				localeMap.put(locale, filenames);
				this.cachedFilenames.put(basename, localeMap);
			}
			return filenames;
		}
	}

	/**
	 * Calculate the filenames for the given bundle basename and Locale,
	 * appending language code, country code, and variant code.
	 * E.g.: basename "messages", Locale "de_AT_oo" -> "messages_de_AT_OO",
	 * "messages_de_AT", "messages_de".
	 * @param basename the basename of the bundle
	 * @param locale the locale
	 * @return the List of filenames to check
	 */
	protected List calculateFilenamesForLocale(String basename, Locale locale) {
		List result = new ArrayList(3);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		StringBuffer temp = new StringBuffer(basename);

		if (language.length() > 0) {
			temp.append('_').append(language);
			result.add(0, temp.toString());
		}

		if (country.length() > 0) {
			temp.append('_').append(country);
			result.add(0, temp.toString());
		}

		if (variant.length() > 0) {
			temp.append('_').append(variant);
			result.add(0, temp.toString());
		}

		return result;
	}

	/**
	 * Get PropertiesHolder for the given filename, either from the cache
	 * or freshly loaded.
	 */
	protected PropertiesHolder getProperties(String filename) {
		synchronized (this.cachedProperties) {
			PropertiesHolder propHolder = (PropertiesHolder) this.cachedProperties.get(filename);
			if (propHolder != null &&
					(propHolder.getRefreshTimestamp() < 0 ||
					 propHolder.getRefreshTimestamp() > System.currentTimeMillis() - this.cacheMillis)) {
				return propHolder;
			}
			else {
				return refreshProperties(filename, propHolder);
			}
		}
	}

	/**
	 * Refresh the PropertiesHolder for the given bundle filename.
	 * The holder can be null if not cached before, or a timed-out cache entry
	 * (potentially getting re-validated against the current last-modified timestamp).
	 */
	protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
		long refreshTimestamp = (this.cacheMillis < 0) ? -1 : System.currentTimeMillis();
		Resource resource = this.resourceLoader.getResource(filename + PROPERTIES_SUFFIX);
		try {
			long fileTimestamp = -1;
			if (this.cacheMillis >= 0) {
				// last-modified timestamp of file will just be read if caching with timeout
				// (allowing to use classpath resources if caching forever)
				fileTimestamp = resource.getFile().lastModified();
				if (fileTimestamp == 0) {
					throw new IOException("File [" + resource.getFile().getAbsolutePath() + "] does not exist");
				}
				if (propHolder != null && propHolder.getFileTimestamp() == fileTimestamp) {
					if (logger.isDebugEnabled()) {
						logger.debug("Re-caching properties for filename [" + filename + "] - file hasn't been modified");
					}
					propHolder.setRefreshTimestamp(refreshTimestamp);
					return propHolder;
				}
			}
			InputStream is = resource.getInputStream();
			Properties props = new Properties();
			try {
				String charset = null;
				if (this.fileEncodings != null) {
					charset = this.fileEncodings.getProperty(filename);
				}
				if (charset == null) {
					charset = this.defaultEncoding;
				}
				if (charset != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties for filename [" + filename + "] with charset '" + charset + "'");
					}
					this.propertiesPersister.load(props, new InputStreamReader(is, charset));
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties for filename [" + filename + "]");
					}
					this.propertiesPersister.load(props, is);
				}
				propHolder = new PropertiesHolder(props, fileTimestamp);
			}
			finally {
				is.close();
			}
		}
		catch (IOException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Properties file [" + filename + "] not found for MessageSource: " + ex.getMessage());
			}
			// empty holder representing "not found"
			propHolder = new PropertiesHolder();
		}
		propHolder.setRefreshTimestamp(refreshTimestamp);
		this.cachedProperties.put(filename, propHolder);
		return propHolder;
	}

	/**
	 * Clear the resource bundle cache.
	 * Following resolve calls will lead to reloading of the properties files.
	 */
	public void clearCache() {
		logger.info("Clearing resource bundle cache");
		synchronized (this.cachedProperties) {
			this.cachedProperties.clear();
		}
	}

	/**
	 * Clear the resource bundle caches of this MessageSource and all its ancestors.
	 * @see #clearCache
	 */
	public void clearCacheIncludingAncestors() {
		clearCache();
		if (getParentMessageSource() instanceof ReloadableResourceBundleMessageSource) {
			((ReloadableResourceBundleMessageSource) getParentMessageSource()).clearCacheIncludingAncestors();
		}
	}

	public String toString() {
		return getClass().getName() + ": basenames=[" + StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}


	/**
	 * PropertiesHolder for caching.
	 * Stores the last-modified timestamp of the source file for efficient
	 * change detection, and the timestamp of the last refresh attempt
	 * (updated every time the cache entry gets re-validated).
	 */
	protected class PropertiesHolder {

		private Properties properties;

		private long fileTimestamp = -1;

		private long refreshTimestamp = -1;

		/** Cache to hold already generated MessageFormats per message code */
		private final Map cachedMessageFormats = new HashMap();

		protected PropertiesHolder(Properties properties, long fileTimestamp) {
			this.properties = properties;
			this.fileTimestamp = fileTimestamp;
		}

		protected PropertiesHolder() {
		}

		protected Properties getProperties() {
			return properties;
		}

		protected long getFileTimestamp() {
			return fileTimestamp;
		}

		protected void setRefreshTimestamp(long refreshTimestamp) {
			this.refreshTimestamp = refreshTimestamp;
		}

		protected long getRefreshTimestamp() {
			return refreshTimestamp;
		}

		protected MessageFormat getMessageFormat(String code, Locale locale) {
			synchronized (this.cachedMessageFormats) {
				Map localeMap = (Map) this.cachedMessageFormats.get(code);
				if (localeMap != null) {
					MessageFormat result = (MessageFormat) localeMap.get(locale);
					if (result != null) {
						return result;
					}
				}
				String msg = this.properties.getProperty(code);
				if (msg != null) {
					if (localeMap == null) {
						localeMap = new HashMap();
						this.cachedMessageFormats.put(code, localeMap);
					}
					MessageFormat result = createMessageFormat(msg, locale);
					localeMap.put(locale, result);
					return result;
				}
				return null;
			}
		}
	}

}
