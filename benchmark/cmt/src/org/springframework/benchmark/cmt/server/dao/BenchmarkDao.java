/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.dao;

import java.util.Collection;

import org.springframework.dao.DataAccessException;

import org.springframework.benchmark.cmt.data.Item;
import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.data.User;
import org.springframework.benchmark.cmt.server.NoSuchUserException;

/**
 * 
 * @author Rod Johnson
 */
public interface BenchmarkDao {
	
	/**
	 * Return the user or null
	 * @param id
	 * @return
	 * @throws DataAccessException
	 */
	User getUser(long id) throws DataAccessException;
	
	Collection getOrders(long userid) throws DataAccessException;
	
	// Enables us to find the stock level
	Item getItem(long id) throws DataAccessException;
	
	// Can throw an exception if the user isn't found and catch (DataIntegrity or something?)
	// We know stock is there
	// Decrement stock
	// Use sproc?
	void placeOrder(Order order) throws NoSuchUserException, DataAccessException; 

}
