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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AOP Alliance MethodInterceptor that can be introduced in a chain to display
 * verbose information about intercepted invocations to the logger.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class DebugInterceptor implements MethodInterceptor, Serializable {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private int count;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		++this.count;
		if (logger.isDebugEnabled()) {
			logger.debug("Before invocation (count=" + this.count + "): " + invocation);
		}
		try {
			Object rval = invocation.proceed();
			if (logger.isInfoEnabled()) {
				logger.debug("Invocation successfully returned (count=" + this.count + "): " + invocation);
			}
			return rval;
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invocation threw exception (count=" + this.count + "): " + invocation, ex);
			}
			throw ex;
		}
	}
	
	/**
	 * Return the number of times this interceptor has been invoked.
	 */
	public int getCount() {
		return this.count;
	}
    
    /**
     * Reset the invocation count to zero.
     *
     */
    public void resetCount() {
        this.count = 0;
    }

}
