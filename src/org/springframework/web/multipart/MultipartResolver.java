package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for multipart resolution strategies that handle file uploads as
 * defined in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within any application context
 * and standalone.
 *
 * <p>There are two concrete implementations included in Spring:
 * <ul>
 * <li>CommonsMultipartResolver for Jakarta Commons FileUpload
 * <li>CosMultipartResolver for Jason Hunter's COS (com.oreilly.servlet)
 * </ul>
 *
 * <p>There is no default resolver implementation used for Spring DispatcherServlets,
 * as an application might choose to parse its multipart requests itself. To define an
 * implementation, create a bean with the id "multipartResolver" in a DispatcherServlet's
 * application context. Such a resolver gets applied to all requests handled by that
 * DispatcherServlet. Use RequestContextUtils.getMultipartResolver() to retrieve the
 * current resolver in controllers etc, independent of the actual resolution strategy.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.9.2003
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.cos.CosMultipartResolver
 * @see org.springframework.web.servlet.support.RequestContextUtils#getMultipartResolver
 */
public interface MultipartResolver {

	/**
	 * Determine if the request contains multipart content.
	 * <p>Will typically check for content type "multipart/form-data", but the actually
	 * accepted requests might depend on the capabilities of the resolver implementation.
	 * @param request the servlet request to be evaluated
	 * @return <code>true</code> if the request contains multipart content;
	 * <code>false</code> otherwise
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * Wrap the servlet request inside a MultipartHttpServletRequest.
	 * <p>In addition to being accessible from the methods defined in MultipartHttpServletRequest,
	 * multipart files should be retrievable as normal parameters (such as with
	 * getParameter(String)) in order to allow other Spring features such as binding
	 * to occur correctly. Multipart files retrieved as regular parameters must
	 * fulfil the contract <code>key == value</code>.  This will allow property editors
	 * to be able to access them as a standard String value in order to retrieve
	 * the appropriate MultipartFile.
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * @return the wrapped servlet request
	 * @throws org.springframework.web.multipart.MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file
	 * size limits)
	 */
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * Cleanup any resources used for the multipart handling,
	 * like a storage for the uploaded files.
	 * @param request the request to cleanup resources for
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
