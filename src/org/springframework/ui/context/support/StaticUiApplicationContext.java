package org.springframework.ui.context.support;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;

/**
 * Static ApplicationContext implementation that adds theme capabilities
 * for UI contexts.
 * @author Jean-Pierre Pawlak
 */
public class StaticUiApplicationContext extends StaticApplicationContext implements ThemeSource {

	private ThemeSource themeSource;

	/**
	 * Standard constructor.
	 */
	public StaticUiApplicationContext()	{
		super();
	}

	/**
	 * Constructor with parent context.
	 */
	public StaticUiApplicationContext(ApplicationContext parent) {
		super(parent);
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
