package org.springframework.transaction.jta;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;

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
 * <p><code>
 * &lt;bean id="jotm" class="org.springframework.transaction.jta.JotmFactoryBean"/&gt;<br>
 * <br>
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="userTransaction"&gt;&lt;ref local="jotm"/&gt;&lt;/property&gt;<br>
 * &lt;/bean&gt;<br>
 * <br>
 * &lt;bean id="innerDataSource" class="org.enhydra.jdbc.standard.StandardXADataSource"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="driverName"&gt;...&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;property name="url"&gt;...&lt;/property&gt;<br>
 * &lt;/bean&gt;<br>
 * <br>
 * &lt;bean id="dataSource" class="org.enhydra.jdbc.pool.StandardXAPoolDataSource"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="dataSource"&gt;&lt;ref local="innerDataSource"/&gt;&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;property name="maxSize"&gt;...&lt;/property&gt;<br>
 * &lt;/bean&gt;
 * </code>
 *
 * <p>Uses JOTM's static access method to obtain the JOTM Current object, which
 * implements both the UserTransaction and the TransactionManager interface.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setTransactionManager
 * @see org.objectweb.jotm.Current#getTransactionManager
 */
public class JotmFactoryBean implements FactoryBean {

	private Current jotmCurrent;

	public JotmFactoryBean() throws ClassNotFoundException, NoSuchMethodException,
	    IllegalAccessException, InvocationTargetException, NamingException {

		// check for already active JOTM instance
		this.jotmCurrent = Current.getCurrent();

		// if none found, create new local JOTM instance
		if (this.jotmCurrent == null) {
			new Jotm(true, false);
			this.jotmCurrent = Current.getCurrent();
		}
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

}
