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

package org.springframework.aop.support;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.AopProxyUtils;

/**
 * Convenient implementation of the IntroductionInterceptor interface.
 *
 * <p>Subclasses merely need to extend this class and implement the interfaces
 * to be introduced themselves. In this case the delegate is the subclass
 * instance itself. Alternatively a separate delegate may implement the
 * interface, and be set via the delegate bean property.
 *
 * <p>Delegates or subclasses may implement any number of interfaces.
 * All interfaces except IntroductionInterceptor are picked up from
 * the subclass or delegate by default.
 *
 * <p>The suppressInterface() method can be used to suppress interfaces
 * implemented by the delegate but which should not be introduced to the
 * owning AOP proxy.
 * 
 * <p>A DelegatingIntroductionInterceptor is serializable if the delegate is.
 *
 * @author Rod Johnson
 */
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {
		
	/**
	 * Object that actually implements the interfaces.
	 * May be "this" if a subclass implements the introduced interfaces.
	 */
	private Object delegate;
	
	/**
	 * Construct a new DelegatingIntroductionInterceptor, providing
	 * a delegate that implements the interfaces to be introduced.
	 * @param delegate the delegate that implements the introduced interfaces
	 */
	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}
	
	/**
	 * Construct a new DelegatingIntroductionInterceptor.
	 * The delegate will be the subclass, which must implement
	 * additional interfaces.
	 */
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}
	
	/**
	 * Both constructors use this, as it's impossible to pass
	 * "this" from one constructor to another. 
	 */
	private void init(Object delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("Delegate cannot be null in DelegatingIntroductionInterceptor");
		}
		this.delegate = delegate;
		implementInterfacesOnObject(delegate);
		// We don't want to expose the control interface
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}
		
	
	/**
	 * Subclasses may need to override this if they want to  perform custom
	 * behaviour in around advice. However, subclasses should invoke this
	 * method, which handles introduced interfaces and forwarding to the target.
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking self on invocation [" + mi + "]; breaking interceptor chain");
			}
			// Using the following method rather than direct reflection, we
			// get correct handling of InvocationTargetException if the introduced method throws an exception
			return AopProxyUtils.invokeJoinpointUsingReflection(delegate, mi.getMethod(), mi.getArguments());
		}
		
		// If we get here, just pass the invocation on
		return mi.proceed();
	}

}
