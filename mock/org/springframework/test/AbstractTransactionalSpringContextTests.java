/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Convenient superclass for tests that should occur in a transaction, but normally
 * will roll the transaction back on the completion of each test.
 *
 * <p>This is useful in a range of circumstances, allowing the following benefits:
 * <ul>
 * <li>Ability to delete or insert any data in the database, without affecting other tests
 * <li>Providing a transactional context for any code requiring a transaction
 * <li>Ability to write anything to the database without any need to clean up.
 * </ul>
 *
 * <p>This class is typically very fast, compared to traditional setup/teardown scripts.
 *
 * <p>If data should be left in the database, call the setComplete() method in each test.
 * The defaultRollback() property, which defaults to true, determines whether
 * transactions will complete by default.
 *
 * Requires a single bean in the context implementing the PlatformTransactionManager
 * interface. This will be set by the superclass's Dependency Injection mechanism.
 * If using the superclass's Field Injection mechanism, the implementation should be
 * named "transactionManager". This mechanism allows the use of this superclass even
 * when there's more than one transaction manager in the context.
 *
 * @author Rod Johnson
 * @since 1.1.1
 */
public abstract class AbstractTransactionalSpringContextTests
    extends AbstractDependencyInjectionSpringContextTests {

	/**
	 * Should we commit this transction?
	 */
	private boolean complete;

	/**
	 * TransactionStatus for this test. Subclasses probably won't need to use it.
	 */
	protected TransactionStatus transactionStatus;

	protected PlatformTransactionManager transactionManager;

	private boolean defaultRollback = true;


	/**
	 * Subclasses can set this value in their constructor to change
	 * default, which is always to roll the transaction back
	 */
	public void setDefaultRollback(boolean defaultRollback) {
		this.defaultRollback = defaultRollback;
	}

	/**
	 * Populated by dependency injection by superclass.
	 */
	public void setTransactionManager(PlatformTransactionManager ptm) {
		this.transactionManager = ptm;
	}

	protected final void onSetUp() throws Exception {
		complete = !defaultRollback;

		// Start a transaction
		this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

		// this.jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");

		logger.info("Began transaction: transaction manager=[" + this.transactionManager +
		    "]; defaultCommit=" + this.complete);

		onSetUpInTransaction();
	}

	protected void onSetUpInTransaction() throws Exception {
	}

	protected final void onTearDown() throws Exception {
		try {
			onTearDownInTransaction();
		}
		finally {
			if (!this.complete) {
				this.transactionManager.rollback(this.transactionStatus);
				logger.info("Rolled back transaction after test execution");
			}
			else {
				this.transactionManager.commit(this.transactionStatus);
				logger.info("Committed transaction after test execution");
			}
		}
	}

	/**
	 * Transaction is still open.
	 * Subclasses will typically run tests here.
	 */
	protected void onTearDownInTransaction() {
	}

	/**
	 * Cause the transaction to commit for this test method,
	 * even if default is set to rollback.
	 */
	protected void setComplete() {
		this.complete = true;
	}

}
