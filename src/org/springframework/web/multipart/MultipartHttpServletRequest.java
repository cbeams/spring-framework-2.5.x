package org.springframework.web.multipart;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface which provides additional methods for dealing with multipart content
 * within a servlet request, allowing to access uploaded files.
 *
 * <p>A concrete implementation is DefaultMultipartHttpServletRequest. As an
 * intermediate step, AbstractMultipartHttpServletRequest can be subclassed.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29-Sep-2003
 * @see MultipartResolver
 * @see MultipartFile
 * @see org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
 * @see org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest
 */
public interface MultipartHttpServletRequest extends HttpServletRequest {

	/**
	 * Return an Iterator of String objects containing the parameter names of the
	 * multipart files contained in this request. These are the field names of
	 * the form (like with normal parameters), not the original file names.
	 * @return the names of the files
	 */
	Iterator getFileNames();

	/**
	 * Return the contents/description of an uploaded file in this request,
	 * or null if it does not exist.
	 * @param name a String specifying the parameter name of the multipart file
	 * @return the uploaded content 
	 */
	MultipartFile getFile(String name);

	/**
	 * Return a Map of the multipart files contained in this request.
	 * @return a map containing the parameter names as keys, and the
	 * MultipartFile instances file as values
	 * @see MultipartFile
	 */
	Map getFileMap();

}
