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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.IntroductionInfo;

/**
 * Support for implementations of IntroductionAdvice.
 * This class allows for querying of all introduced interfaces,
 * as well as implemented the implementsInterface() method
 * from IntroductionAdvice.
 * It also allows subclasses to conveniently add all interfaces
 * from a given object, and to suppress interfaces that should
 * not be added.
 * @author Rod Johnson
 */
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

	protected transient Log logger = LogFactory.getLog(getClass());

	/** Set of Class */
	protected Set publishedInterfaces = new HashSet();

	/**
	 * Suppress the specified interface, which will have
	 * been autodetected due to its implementation by
	 * the delegate.
	 * Does nothing if it's not implemented by the delegate
	 * @param intf interface to suppress
	 */
	public void suppressInterface(Class intf) {
		this.publishedInterfaces.remove(intf);
	}

	public Class[] getInterfaces() {
		return (Class[]) this.publishedInterfaces.toArray(new Class[this.publishedInterfaces.size()]);
	}

	/**
	 * Implementation of IntroductionAdvice method
	 * @see org.springframework.aop.DynamicIntroductionAdvice#implementsInterface(java.lang.Class)
	 */
	public boolean implementsInterface(Class intf) {
		for (Iterator it = this.publishedInterfaces.iterator(); it.hasNext();) {
			Class pubIntf = (Class) it.next();
			if (intf.isInterface() && intf.isAssignableFrom(pubIntf)) {
				return true;
			}
		}
		return false;
	}
	
	protected void implementInterfacesOnObject(Object delegate) {
		this.publishedInterfaces.addAll(AopUtils.getAllInterfacesAsList(delegate));
	}

	/**
	 * Is this method on an introduced interface?
	 * @param mi method invocation
	 * @return whether the method is on an introduced interface
	 */
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
		return implementsInterface(mi.getMethod().getDeclaringClass());
	}

	/**
	 * This method is implemented only to restore the logger.
	 * We don't make the logger static as that would mean that subclasses
	 * would use this class's log category.
	 */
	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new AspectException("Failed to deserialize Spring DelegatingIntroductionInterceptor:" +
					"Check that Spring AOP libraries and implementation class for the introduction are available on the client side");
		}
		
		// Initialize transient fields
		this.logger = LogFactory.getLog(getClass());
	}

}
