package org.springframework.web.servlet.view;

/**
 * Abstract base class for URL-based views. Provides a consistent way of
 * holding the URL that a View wraps, in the form of a "url" bean property.
 * @author Juergen Hoeller
 * @since 13.12.2003
 */
public abstract class AbstractUrlBasedView extends AbstractView {

	private String url;

	/**
	 * Set the URL of the resource that this view wraps.
	 * The URL must be appropriate for the concrete View implementation.
	 */
	public final void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Return the URL of the resource that this view wraps.
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Overridden lifecycle method to check that 'url' property is set.
	 */
	protected void initApplicationContext() throws IllegalArgumentException {
		if (this.url == null) {
			throw new IllegalArgumentException("url is required");
		}
	}

}
