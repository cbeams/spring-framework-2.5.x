/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.pojo;

import java.util.Collection;
import java.util.Random;

import org.springframework.dao.DataAccessException;

import org.springframework.benchmark.cmt.data.Item;
import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.data.User;
import org.springframework.benchmark.cmt.server.Benchmark;
import org.springframework.benchmark.cmt.server.InsufficientStockException;
import org.springframework.benchmark.cmt.server.NoSuchItemException;
import org.springframework.benchmark.cmt.server.NoSuchUserException;
import org.springframework.benchmark.cmt.server.dao.BenchmarkDao;
import org.springframework.benchmark.cmt.server.dao.JdbcBenchmarkDao;

/**
 * 
 * @author Rod Johnson
 */
public class PojoBenchmark implements Benchmark {
	
	private BenchmarkDao dao;
	
	public PojoBenchmark(BenchmarkDao dao) {
	//	System.out.println("New POJO benchmark");
	System.out.println("POJO: DataSource is " + ((JdbcBenchmarkDao) dao).ds);
		this.dao = dao;
	}
	
	public PojoBenchmark() {
		//System.out.println("New POJO benchmark BEAN");
	}
	
	/**
	 * For subclasses
	 * @param dao
	 */
	public void setDao(BenchmarkDao dao) {
		this.dao = dao;
	}
	


	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#getUser(long)
	 */
	public User getUser(long id) throws NoSuchUserException, DataAccessException {
		User user = dao.getUser(id);
		if (user == null)
			throw new NoSuchUserException(id);
		return user;
	}

	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#getOrders(long)
	 */
	public Order[] getOrdersByUser(long userid) throws NoSuchUserException, DataAccessException {
		// Will throw NoSuchUserException if there's no user
		
		// TODO commented out to save extra round trip to the DB: not actually correct
		//User user = getUser(userid);
		
		Collection c = dao.getOrders(userid);
		return (Order[]) c.toArray(new Order[c.size()]);
	}

	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#placeOrder(long, org.springframework.benchmark.cmt.data.Item)
	 */
	public void placeOrder(long userid, Order order) throws NoSuchUserException, NoSuchItemException, InsufficientStockException, DataAccessException {
		// Can throw NoSuchItemException
		Item item = getItem(order.getItemId());
		
		if (item.getStock() < order.getQuantity()) {
			throw new InsufficientStockException(order.getItemId(), item.getStock(), order.getQuantity());
		}
		
		// If we get here we know the item is available
		dao.placeOrder(order);
	}

	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#doVeryLittle()
	 */
	public void doVeryLittle() {
		// Do nothing. Don't hit the database
	}
	
	
	// Share between all instances for fairness
	private static Random rand = new Random();
	
	public int waitUpToMillis(int millis) {
		try {
			int ms = rand.nextInt(millis);
			Thread.sleep(ms);
			return ms;
		}
		catch (InterruptedException e) {
			// Ignore
			return -1;
		}
	}

	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#getItem(long)
	 */
	public Item getItem(long id) throws NoSuchItemException, DataAccessException {
		Item item = dao.getItem(id);
		if (item == null)
			throw new NoSuchItemException(id);
		return item;
	}

}
