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

package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator;
import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.beans.factory.BeanFactory;

/**
 * Simple PoolingTargetSourceCreator that pools everything.
 * @author Rod Johnson
 */
public class TestPoolingTargetSourceCreator extends AbstractPoolingTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator#getPoolingAttribute(java.lang.Object, java.lang.String, org.springframework.beans.factory.BeanFactory)
	 */
	protected PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory bf) {
		if (!beanName.startsWith("pooling"))
			return null;
		return new PoolingAttribute(25);
	}



}
