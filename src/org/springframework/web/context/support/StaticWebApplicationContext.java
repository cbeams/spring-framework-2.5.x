package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Static WebApplicationContext implementation for testing.
 * Not for use in production applications.
 */
public class StaticWebApplicationContext extends StaticApplicationContext
		implements ConfigurableWebApplicationContext {

	private ServletContext servletContext;

	private String namespace;

	/** the ThemeSource for this ApplicationContext */
	private ThemeSource themeSource;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	protected String getNamespace() {
		return this.namespace;
	}

	public void setConfigLocations(String[] configLocations) {
		throw new UnsupportedOperationException("StaticWebApplicationContext does not support configLocations");
	}

	public void refresh() throws BeansException {
		if (this.namespace != null) {
			setDisplayName("StaticWebApplicationContext for namespace '" + this.namespace + "'");
		}
		else {
			setDisplayName("Root StaticWebApplicationContext");
		}
		super.refresh();
	}

	/**
	 * Initialize the theme capability.
	 */
	protected void onRefresh() {
		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	}

	public Theme getTheme(String themeName) {
		return this.themeSource.getTheme(themeName);
	}

}
