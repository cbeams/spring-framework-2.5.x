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

package org.springframework.jndi;

import javax.naming.NamingException;

import org.springframework.aop.TargetSource;

/**
 * TargetSource which performs a fresh JNDI lookup for each call.
 *
 * <p>Can be used as alternative to JndiObjectFactoryBean, to allow for
 * relocating a JNDI object for each operation. This is particularly useful
 * during development, as it allows for hot restarting of the JMS server.
 *
 * <p>Example:
 *
 * <pre>
 * &lt;bean id="queueConnectionFactoryTarget" class="org.springframework.jndi.JndiObjectTargetSource"&gt;
 *   &lt;property name="jndiName"&gt;&lt;value&gt;JmsQueueConnectionFactory&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="queueConnectionFactory" class="org.springframework.aop.framework.ProxyFactoryBean"&gt;
 *   &lt;property name="proxyInterfaces"&gt;&lt;value&gt;javax.jms.QueueConnectionFactory&lt;/value&gt;&lt;/property&gt;
 *   &lt;property name="targetSource"&gt;&lt;ref bean="queueConnectionFactoryTarget"/&gt;&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * A <code>createQueueConnection</code> call on the "queueConnectionFactory" proxy will
 * cause a JNDI lookup for "JmsQueueConnectionFactory" and a subsequent delegating call
 * to the retrieved QueueConnectionFactory's <code>createQueueConnection</code>.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JndiObjectFactoryBean
 * @see org.springframework.aop.framework.ProxyFactoryBean#setTargetSource(TargetSource)
 */
public class JndiObjectTargetSource extends JndiObjectLocator implements TargetSource {

	private boolean lookupOnStartup = true;

	private boolean cache = true;

	private Object cachedObject;

	private Class targetClass;


	/**
	 * Set whether to look up the JNDI object on startup. Default is true.
	 * <p>Can be turned off to allow for late availability of the JNDI object.
	 * In this case, the JNDI object will be fetched on first access.
	 * @see #setCache
	 */
	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
	}

	/**
	 * Set whether to cache the JNDI object once it has been located.
	 * Default is true.
	 * <p>Can be turned off to allow for hot redeployment of JNDI objects.
	 * In this case, the JNDI object will be fetched for each invocation.
	 * @see #setLookupOnStartup
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		if (this.lookupOnStartup) {
			Object object = lookup();
			if (this.cache) {
				this.cachedObject = object;
			}
			else {
				this.targetClass = object.getClass();
			}
		}
	}


	public Class getTargetClass() {
		return (this.cachedObject != null ? this.cachedObject.getClass() : this.targetClass);
	}

	public boolean isStatic() {
		return (this.cachedObject != null);
	}

	public Object getTarget() throws NamingException {
		if (this.lookupOnStartup || !this.cache) {
			return (this.cachedObject != null ? this.cachedObject : lookup());
		}
		else {
			synchronized (this) {
				if (this.cachedObject == null) {
					this.cachedObject = lookup();
				}
				return this.cachedObject;
			}
		}
	}

	public void releaseTarget(Object target) {
	}

}
