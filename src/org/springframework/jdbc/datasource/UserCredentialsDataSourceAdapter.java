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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An adapter for a target DataSource, applying the given user credentials to
 * every standard <code>getConnection()</code> call, that is, implicitly
 * invoking <code>getConnection(username, password)</code> on the target.
 * All other methods simply delegate to the corresponding methods of the
 * target DataSource.
 *
 * <p>Can be used to proxy a target JNDI DataSource that does not have user
 * credentials configured. Client code can work with the DataSource without
 * passing in username and password on every <code>getConnection()</code> call.
 *
 * <p>In the following example, client code can simply transparently work with
 * the preconfigured "myDataSource", implicitly accessing "myTargetDataSource"
 * with the specified user credentials.
 *
 * <pre>
 * &lt;bean id="myTargetDataSource" class="org.springframework.jndi.JndiObjectFactoryBean">
 *   &lt;property name="jndiName">&lt;value>java:comp/env/jdbc/myds&lt;/value>&lt;/property>
 * &lt;/bean>
 *
 * &lt;bean id="myDataSource" class="org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter">
 *   &lt;property name="targetDataSource">&lt;ref bean="myTargetDataSource"/>&lt;/property>
 *   &lt;property name="username">&lt;value>myusername&lt;/value>&lt;/property>
 *   &lt;property name="password">&lt;value>mypassword&lt;/value>&lt;/property>
 * &lt;/bean></pre>
 *
 * <p>If the "username" is empty, this proxy will simply delegate to the
 * standard <code>getConnection()</code> method of the target DataSource.
 * This can be used to keep a UserCredentialsDataSourceAdapter bean definition
 * just for the <i>option</i> of implicitly passing in user credentials if
 * a particular target DataSource requires it.
 *
 * @author Juergen Hoeller
 * @since 28.05.2004
 * @see #getConnection
 */
public class UserCredentialsDataSourceAdapter extends DelegatingDataSource {

	private String username = "";

	private String password = "";

	private final ThreadLocal threadBoundCredentials = new ThreadLocal();


	/**
	 * Set the username that this adapter should use for retrieving Connections.
	 * Default is the empty string, i.e. no specific user.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the password that this adapter should use for retrieving Connections.
	 * Default is the empty string, i.e. no specific password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Set user credententials for this proxy and the current thread.
	 * The given username and password will be applied to all subsequent
	 * <code>getConnection()</code> calls on this DataSource proxy.
	 * <p>This will override any statically specified user credentials,
	 * that is, values of the "username" and "password" bean properties.
	 * @param username the username to apply
	 * @param password the password to apply
	 * @see #removeCredentialsFromCurrentThread
	 */
	public void setCredentialsForCurrentThread(String username, String password) {
		this.threadBoundCredentials.set(new String[] {username, password});
	}

	/**
	 * Remove any user credentials for this proxy from the current thread.
	 * Statically specified user credentials apply again afterwards.
	 * @see #setCredentialsForCurrentThread
	 */
	public void removeCredentialsFromCurrentThread() {
		this.threadBoundCredentials.set(null);
	}


	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 * @see #doGetConnection
	 */
	public final Connection getConnection() throws SQLException {
		String[] threadCredentials = (String[]) this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doGetConnection(threadCredentials[0], threadCredentials[1]);
		}
		else {
			return doGetConnection(this.username, this.password);
		}
	}

	/**
	 * This implementation delegates to the <code>getConnection(username, password)</code>
	 * method of the target DataSource, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * <code>getConnection()</code> method of the target DataSource.
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see javax.sql.DataSource#getConnection(String, String)
	 * @see javax.sql.DataSource#getConnection()
	 */
	protected Connection doGetConnection(String username, String password) throws SQLException {
		if (!"".equals(username)) {
			return getTargetDataSource().getConnection(username, password);
		}
		else {
			return getTargetDataSource().getConnection();
		}
	}

}
