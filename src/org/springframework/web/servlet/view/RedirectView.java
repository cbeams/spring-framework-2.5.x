package org.springframework.web.servlet.view;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * View that redirects to an internal or external URL,
 * exposing all model attributes as HTTP query parameters.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: RedirectView.java,v 1.2 2003-11-19 15:31:09 dkopylenko Exp $
 */
public class RedirectView extends AbstractView {

	public static final String DEFAULT_ENCODING_SCHEME = "UTF-8";

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;

	private String url;

	public RedirectView() {
	}

	public RedirectView(String url) {
		setUrl(url);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected String getUrl() {
		return url;
	}

	/**
	 * Get encodingScheme
	 * @return String representing encoding scheme
     */
	public String getEncodingScheme() {
		return this.encodingScheme;
	}

	/**
	 * Set encodingScheme
	 * @param String representing encoding scheme
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}

	/**
	 * Subclasses can override this method to return name-value pairs for query strings,
	 * which will be URLEncoded and formatted by this class.
	 * This implementation tries to stringify all model elements.
	 */
	protected Map queryProperties(Map model) {
		return model;
	}

	/**
	 * Convert model to request parameters and redirect to url.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		if (getUrl() == null)
			throw new ServletException("RedirectView is not configured: URL cannot be null");

		StringBuffer url = new StringBuffer(getUrl());

		// If there are not already some parameters, we need a ?
		boolean first = (getUrl().indexOf('?') < 0);

		Iterator entries = queryProperties(model).entrySet().iterator();
		while (entries.hasNext()) {
			if (first) {
				url.append("?");
				first = false;
			}
			else {
				url.append("&");
			}

			Map.Entry entry = (Map.Entry)entries.next();

			url.append(URLEncoder.encode(entry.getKey().toString(), this.encodingScheme));
			url.append("=");
			url.append(URLEncoder.encode(entry.getValue().toString(), this.encodingScheme));
		}

		response.sendRedirect(response.encodeRedirectURL(url.toString()));
	}

}
