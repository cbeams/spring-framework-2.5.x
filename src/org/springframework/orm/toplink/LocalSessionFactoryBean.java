/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.TopLinkException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory bean that configures a TopLink SessionFactory and provides it as bean
 * reference. This is the usual way to define a TopLink SessionFactory in a Spring
 * application context, allowing to pass it to TopLink DAOs as bean reference.
 *
 * <p>See the base class LocalSessionFactory for configuration details.
 *
 * <p>If your DAOs expect to receive a raw TopLink Session, consider defining a
 * TransactionAwareSessionAdapter in front of this bean. This adapter will provide
 * a TopLink Session rather than a SessionFactory as bean reference. Your DAOs can
 * then, for example, access the currently active Session and UnitOfWork via
 * <code>Session.getActiveSession()</code> and <code>Session.getActiveUnitOfWork()</code>,
 * respectively. Note that you can still access the SessionFactory too, by defining
 * a bean reference that points directly at the LocalSessionFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see LocalSessionFactory
 * @see org.springframework.orm.toplink.support.TransactionAwareSessionAdapter
 */
public class LocalSessionFactoryBean extends LocalSessionFactory
		implements FactoryBean, InitializingBean, DisposableBean {

	private SessionFactory sessionFactory;


	public void afterPropertiesSet() throws TopLinkException {
		this.sessionFactory = createSessionFactory();
	}


	public Object getObject() {
		return this.sessionFactory;
	}

	public Class getObjectType() {
		return (this.sessionFactory != null ? this.sessionFactory.getClass() : SessionFactory.class);
	}

	public boolean isSingleton() {
		return true;
	}


	public void destroy() {
		logger.info("Closing TopLink SessionFactory");
		this.sessionFactory.close();
	}

}
