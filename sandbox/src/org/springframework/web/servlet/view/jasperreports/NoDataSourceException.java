/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class NoDataSourceException extends RuntimeException {

	/**
	 * 
	 */
	public NoDataSourceException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public NoDataSourceException(String msg) {
		super(msg);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public NoDataSourceException(String msg, Throwable rootCause) {
		super(msg, rootCause);
	}

	/**
	 * @param arg0
	 */
	public NoDataSourceException(Throwable rootCause) {
		super(rootCause);
	}

}
