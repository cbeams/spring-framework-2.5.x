package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that transforms the file name at the end of a URL to view name.
 * Example: "/index.html" -> "index"
 * @author Alef Arendsen
 */
public class FileNameViewController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
		int begin = uri.lastIndexOf('/');
		if (begin == -1) {
			begin = 0;
		}
		else {
			begin++;
		}
		int end;
		if (uri.indexOf(";") != -1) {
			end = uri.indexOf(";");
		}
		else if (uri.indexOf("?") != -1) {
			end = uri.indexOf("?");
		}
		else {
			end = uri.length();
		}
		String fileName = uri.substring(begin, end);
		if (fileName.indexOf(".") != -1) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		return new ModelAndView(fileName);
	}

}
