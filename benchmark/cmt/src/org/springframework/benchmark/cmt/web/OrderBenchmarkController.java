/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.web;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 */
public class OrderBenchmarkController extends AbstractBenchmarkController {

protected void order(Benchmark benchmark) throws Exception {
		long uid = randomIndex(USERS) + 1;
		long iid = randomIndex(ITEMS) + 1;
		int qty = randomIndex(100) + 1;
		//int oldOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		Order order = new Order(uid, iid, qty);
		benchmark.placeOrder(uid, order);
		//assertEquals("Item id matches", iid, order.getItemId());
		//assertEquals("User id matches", uid, order.getUserId());
	}

	/**
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView run(Benchmark benchmark) throws Exception {

		order(benchmark);
		return new ModelAndView("order.jsp");
		
	}

}
