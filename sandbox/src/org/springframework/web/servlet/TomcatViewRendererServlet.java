package org.springframework.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>When redirecting from a POSTed request to a Servlet with Portlet compliant HTTPRequestWrappers,
 * Tomcat fails to process the request unless the the Servlet also supports the Post method.</p>
 *
 * <p>Note that under versions of Tomcat up to at least 5.0.27 you will get a NullPointerException
 * inside Tomcat rather than a better MethodNotSupported 500 error. Fixing bug http://issues.apache.org/bugzilla/show_bug.cgi?id=30746
 * would fix this.</p>
 *
 * <p>This Servlet should be used in at least the Pluto and EXO portlets</p>
 *
 * @author Nick Lothian
 */
public class TomcatViewRendererServlet extends org.springframework.web.servlet.ViewRendererServlet {

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
    }
}
