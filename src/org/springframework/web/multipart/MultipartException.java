package org.springframework.web.multipart;

import javax.servlet.ServletException;

/**
 * Base class for all multipart exceptions.
 * @author Trevor D. Cook
 * @since 29-Sep-2003
 */
public class MultipartException extends ServletException {

	public MultipartException() {
		super();
	}

	public MultipartException(String s) {
		super(s);
	}

	public MultipartException(String s, Throwable t) {
		super(s, t);
	}

	public MultipartException(Throwable t) {
		super(t);
	}

}
