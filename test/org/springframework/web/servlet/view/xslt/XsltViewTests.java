/*
 * XsltViewTests.java
 */

package org.springframework.web.servlet.view.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Darren Davison
 * @since 11.03.2005
 */
public class XsltViewTests extends TestCase {

	private TestXsltView view;

	private int warnings = 0;

	private int errors = 0;

	private int fatal = 0;

	public void setUp() {
		view = new TestXsltView();
	}

	private void incWarnings() {
		warnings++;
	}

	private void incErrors() {
		errors++;
	}

	private void incFatals() {
		fatal++;
	}

	public void testNoSuchStylesheet() {
		view.setStylesheetLocation(new FileSystemResource("/does/not/exist.xsl"));
		try {
			view.initApplicationContext();
			fail("Should have thrown ApplicationContextException");
		}
		catch (ApplicationContextException e) {
			// ok
		}
		catch (Exception e) {
			fail("Should have thrown ApplicationContextException");
		}
	}

	public void testChangeStylesheetReCachesTemplate() {
		view.setStylesheetLocation(new ClassPathResource("org/springframework/web/servlet/view/xslt/valid.xsl"));
		view.initApplicationContext();

		try {
			view.setStylesheetLocation(new FileSystemResource("/does/not/exist.xsl"));
			fail("Should throw ApplicationContextException on re-caching template");
		}
		catch (ApplicationContextException ex) {
			// ok
		}
	}

	public void testCustomErrorListener() {
		view.setErrorListener(new ErrorListener() {
			public void warning(TransformerException ex) {
				incWarnings();
			}
			public void error(TransformerException ex) {
				incErrors();
			}
			public void fatalError(TransformerException ex) {
				incFatals();
			}
		});

		// loaded stylesheet is not well formed
		view.setStylesheetLocation(new ClassPathResource("org/springframework/web/servlet/view/xslt/errors.xsl"));
		try {
			view.initApplicationContext();
		}
		catch (ApplicationContextException ex) {
			// shouldn't really happen, but can be let through by XSLT engine
			assertTrue(ex.getCause() instanceof TransformerException);
		}
		assertEquals(1, fatal);
		assertEquals(1, errors);
		assertEquals(0, warnings);
	}


	private static class TestXsltView extends AbstractXsltView {

	}

}
