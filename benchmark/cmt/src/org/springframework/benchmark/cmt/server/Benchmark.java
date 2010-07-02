/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

import java.rmi.RemoteException;

import org.springframework.dao.DataAccessException;

import org.springframework.benchmark.cmt.data.Item;
import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.data.User;

/**
 * Note: has to throw RemoteException to allow remote EJB
 * @author Rod Johnson
 */
public interface Benchmark {
	
	User getUser(long id) throws NoSuchUserException, DataAccessException, RemoteException; 
	
	Order[] getOrdersByUser(long userid) throws NoSuchUserException, DataAccessException, RemoteException;
	
	Item getItem(long id) throws NoSuchItemException, DataAccessException, RemoteException;
	
	void placeOrder(long userid, Order order) throws NoSuchUserException, NoSuchItemException, InsufficientStockException, DataAccessException, RemoteException; 
	
	// Doesn't hit the database
	void doVeryLittle() throws RemoteException;
	
	/**
	 * Return time waited in milliseconds
	 * @param millis
	 * @return
	 * @throws RemoteException
	 */
	int waitUpToMillis(int millis) throws RemoteException;

}
