package org.springframework.autobuilds.jpetstore.web.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author darren davison
 * @since 27.5.2004
 */
public class HelpController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    String param = request.getParameter("param");
	    if (param == null || param.equals("")) param = "None supplied";
		return new ModelAndView("Help", "param", param);
	}

}
