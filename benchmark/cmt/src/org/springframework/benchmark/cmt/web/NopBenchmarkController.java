/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.benchmark.cmt.web;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * @author Rod Johnson
 */
public class NopBenchmarkController extends AbstractBenchmarkController {

	private int invocations;

	public void setInvocations(int invocations) {
		this.invocations = invocations;
	}

	protected void runPass(Benchmark bm) throws Exception {

		for (int i = 0; i < invocations; i++) {
			bm.doVeryLittle();
		}
	}

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView run(Benchmark benchmark) throws Exception {

		runPass(benchmark);

		return new ModelAndView("nop.jsp");
	}

}