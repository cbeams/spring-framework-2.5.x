/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.orm.hibernate3.support;

import org.hibernate.EmptyInterceptor;
import org.springframework.aop.scope.ScopedObject;

/**
 * Hibernate 3 interceptor used for getting the proper entity name for scoped
 * beans. As scoped bean classes are proxies generated at runtime, they are
 * unrecognized by the persisting framework. Using this interceptor, the
 * original scoped bean class is retrieved end given to Hibernate for
 * persisting. <p/> Usage example:
 * </p>
 * 
 * <pre>
 *  &lt;bean id=&quot;sessionFactory&quot; class=&quot;org.springframework.orm.hibernate3.LocalSessionFactoryBean&quot;&gt;
 *     ...
 *    &lt;property name=&quot;entityInterceptor&quot;&gt;
 *   	&lt;bean class=&quot;org.springframework.orm.hibernate3.support.ScopedBeanInterceptor&quot;/&gt;
 *    &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 * 
 * @author Costin Leau
 * 
 */
public class ScopedBeanInterceptor extends EmptyInterceptor {

	public String getEntityName(Object entity) {
		if (entity instanceof ScopedObject) {
			// get underlying object
			Object targetObject = ((ScopedObject) entity).getTargetObject();
			return targetObject.getClass().getName();
		}

		// delegate to default implementation
		return super.getEntityName(entity);
	}

}
