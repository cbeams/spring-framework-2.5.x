package org.springframework.web.servlet.view.tiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;

/**
 * @author Juergen Hoeller
 * @since 22.08.2003
 */
public class TestComponentController extends ComponentControllerSupport {

	protected void doPerform(ComponentContext componentContext, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("testAttr", "testVal");
	}

}
