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
 *
 * <p>A URL for this view is supposed to be a HTTP redirect URL,
 * i.e. suitable for HttpServletResponse's sendRedirect method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: RedirectView.java,v 1.4 2003-12-15 08:33:57 jhoeller Exp $
 * @see javax.servlet.http.HttpServletResponse#sendRedirect
 */
public class RedirectView extends AbstractUrlBasedView {

	public static final String DEFAULT_ENCODING_SCHEME = "UTF-8";

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;

	/**
	 * Constructor for use as a bean.
	 */
	public RedirectView() {
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * @param url the URL to redirect to
	 */
	public RedirectView(String url) {
		setUrl(url);
	}

	/**
	 * Set the encoding scheme for this view.
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}

	/**
	 * Return the encoding scheme for this view.
   */
	protected String getEncodingScheme() {
		return this.encodingScheme;
	}

	/**
	 * Convert model to request parameters and redirect to the given URL.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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
			String encodedKey = URLEncoder.encode(entry.getKey().toString());
			String encodedValue = (entry.getValue() != null ? URLEncoder.encode(entry.getValue().toString()) : "");
			url.append(new String(encodedKey.getBytes(this.encodingScheme), this.encodingScheme));
			url.append("=");
			url.append(new String(encodedValue.getBytes(this.encodingScheme), this.encodingScheme));
		}
		response.sendRedirect(response.encodeRedirectURL(url.toString()));
	}

	/**
	 * Subclasses can override this method to return name-value pairs for query strings,
	 * which will be URLEncoded and formatted by this class.
	 * This implementation tries to stringify all model elements.
	 */
	protected Map queryProperties(Map model) {
		return model;
	}

}
