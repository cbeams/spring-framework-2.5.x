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

package org.springframework.transaction.jta;

import javax.naming.NamingException;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that retrieves the JTA UserTransaction/TransactionManager for
 * ObjectWeb's <a href="http://jotm.objectweb.org">JOTM</a>. Will retrieve an
 * already active JOTM instance if found (e.g. if running in JOnAS), else create
 * a new local JOTM instance. The same object implements both the UserTransaction
 * and the TransactionManager interface, as returned by this FactoryBean.
 *
 * <p>A local JOTM instance is well-suited for working in conjunction with
 * ObjectWeb's <a href="http://xapool.experlog.com">XAPool</a>, e.g. with bean
 * definitions like the following:
 *
 * <pre>
 * &lt;bean id="jotm" class="org.springframework.transaction.jta.JotmFactoryBean"/&gt;
 *
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager"&gt;
 *   &lt;property name="userTransaction"&gt;&lt;ref local="jotm"/&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="innerDataSource" class="org.enhydra.jdbc.standard.StandardXADataSource" destroy-method="shutdown"&gt;
 *   &lt;property name="transactionManager"&gt;&lt;ref local="jotm"/&gt;&lt;/property&gt;
 *   &lt;property name="driverName"&gt;...&lt;/property&gt;
 *   &lt;property name="url"&gt;...&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="dataSource" class="org.enhydra.jdbc.pool.StandardXAPoolDataSource" destroy-method="shutdown"&gt;
 *   &lt;property name="dataSource"&gt;&lt;ref local="innerDataSource"/&gt;&lt;/property&gt;
 *   &lt;property name="maxSize"&gt;...&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Uses JOTM's static access method to obtain the JOTM Current object, which
 * implements both the UserTransaction and the TransactionManager interface.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setUserTransaction
 * @see JtaTransactionManager#setTransactionManager
 * @see org.objectweb.jotm.Current
 */
public class JotmFactoryBean implements FactoryBean, DisposableBean {

	private Current jotmCurrent;

	private Jotm jotm;

	public JotmFactoryBean() throws NamingException {
		// check for already active JOTM instance
		this.jotmCurrent = Current.getCurrent();

		// if none found, create new local JOTM instance
		if (this.jotmCurrent == null) {
			this.jotm = new Jotm(true, false);
			this.jotmCurrent = Current.getCurrent();
		}
	}

	/**
	 * Return the JOTM instance created by this factory bean, if any.
	 * Will be null if an already active JOTM instance is used.
	 * <p>Application code should never need to access this.
	 */
	public Jotm getJotm() {
		return jotm;
	}

	public Object getObject() {
		return this.jotmCurrent;
	}

	public Class getObjectType() {
		return this.jotmCurrent.getClass();
	}

	public boolean isSingleton() {
		return true;
	}

	public void destroy() {
		if (this.jotm != null) {
			this.jotm.stop();
		}
	}

}
