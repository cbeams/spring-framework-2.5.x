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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.HierarchicalThemeSource;
import org.springframework.ui.context.ThemeSource;

/**
 * Utilities common to all UI application context implementations.
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 */
public abstract class UiApplicationContextUtils {

	/**
	 * Name of the ThemeSource bean in the factory.
	 * If none is supplied, theme resolution is delegated to the parent.
	 * @see org.springframework.ui.context.ThemeSource
	 */
	public static final String THEME_SOURCE_BEAN_NAME = "themeSource";

	private static final Log logger = LogFactory.getLog(UiApplicationContextUtils.class);

	/**
	 * Initialize the ThemeSource for the given application context,
	 * auto-detecting a bean with the name "themeSource". If no such
	 * bean is found, a default (empty) ThemeSource will be used.
	 * @param context current application context
	 * @return the initialized theme source (will never be null)
	 * @see #THEME_SOURCE_BEAN_NAME
	 */
	public static ThemeSource initThemeSource(ApplicationContext context) {
		ThemeSource themeSource;
		try {
			themeSource = (ThemeSource) context.getBean(THEME_SOURCE_BEAN_NAME);
			// set parent theme source if applicable,
			// and if the theme source is defined in this context, not in a parent
			if (context.getParent() instanceof ThemeSource &&
					themeSource instanceof HierarchicalThemeSource &&
					Arrays.asList(context.getBeanDefinitionNames()).contains(THEME_SOURCE_BEAN_NAME)) {
				((HierarchicalThemeSource) themeSource).setParentThemeSource((ThemeSource) context.getParent());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.info("No ThemeSource found for [" + context.getDisplayName() +
									"]: using ResourceBundleThemeSource");
			themeSource = new ResourceBundleThemeSource();
		}
		return themeSource;
	}

}
