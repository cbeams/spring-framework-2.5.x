/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jca.endpoint;

import java.lang.reflect.Method;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.jta.SimpleTransactionFactory;
import org.springframework.transaction.jta.TransactionFactory;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public abstract class AbstractMessageEndpointFactory implements MessageEndpointFactory {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private TransactionFactory transactionFactory;

	private String transactionName;

	private int transactionTimeout;


	public void setTransactionManager(Object transactionManager) {
		if (transactionManager instanceof TransactionFactory) {
			this.transactionFactory = (TransactionFactory) transactionManager;
		}
		else if (transactionManager instanceof TransactionManager) {
			this.transactionFactory = new SimpleTransactionFactory((TransactionManager) transactionManager);
		}
		else {
			throw new IllegalArgumentException("Transaction manager [" + transactionManager +
					"] is neither a TransactionFactory nor a TransactionManager");
		}
	}

	public void setTransactionFactory(TransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}

	public void setTransactionTimeout(int transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}


	public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
		return (this.transactionFactory != null);
	}

	public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
		AbstractMessageEndpoint endpoint = createEndpointInternal();
		endpoint.initXAResource(xaResource);
		return endpoint;
	}

	protected abstract AbstractMessageEndpoint createEndpointInternal()
			throws UnavailableException;


	protected abstract class AbstractMessageEndpoint implements MessageEndpoint {

		private TransactionDelegate transactionDelegate;

		private boolean beforeDeliveryCalled = false;

		private ClassLoader previousContextClassLoader;

		void initXAResource(XAResource xaResource) {
			this.transactionDelegate = new TransactionDelegate(xaResource);
		}

		public void beforeDelivery(Method method) throws ResourceException {
			this.beforeDeliveryCalled = true;
			try {
				this.transactionDelegate.beginTransaction();
			}
			catch (Throwable ex) {
				throw new ApplicationServerInternalException("Failed to begin transaction", ex);
			}
			Thread currentThread = Thread.currentThread();
			this.previousContextClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getEndpointClassLoader());
		}

		protected abstract ClassLoader getEndpointClassLoader();

		protected boolean hasBeforeDeliveryBeenCalled() {
			return this.beforeDeliveryCalled;
		}

		protected void onEndpointException(Throwable ex) {
			this.transactionDelegate.setRollbackOnly();
		}

		public void afterDelivery() throws ResourceException {
			this.beforeDeliveryCalled = false;
			Thread.currentThread().setContextClassLoader(this.previousContextClassLoader);
			this.previousContextClassLoader = null;
			try {
				this.transactionDelegate.endTransaction();
			}
			catch (Throwable ex) {
				throw new ApplicationServerInternalException("Failed to complete transaction", ex);
			}
		}

		public void release() {
			try {
				this.transactionDelegate.setRollbackOnly();
				this.transactionDelegate.endTransaction();
			}
			catch (Throwable ex) {
				logger.error("Could not complete unfinished transaction on endpoint release", ex);
			}
		}
	}


	private class TransactionDelegate {

		private XAResource xaResource;

		private Transaction transaction;

		private boolean rollbackOnly;

		protected TransactionDelegate(XAResource xaResource) {
			if (transactionFactory != null && xaResource == null) {
				throw new IllegalStateException("ResourceAdapter-provided XAResource is required for " +
						"transaction management. Check your ResourceAdapter's configuration.");
			}
			this.xaResource = xaResource;
		}

		public void beginTransaction() throws Exception {
			if (transactionFactory != null) {
				this.transaction = transactionFactory.createTransaction(transactionName, transactionTimeout);
				this.transaction.enlistResource(this.xaResource);
			}
		}

		public void setRollbackOnly() {
			if (this.transaction != null) {
				this.rollbackOnly = true;
			}
		}

		public void endTransaction() throws Exception {
			if (this.transaction != null) {
				try {
					if (this.rollbackOnly) {
						this.transaction.rollback();
					}
					else {
						this.transaction.commit();
					}
				}
				finally {
					this.transaction = null;
					this.rollbackOnly = false;
				}
			}
		}
	}

}
