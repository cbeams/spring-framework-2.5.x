/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server.pojo;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.springframework.benchmark.cmt.data.Order;
import org.springframework.benchmark.cmt.server.InsufficientStockException;
import org.springframework.benchmark.cmt.server.NoSuchItemException;
import org.springframework.benchmark.cmt.server.NoSuchUserException;
import org.springframework.benchmark.cmt.server.dao.BenchmarkDao;

/**
 * Transaction decorator, which avoids reliance on EJB or AOP declarative transactions.
 * @author Rod Johnson
 */
public class TxDecoratorPojoBenchmark extends PojoBenchmark {
	
	private PlatformTransactionManager txManager;

	public TxDecoratorPojoBenchmark(BenchmarkDao dao, PlatformTransactionManager txManager) {
		super(dao);
		this.txManager = txManager;
	}
	

	/**
	 * @see org.springframework.benchmark.cmt.server.Benchmark#placeOrder(long, org.springframework.benchmark.cmt.data.Order)
	 */
	public void placeOrder(long userid, Order order) throws NoSuchUserException, NoSuchItemException, InsufficientStockException  {
		TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
		try {
			super.placeOrder(userid, order);
			// Leave txStatus alone
		}
		catch (DataAccessException ex) {
			txStatus.setRollbackOnly();
			throw ex;
		}
		catch (NoSuchUserException ex) {
			txStatus.setRollbackOnly();
			throw ex;
		}
		catch (NoSuchItemException ex) {
			txStatus.setRollbackOnly();
			throw ex;
		}
		catch (InsufficientStockException ex) {
			txStatus.setRollbackOnly();
			throw ex;
		}
		finally {
			// Close transaction
			// May have been marked for rollback
			txManager.commit(txStatus);
		}
	}
}
