/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.load.http;

import java.io.IOException;

import org.springframework.load.AbortTestException;
import org.springframework.load.AbstractTest;
import org.springframework.load.TestFailedException;

import HTTPClient.CookieModule;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;

/**
 * Simple: presently there's no support for query strings etc, although this
 * wouldn't be hard to add.
 * @author Rod Johnson
 */
public class HttpTest extends AbstractTest {

	/**
	 * The URL, relative to the base of the server. Doesn't need to be prefixed by a /.
	 */
	private String page;
	
	private int port = 80;
	
	private String host;
	
	private int bytesReceived;
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getBytesReceived() {
		return this.bytesReceived;
	}
	
	public int getAverageBytesReceived() {
		return this.bytesReceived / getTestsCompletedCount();
	}
	
	public String toString() {
		return host + ":" + port + "/" + page + "; " + 
			bytesReceived + " bytesReceived, avg=" + getAverageBytesReceived() + 
			": " + super.toString();
	}

	/**
	 * Returns the page.
	 * 
	 * @return String
	 */
	public String getPage() {
		return page;
	}

	/**
	 * Sets the page.
	 * 
	 * @param page
	 *            The page to set
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * If this property is set to false, cookies will be accepted automatically. Default behaviour is to throw up a
	 * modal popup asking whether the user wants to accept cookies.
	 */
	public void setShowCookies(boolean flag) {
		if (!flag) {
			CookieModule.setCookiePolicyHandler(null);
			logger.info("Cookies will be accepted automatically");
		}
	}


	/**
	 * Get the page's data
	 */
	private byte[] requestPage(String baseUrl, int port, String page) throws TestFailedException {

		byte[] data = null;
		try {
			logger.debug("Trying HTTP connection to host '" + baseUrl + "' on port " + port);
			HTTPConnection con = new HTTPConnection(baseUrl, port);
			logger.debug("Requesting page '" + page + "'");
			HTTPResponse rsp = con.Get(page);
			if (rsp.getStatusCode() >= 300) {
				System.err.println("Received Error: " + rsp.getReasonLine());
				//System.err.println(rsp.getText());
					throw new TestFailedException("Received HTTP error response code " + rsp.getStatusCode() + ": " + " error description was '"
						+ rsp.getReasonLine() + "'");
			}
			else {
				// No error: assume 200
				data = rsp.getData();
				if (logger.isInfoEnabled()) {
					logger.info("Data is " + new String(data));
				}		}

			return data;
		}
		catch (IOException ioe) {
			System.err.println(ioe.toString());
			throw new TestFailedException( "HTTP Error", ioe);
		}
		catch (ModuleException me) {
			System.err.println("Error handling request: " + me.getMessage());
			throw new TestFailedException( "HTTP Error", me);
		}

	}

	/**
	 * We override this superclass method to apply the rule that the Host URL is the name if no name is specified.
	 * 
	 * @see com.lch.itstrategy.testing.ist.Test#getName()
	 */
//	public String getName() {
//		if (super.getName() != null)
//			return super.getName();
//		if (getHost() == null)
//			return null;
//		// Default
//		return getHost().getUrl() + ":" + getHost().getWebPort();
//	}

	/**
	 * @see org.springframework.load.AbstractTest#runPass(int)
	 */
	protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		byte[] data = requestPage(host, port, page);
		logger.debug("Page size was " + data.length);
		bytesReceived += data.length;

		// Assertions?
		assertCorrect(data);
	}
	
	/**
	 * Check that data is valid
	 * @param data
	 */
	protected void assertCorrect(byte[] data) {
		
	}

}