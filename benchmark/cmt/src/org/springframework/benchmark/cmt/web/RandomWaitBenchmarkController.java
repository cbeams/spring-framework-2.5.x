/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.benchmark.cmt.web;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * Eliminates DB access time
 * @author Rod Johnson
 * @version $Id: RandomWaitBenchmarkController.java,v 1.1 2003-12-02 18:31:08 johnsonr Exp $
 */
public class RandomWaitBenchmarkController extends AbstractBenchmarkController {
	
	private int maxMillis = 40;
	
	public void setMaxMillis(int maxMillis) {
		this.maxMillis = maxMillis;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView run(Benchmark bm) throws Exception {
		int t = bm.waitUpToMillis(maxMillis);
		return new ModelAndView("wait.jsp", "time", t + "");
	}

}