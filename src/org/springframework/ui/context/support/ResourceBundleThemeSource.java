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

package org.springframework.ui.context.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.context.HierarchicalThemeSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;

/**
 * ThemeSource implementation that looks up an individual ResourceBundle
 * per theme. The theme name gets interpreted as ResourceBundle basename,
 * supporting a common basename prefix for all themes.
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see #setBasenamePrefix
 */
public class ResourceBundleThemeSource implements HierarchicalThemeSource {

	protected final Log logger = LogFactory.getLog(getClass());

	private ThemeSource parentThemeSource;

	private String basenamePrefix = "";

	/** Map from theme name to Theme instance */
	private Map themes = new HashMap();

	public void setParentThemeSource(ThemeSource parent) {
		this.parentThemeSource = parent;
		Iterator it = this.themes.values().iterator();
		while (it.hasNext()) {
			initParent((Theme) it.next());
		}
	}

	public ThemeSource getParentThemeSource() {
		return parentThemeSource;
	}

	/**
	 * Set the prefix that gets applied to the ResourceBundle basenames,
	 * i.e. the theme names.
	 * E.g.: basenamePrefix="test.", themeName="theme" -> basename="test.theme".
	 * @param basenamePrefix prefix for ResourceBundle basenames
	 */
	public void setBasenamePrefix(String basenamePrefix) {
		this.basenamePrefix = (basenamePrefix != null) ? basenamePrefix : "";
	}

	public Theme getTheme(String themeName) {
		if (themeName == null) {
			return null;
		}
		Theme theme = (Theme) this.themes.get(themeName);
		if (theme == null) {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			logger.info("Theme created: name=" + themeName + ", baseName=" + this.basenamePrefix + themeName);
			messageSource.setBasename(this.basenamePrefix + themeName);
			theme = new SimpleTheme(themeName, messageSource);
			initParent(theme);
			this.themes.put(themeName, theme);
		}
		return theme;
	}

	/**
	 * Initialize the MessageSource of the given theme with the
	 * one from the respective parentThemeSource of this ThemeSource.
	 */
	protected void initParent(Theme theme) {
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) theme.getMessageSource();
		if (this.parentThemeSource != null) {
			Theme parentTheme = this.parentThemeSource.getTheme(theme.getName());
			if (parentTheme != null) {
				messageSource.setParentMessageSource(parentTheme.getMessageSource());
			}
		}
		else {
			messageSource.setParentMessageSource(null);
		}
	}

}
