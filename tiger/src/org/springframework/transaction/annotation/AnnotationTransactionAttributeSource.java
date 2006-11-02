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

package org.springframework.transaction.annotation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Implementation of the
 * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}
 * interface for working with transaction metadata in JDK 1.5+ annotation format.
 *
 * <p>This class reads Spring's JDK 1.5+ {@link Transactional} annotation and
 * exposes corresponding transaction attributes to Spring's transaction infrastructure.
 * Can also be used as base class for a custom annotation-based TransactionAttributeSource.
 *
 * <p>This is a direct alternative to
 * {@link org.springframework.transaction.interceptor.AttributesTransactionAttributeSource},
 * which is able to read in source-level attributes via Commons Attributes.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see Transactional
 * @see #findTransactionAttribute
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.AttributesTransactionAttributeSource
 * @see org.springframework.metadata.commons.CommonsAttributes
 */
public class AnnotationTransactionAttributeSource
		extends AbstractFallbackTransactionAttributeSource implements Serializable {

	private final boolean publicMethodsOnly;


	/**
	 * Create a default AnnotationTransactionAttributeSource, supporting
	 * public methods that carry the <code>Transactional</code> annotation.
	 */
	public AnnotationTransactionAttributeSource() {
		this.publicMethodsOnly = true;
	}

	/**
	 * Create a custom AnnotationTransactionAttributeSource.
	 * @param publicMethodsOnly whether to support public methods that carry
	 * the <code>Transactional</code> annotation only (typically for use
	 * with proxy-based AOP), or protected/private methods as well
	 * (typically used with AspectJ class weaving)
	 */
	public AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
		this.publicMethodsOnly = publicMethodsOnly;
	}


	/**
	 * Returns all JDK 1.5+ annotations found for the given method.
	 */
	protected Collection findAllAttributes(Method method) {
		return Arrays.asList(AnnotationUtils.getAnnotations(method));
	}

	/**
	 * Returns all JDK 1.5+ annotations found for the given class.
	 */
	protected Collection findAllAttributes(Class clazz) {
		return Arrays.asList(clazz.getAnnotations());
	}

	/**
	 * Return the transaction attribute, given this set of attributes
	 * attached to a method or class. Overrides method from parent class.
	 * <p>This implementation converts Spring's <code>Transactional</code> annotation
	 * to the Spring metadata classes. Returns <code>null</code> if it's not transactional.
	 * <p>Can be overridden to support custom annotations that carry transaction metadata.
	 * @param atts attributes attached to a method or class. May be <code>null</code>,
	 * in which case a <code>null</code> TransactionAttribute will be returned.
	 * @return TransactionAttribute the configured transaction attribute,
	 * or <code>null</code> if none was found
	 * @see Transactional
	 */
	protected TransactionAttribute findTransactionAttribute(Collection atts) {
		if (atts == null) {
			return null;
		}

		// See if there is a transaction annotation.
		for (Object att : atts) {
			if (att instanceof Transactional) {
				Transactional ruleBasedTx = (Transactional) att;

				RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
				rbta.setPropagationBehavior(ruleBasedTx.propagation().value());
				rbta.setIsolationLevel(ruleBasedTx.isolation().value());
				rbta.setTimeout(ruleBasedTx.timeout());
				rbta.setReadOnly(ruleBasedTx.readOnly());

				ArrayList<RollbackRuleAttribute> rollBackRules = new ArrayList<RollbackRuleAttribute>();

				Class[] rbf = ruleBasedTx.rollbackFor();
				for (int i = 0; i < rbf.length; ++i) {
					RollbackRuleAttribute rule = new RollbackRuleAttribute(rbf[i]);
					rollBackRules.add(rule);
				}

				String[] rbfc = ruleBasedTx.rollbackForClassName();
				for (int i = 0; i < rbfc.length; ++i) {
					RollbackRuleAttribute rule = new RollbackRuleAttribute(rbfc[i]);
					rollBackRules.add(rule);
				}

				Class[] nrbf = ruleBasedTx.noRollbackFor();
				for (int i = 0; i < nrbf.length; ++i) {
					NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(nrbf[i]);
					rollBackRules.add(rule);
				}

				String[] nrbfc = ruleBasedTx.noRollbackForClassName();
				for (int i = 0; i < nrbfc.length; ++i) {
					NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(nrbfc[i]);
					rollBackRules.add(rule);
				}

				rbta.getRollbackRules().addAll(rollBackRules);

				return rbta;
			}
		}

		return null;
	}

	/**
	 * By default, only public methods can be made transactional using
	 * {@link Transactional}.
	 */
	protected boolean allowPublicMethodsOnly() {
		return this.publicMethodsOnly;
	}


	public boolean equals(Object other) {
		return (this == other || other instanceof AnnotationTransactionAttributeSource);
	}

	public int hashCode() {
		return AnnotationTransactionAttributeSource.class.hashCode();
	}

}
