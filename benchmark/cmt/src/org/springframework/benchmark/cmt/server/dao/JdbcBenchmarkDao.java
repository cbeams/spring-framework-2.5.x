/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.StoredProcedure;

import org.springframework.benchmark.cmt.data.Item;
import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.data.User;
import org.springframework.benchmark.cmt.server.NoSuchUserException;

/**
 * 
 * @author Rod Johnson
 */
public class JdbcBenchmarkDao implements BenchmarkDao {
	
	private OrderProc orderProc;
	
	private UserQuery userQuery;
	
	private OrderByUserQuery orderByUserQuery;
	
	private ItemQuery itemQuery;
	
	// TODO hack
	public DataSource ds;
	
	public JdbcBenchmarkDao(DataSource ds) {
		this.orderProc = new OrderProc(ds);
		this.userQuery = new UserQuery(ds);
		this.orderByUserQuery = new OrderByUserQuery(ds);
		this.itemQuery = new ItemQuery(ds);
		
		this.ds = ds;
	}


	/**
	 * @see org.springframework.benchmark.cmt.server.dao.BenchmarkDao#getUser(long)
	 */
	public User getUser(long id) throws DataAccessException {
		List l = userQuery.execute(id);
		if (l.size() == 0)
			return null;
		return (User) l.get(0);
	}
	
	/**
	 * @see org.springframework.benchmark.cmt.server.dao.BenchmarkDao#getOrders(long)
	 */
	public Collection getOrders(long userid) throws DataAccessException {
		
		//new Exception().printStackTrace();
		
		return this.orderByUserQuery.execute(userid);
	}
	

	/**
	 * @see org.springframework.benchmark.cmt.server.dao.BenchmarkDao#getItem(long)
	 */
	public Item getItem(long id) throws DataAccessException {
		List l = itemQuery.execute(id);
		if (l.size() == 0)
			return null;
		return (Item) l.get(0);
	}
	
	
	public void placeOrder(final Order order) throws DataAccessException, NoSuchUserException {
		try {
			this.orderProc.order(order);
		}
		catch (DataIntegrityViolationException ex) {
			// No parent user key found
			throw new NoSuchUserException(order.getUserId());
		}
	}
	
	private class UserQuery extends MappingSqlQuery {
		public UserQuery(DataSource ds) {
			super(ds, "SELECT FORENAME, SURNAME FROM USERS WHERE ID = ?");
			declareParameter(new SqlParameter(Types.NUMERIC));
		}
		/**
		 * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet, int)
		 */
		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new User(rs.getString("forename"), rs.getString("surname"));
		}
	}

	private abstract class AbstractOrderQuery extends MappingSqlQuery {
		public AbstractOrderQuery(DataSource ds, String sql) {
			super(ds, sql);
		}
		/**
		 * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet, int)
		 */
		protected final Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Order(rs.getLong("user_id"), rs.getLong("item_id"), rs.getInt("quantity"));
		}
	}
	
	private class OrderByUserQuery extends AbstractOrderQuery {
		public OrderByUserQuery(DataSource ds) {
			super(ds, "SELECT USER_ID, ITEM_ID, QUANTITY FROM ORDERS WHERE USER_ID = ?");
			declareParameter(new SqlParameter(Types.NUMERIC));
		}
	}

	private class ItemQuery extends MappingSqlQuery {
		public ItemQuery(DataSource ds) {
			super(ds, "SELECT NAME, STOCK FROM ITEMS WHERE ID = ?");
			declareParameter(new SqlParameter(Types.NUMERIC));
		}
		/**
		 * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet, int)
		 */
		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Item(rs.getString("name"), rs.getInt("stock"));
		}
	}
	
	private class OrderProc extends StoredProcedure {
		 public OrderProc(DataSource ds) {
		 	super(ds, "place_order");
		 	declareParameter(new SqlParameter("user_id", Types.NUMERIC));
			declareParameter(new SqlParameter("item_id", Types.NUMERIC));
			declareParameter(new SqlParameter("quantity", Types.NUMERIC));
			declareParameter(new SqlOutParameter("user_id", Types.NUMERIC));
			compile();
		 }
		 
		 public void order(Order order) {
		 	//new Exception().printStackTrace();
		 	
		 	
		 	HashMap in = new HashMap();
		 	in.put("user_id", new Long(order.getUserId()));
		 	in.put("item_id", new Long(order.getItemId()));
		 	in.put("quantity", new Integer(order.getQuantity()));
		 	Map out = execute(in);
		 	//return 
		 }
	}

}
