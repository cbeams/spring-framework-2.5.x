/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple AOP Alliance MethodInterceptor that can be introduced in a chain to display verbose trace information about
 * intercepted method invocations with method entry and method exit info using Commons Logging at DEBUG level. *
 * 
 * @author Dmitriy Kopylenko
 */
public class TraceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * Static to avoid serializing the logger
	 */
	protected static final Log logger = LogFactory.getLog(TraceInterceptor.class);

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.debug("Entering method [" + invocation.getMethod().getName() + "] in class [" + invocation.getThis().getClass().getName() + "]");
        Object rval = invocation.proceed();
        logger.debug("Exiting method [" + invocation.getMethod().getName() + "] in class [" + invocation.getThis().getClass().getName() + "]");
        return rval;
    }
}