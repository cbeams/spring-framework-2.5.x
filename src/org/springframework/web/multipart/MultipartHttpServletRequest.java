package org.springframework.web.multipart;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface which provides additional methods for dealing with multipart content
 * within a servlet request.
 *
 * <p>A concrete implementation is DefaultMultipartHttpServletRequest.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29-Sep-2003
 * @see org.springframework.web.multipart.MultipartResolver
 * @see org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
 */
public interface MultipartHttpServletRequest extends HttpServletRequest {

	/**
	 * Return an Iterator of String objects containing the parameter names of the
	 * multipart files contained in this request.  These are the names reference
	 * in the request, not the actual filenames.
	 * @return the names of the files
	 */
	Iterator getFileNames();

	/**
	 * Return the contents/description of an uploaded file in this request, or
	 * null if it does not exist.
	 * @param name a String specifying the parameter name of the multipart file
	 * @return the uploaded content 
	 */
	MultipartFile getFile(String name);

	/**
	 * Return a Map of the multipart files contained in this request.
	 * @return a map containing the parameter names as the key, and the multipart
	 * file as the value.
	 */
	Map getFileMap();

}
