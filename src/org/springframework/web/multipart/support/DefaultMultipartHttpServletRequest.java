package org.springframework.web.multipart.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the MultipartHttpServletRequest interface.
 * Provides management of pre-generated parameter values.
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29-Sep-2003
 * @see org.springframework.web.multipart.MultipartResolver
 */
public class DefaultMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private Map parameters;

	/**
	 * Create a wrapped HttpServletRequest.
	 * @param request the request to wrap
	 * @param parameters a map of the parameters,
	 * with Strings as keys and String arrays as values
	 * @param multipartFiles a map of the multipart files
	 */
	public DefaultMultipartHttpServletRequest(HttpServletRequest request, Map multipartFiles, Map parameters) {
		super(request);
		setMultipartFiles(multipartFiles);
		this.parameters = Collections.unmodifiableMap(parameters);
	}

	public Enumeration getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	public String getParameter(String name) {
		String[] values = getParameterValues(name);
		return (values != null && values.length > 0 ? values[0] : null);
	}

	public String[] getParameterValues(String name) {
		return (String[]) this.parameters.get(name);
	}

	public Map getParameterMap() {
		return this.parameters;
	}

}
