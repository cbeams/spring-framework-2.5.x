/*
 * Spring framework demo
 */
 
package example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Juergen Hoeller
 * @author Rod Johnson
 */
public class ExampleController implements Controller {

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(
		HttpServletRequest request,
		HttpServletResponse response)
		throws ServletException, IOException {
		
		return new ModelAndView("/test.jsp");
	}

}
