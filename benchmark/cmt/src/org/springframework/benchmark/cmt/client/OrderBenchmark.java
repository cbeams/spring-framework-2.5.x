/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import org.springframework.benchmark.cmt.data.Order;

/**
 * 
 * @author Rod Johnson
 */
public class OrderBenchmark extends AbstractBenchmark {

	protected void runPass(int i) throws Exception {
		long uid = randomIndex(USERS) + 1;
		long iid = randomIndex(ITEMS) + 1;
		int qty = randomIndex(100) + 1;
		//int oldOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		Order order = new Order(uid, iid, qty);
		factory.getBenchmark().placeOrder(uid, order);
		assertEquals("Item id matches", iid, order.getItemId());
		assertEquals("User id matches", uid, order.getUserId());
	}
	
}
