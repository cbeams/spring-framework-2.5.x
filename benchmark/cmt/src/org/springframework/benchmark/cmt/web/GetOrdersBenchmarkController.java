/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.benchmark.cmt.web;

import javax.servlet.ServletException;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * @author Rod Johnson
 */
public class GetOrdersBenchmarkController extends AbstractBenchmarkController {

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView run(Benchmark bm) throws Exception {
		long uid = randomIndex(USERS) + 1;
		long st = System.currentTimeMillis();
		try {
			Order[] orders = bm.getOrdersByUser(uid);
			long t = System.currentTimeMillis() - st;
			OrdersBean ordersBean = new OrdersBean(orders, bm.getClass().getName(), t);
			return new ModelAndView("orders.jsp", "orders", ordersBean);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new ServletException(ex);

		}	
	}

}