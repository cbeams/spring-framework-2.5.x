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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;

/**
 * Abstract implementation of TransactionAttributeSource that caches attributes
 * for methods, and implements fallback policy of 1. most specific method; 
 * 2. target class attribute; 3. declaring method; 4. declaring class.
 *
 * <p>Defaults to using class's transaction attribute if none is associated
 * with the target method. Any transaction attribute associated with the
 * target method completely overrides a class transaction attribute.
 *
 * <p>This implementation caches attributes by method after they are first used.
 * If it's ever desirable to allow dynamic changing of transaction attributes
 * (unlikely) caching could be made configurable. Caching is desirable because
 * of the cost of evaluating rollback rules.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {
	
	/**
	 * Canonical value held in cache to indicate no transaction attribute was
	 * found for this method, and we don't need to look again.
	 */
	private final static Object NULL_TRANSACTION_ATTRIBUTE = new Object();
	
	
	/**
	 * Logger available to subclasses.
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Cache of TransactionAttributes, keyed by Method and target class.
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	private Map cache = new HashMap();


	/**
	 * Return the transaction attribute for this method invocation.
	 * Defaults to the class's transaction attribute if no method
	 * attribute is found
	 * @param method method for the current invocation. Can't be <code>null</code>
	 * @param targetClass target class for this invocation. May be <code>null</code>.
	 * @return TransactionAttribute for this method, or <code>null</code> if the method is non-transactional
	 */
	public final TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		// First, see if we have a cached value.
		Object cacheKey = getCacheKey(method, targetClass);
		Object cached = this.cache.get(cacheKey);
		if (cached != null) {
			// Value will either be canonical value indicating there is no transaction attribute,
			// or an actual transaction attribute.
			if (cached == NULL_TRANSACTION_ATTRIBUTE) {
				return null;
			}
			else {
				return (TransactionAttribute) cached;
			}
		}
		else {
			// We need to work it out.
			TransactionAttribute txAtt = computeTransactionAttribute(method, targetClass);
			// Put it in the cache.
			if (txAtt == null) {
				this.cache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Adding transactional method [" + method.getName() + "] with attribute [" + txAtt + "]");
				}
				this.cache.put(cacheKey, txAtt);
			}
			return txAtt;
		}
	}

	/**
	 * Determine a cache key for the given method and target class.
	 * Must not produce same key for overloaded methods.
	 * Must produce same key for different instances of the same method.
	 * @param method the method
	 * @param targetClass the target class (may be <code>null</code>)
	 * @return the cache key
	 */
	protected Object getCacheKey(Method method, Class targetClass) {
		// TODO this works fine, but could consider making it faster in future:
		// Method.toString() is relatively (although not disastrously) slow.
		return targetClass + "." + method;
	}
	
	/**
	 * Same return as getTransactionAttribute method, but doesn't cache the result.
	 * getTransactionAttribute is a caching decorator for this method.
	 */
	private TransactionAttribute computeTransactionAttribute(Method method, Class targetClass) {
		// The method may be on an interface, but we need attributes from the target class.
		// The AopUtils class provides a convenience method for this. If the target class
		// is null, the method will be unchanged.
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		
		// First try is the method in the target class.
		TransactionAttribute txAtt = findTransactionAttribute(findAllAttributes(specificMethod));
		if (txAtt != null) {
			return txAtt;
		}

		// Second try is the transaction attribute on the target class.
		txAtt = findTransactionAttribute(findAllAttributes(specificMethod.getDeclaringClass()));
		if (txAtt != null) {
			return txAtt;
		}

		if (specificMethod != method ) {
			// Fallback is to look at the original method.
			txAtt = findTransactionAttribute(findAllAttributes(method));
			if (txAtt != null) {
				return txAtt;
			}
			// Last fallback is the class of the original method.
			return findTransactionAttribute(findAllAttributes(method.getDeclaringClass()));
		}
		return null;
	}
	
	/**
	 * Subclasses should implement this to return all attributes for this method.
	 * We need all because of the need to analyze rollback rules.
	 * @param method the method to retrieve attributes for
	 * @return all attributes associated with this method.
	 * May return null.
	 */
	protected abstract Collection findAllAttributes(Method method);
	
	/**
	 * Subclasses should implement this to return all attributes for this class.	 
	 * @param clazz class to retrieve attributes for
	 * @return all attributes associated with this class.
	 * May return null.
	 */
	protected abstract Collection findAllAttributes(Class clazz);


	/**
	 * Return the transaction attribute, given this set of attributes
	 * attached to a method or class.
	 * <p>Protected rather than private as subclasses may want to customize
	 * how this is done: for example, returning a TransactionAttribute
	 * affected by the values of other attributes.
	 * <p>This implementation takes into account RollbackRuleAttributes,
	 * if the TransactionAttribute is a RuleBasedTransactionAttribute.
	 * Return null if it's not transactional. 
	 * @param atts attributes attached to a method or class. May
	 * be <code>null</code>, in which case a null TransactionAttribute will be returned.
	 * @return TransactionAttribute configured transaction attribute, or <code>null</code>
	 * if none was found
	 */
	protected TransactionAttribute findTransactionAttribute(Collection atts) {
		if (atts == null) {
			return null;
		}

		TransactionAttribute txAttribute = null;

		// Check whether there is a transaction attribute.
		for (Iterator itr = atts.iterator(); itr.hasNext() && txAttribute == null; ) {
			Object att = itr.next();
			if (att instanceof TransactionAttribute) {
				txAttribute = (TransactionAttribute) att;
			}
		}

		// Check if we have a RuleBasedTransactionAttribute.
		if (txAttribute instanceof RuleBasedTransactionAttribute) {
			RuleBasedTransactionAttribute rbta = (RuleBasedTransactionAttribute) txAttribute;
			// We really want value: bit of a hack.
			List rollbackRules = new LinkedList();
			for (Iterator it = atts.iterator(); it.hasNext(); ) {
				Object att = it.next();
				if (att instanceof RollbackRuleAttribute) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found rollback rule: " + att);
					}
					rollbackRules.add(att);
				}
			}
			// Repeatedly setting this isn't elegant, but it works.
			rbta.setRollbackRules(rollbackRules);
		}
		
		return txAttribute;
	}

}
