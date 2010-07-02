/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

import java.rmi.RemoteException;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.benchmark.cmt.data.Item;
import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.data.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcHelper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * 
 * @author Rod Johnson
 */
public class LiveBenchmarkTests extends TestCase {
	
	private Benchmark benchmark;
	
	private JdbcHelper jh;
	
	public LiveBenchmarkTests(String s)  throws Exception{
		super(s);
		
		DataSource ds = new SingleConnectionDataSource("oracle.jdbc.OracleDriver", 
					"jdbc:oracle:thin:@ 192.168.0.233:1521:rj",
					"SYSTEM", "MANAGER", false);
		jh = new JdbcHelper(ds);
		//loadData(100, 1000);
	}
	
	protected void loadData(int users, int items) {
		jh.getTemplate().update("DELETE FROM ORDERS");
		jh.getTemplate().update("DELETE FROM ITEMS");
		jh.getTemplate().update("DELETE FROM USERS");
		for (int i = 0; i < users; i++) {
			String forename = "forename" + i;
			String surname = "surname" + i;
			jh.getTemplate().update("INSERT INTO USERS (ID, FORENAME, SURNAME)" +
					" VALUES (" + i + ", '" + forename + "', '" + surname + "')");
		}
		
		for (int i = 0; i < items; i++) {
			String name = "item" + i;
			int qty = 1000000;
			jh.getTemplate().update("INSERT INTO ITEMS (ID, NAME, STOCK)" +
					" VALUES (" + i + ", '" + name + "', '" + qty + "')");
		}
	}
	
	protected void setUp() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("test.xml", getClass()));
		benchmark = (Benchmark) bf.getBean("benchmark");
	}
	
	
	public void testCantGetNonexistentUser() throws RemoteException {
		long id = -1;
		
		try {
			benchmark.getUser(id);
			fail();
		}
		catch (NoSuchUserException ex) {
			// Ok
			assertEquals(id, ex.getId());
		}
	}
	
	public void testGetValidUser() throws NoSuchUserException, RemoteException {
		long id = 1;
		User user = new User("forename" + id, "surname" + id);
		User u = benchmark.getUser(id);
		assertEquals(user, u);
	}
	
	
	/**
	 * Must throw NoSuchUser
	 * @throws NoSuchUserException
	 */
	public void testGetOrdersForNoSuchUser() throws NoSuchUserException, RemoteException {
		long id = -1;
		try {
			benchmark.getOrdersByUser(id);
			fail();
		}
		catch (NoSuchUserException ex) {
			// Ok
			assertEquals(id, ex.getId());
		}
	}

	
	public void testGetOrdersForValidUser() throws NoSuchUserException, RemoteException {
		long id = 1;
		Order[] orders = benchmark.getOrdersByUser(id);
		assertTrue(orders.length > 0);
		assertTrue(orders[0].getItemId() == 1);
		assertTrue(orders[0].getUserId() == 1);
	}
	
	public void testGetValidItem() throws NoSuchUserException, NoSuchItemException, RemoteException {
		long id = 1;
		Item item = benchmark.getItem(1);
		assertEquals("item" + id, item.getName());
		//assertEquals(1000, item.getStock());
		assertTrue(item.getStock() > 0);
	}
	
	public void testNoSuchItem() throws Exception {
		long id = -1;
		try {
			benchmark.getItem(id);
			fail();
		}
		catch (NoSuchItemException ex) {
			assertEquals(id, ex.getId());
		}
	}

	
	public void testValidUserOrdersValidItem() throws Exception {
		int uid = 1;
		int iid = 1;
		int oldOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		Order order = new Order(uid, iid, 5);
		benchmark.placeOrder(uid, order);
		assertEquals(iid, order.getItemId());
		assertEquals(uid, order.getUserId());
		int newOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		assertTrue(newOrderCount == oldOrderCount + 1);
	}
	
	public void testValidUserOrdersValidItemBeyondStockLimit() throws Exception {
		int uid = 1;
		int iid = 1;
		int oldOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		Order order = new Order(uid, iid, 50000000);
		try {
			benchmark.placeOrder(uid, order);
			fail();
		}
		catch (InsufficientStockException ex) {
			
		}
		int newOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		assertEquals(oldOrderCount, newOrderCount);
	}
	
	public void testNoSuchUserOrdersValidItem() throws Exception {
		int uid = -1;
		int iid = 1;
		int oldOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		Order order = new Order(uid, iid, 2);
		try {
			benchmark.placeOrder(uid, order);
			fail();
		}
		catch (NoSuchUserException ex) {
		
		}
		int newOrderCount = jh.runSQLFunction("SELECT COUNT(*) FROM ORDERS");
		assertEquals(oldOrderCount, newOrderCount);
	}

}
