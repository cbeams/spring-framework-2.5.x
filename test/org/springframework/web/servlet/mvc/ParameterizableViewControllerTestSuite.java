
package org.springframework.web.servlet.mvc;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * @version $Id: ParameterizableViewControllerTestSuite.java,v 1.1.1.1 2003-08-14 16:21:28 trisberg Exp $
 * @author Rod Johnson
 * @since March 2, 2003
 * 
 */
public class ParameterizableViewControllerTestSuite extends TestCase {

	/**
	 * Constructor for ParameterizableViewControllerTestSuite.
	 * @param arg0
	 */
	public ParameterizableViewControllerTestSuite(String arg0) {
		super(arg0);
	}
	
	
	public void testPropertyNotSet() throws Exception {
		ParameterizableViewController pvc = new ParameterizableViewController();
		try {
			pvc.afterPropertiesSet();
			fail("should require viewName property to be set");
		}
		catch (ServletException ex){
			// ok
		}
	}
	
	public void testModelIsCorrect() throws Exception {
		String viewName = "viewName";
		ParameterizableViewController pvc = new ParameterizableViewController();
		pvc.setViewName(viewName);
		pvc.afterPropertiesSet();
		// We don't care about the params
		ModelAndView mv = pvc.handleRequest(new MockHttpServletRequest(null, "GET", "foo.html"), null);
		assertTrue("model has no data", mv.getModel().size() == 0);
		assertTrue("model has correct viewname", mv.getViewName().equals(viewName));
		
		assertTrue("getViewName matches", pvc.getViewName().equals(viewName));
	}

}
