
package org.springframework.web.servlet.mvc;

import junit.framework.TestCase;

import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rod Johnson
 * @since March 2, 2003
 * @version $Id: ParameterizableViewControllerTestSuite.java,v 1.2 2003-11-23 11:58:19 jhoeller Exp $
 */
public class ParameterizableViewControllerTestSuite extends TestCase {

	public void testPropertyNotSet() throws Exception {
		ParameterizableViewController pvc = new ParameterizableViewController();
		try {
			pvc.initApplicationContext();
			fail("should require viewName property to be set");
		}
		catch (IllegalArgumentException ex){
			// ok
		}
	}
	
	public void testModelIsCorrect() throws Exception {
		String viewName = "viewName";
		ParameterizableViewController pvc = new ParameterizableViewController();
		pvc.setViewName(viewName);
		pvc.initApplicationContext();
		// We don't care about the params
		ModelAndView mv = pvc.handleRequest(new MockHttpServletRequest(null, "GET", "foo.html"), null);
		assertTrue("model has no data", mv.getModel().size() == 0);
		assertTrue("model has correct viewname", mv.getViewName().equals(viewName));
		
		assertTrue("getViewName matches", pvc.getViewName().equals(viewName));
	}

}
