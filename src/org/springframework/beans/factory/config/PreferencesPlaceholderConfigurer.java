package org.springframework.beans.factory.config;

import java.util.Properties;
import java.util.prefs.Preferences;

import org.springframework.beans.factory.InitializingBean;

/**
 * Subclass of PropertyPlaceholderConfigurer that supports J2SE 1.4's
 * Preferences API (java.util.prefs).
 *
 * <p>Tries to resolve placeholders as keys first in the user preferences,
 * then in the system preferences, then in this configurer's properties.
 * Thus, behaves like PropertyPlaceholderConfigurer if no corresponding
 * preferences defined.
 *
 * <p>Supports custom paths for the system and user preferences trees.
 * Uses the respective root nodes if not specified.
 *
 * @author Juergen Hoeller
 * @since 16.02.2004
 * @see #setSystemTreePath
 * @see #setUserTreePath
 * @see java.util.prefs.Preferences
 */
public class PreferencesPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

	private String systemTreePath;

	private String userTreePath;

	private Preferences systemPrefs;

	private Preferences userPrefs;

	/**
	 * Set the path in the system preferences tree to use for resolving
	 * placeholders. Uses the root node by default.
	 */
	public void setSystemTreePath(String systemTreePath) {
		this.systemTreePath = systemTreePath;
	}

	/**
	 * Set the path in the system preferences tree to use for resolving
	 * placeholders. Uses the root node by default.
	 */
	public void setUserTreePath(String userTreePath) {
		this.userTreePath = userTreePath;
	}

	/**
	 * This implementation eagerly fetches the Preferences instances
	 * for the required system and user tree nodes.
	 */
	public void afterPropertiesSet() {
		this.systemPrefs = (this.systemTreePath != null) ?
		    Preferences.systemRoot().node(this.systemTreePath) : Preferences.systemRoot();
		this.userPrefs = (this.userTreePath != null) ?
		    Preferences.userRoot().node(this.userTreePath) : Preferences.userRoot();
	}

	/**
	 * This implementation tries to resolve placeholders as keys first
	 * in the user preferences, then in the system preferences, then in
	 * the passed-in properties.
	 */
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String value = this.userPrefs.get(placeholder, null);
		if (value == null) {
			value = this.systemPrefs.get(placeholder, null);
			if (value == null) {
				value = props.getProperty(placeholder);
			}
		}
		return value;
	}

}
