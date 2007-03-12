/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.util.ObjectUtils;

/**
 * Visitor base class for traversing {@link BeanDefinition} objects, in particular
 * the property values and constructor argument values contained in them.
 *
 * <p>The abstract {@link #resolveStringValue} method has to be implemented
 * in concrete subclasses, following arbitrary resolution strategies.
 *
 * <p>Used by {@link PropertyPlaceholderConfigurer} to parse all String values
 * contained in a BeanDefinition, resolving any placeholders found.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BeanDefinition
 * @see BeanDefinition#getPropertyValues
 * @see BeanDefinition#getConstructorArgumentValues
 * @see #resolveStringValue(String)
 * @see PropertyPlaceholderConfigurer
 */
public abstract class BeanDefinitionVisitor {

	/**
	 * Traverse the given BeanDefinition object and the MutablePropertyValues
	 * and ConstructorArgumentValues contained in them.
	 * @param beanDefinition the BeanDefinition object to traverse
	 * @see #resolveStringValue(String)
	 */
	public void visitBeanDefinition(BeanDefinition beanDefinition) {
		visitBeanClassName(beanDefinition);
		visitScope(beanDefinition);
		visitPropertyValues(beanDefinition.getPropertyValues());
		ConstructorArgumentValues cas = beanDefinition.getConstructorArgumentValues();
		visitIndexedArgumentValues(cas.getIndexedArgumentValues());
		visitGenericArgumentValues(cas.getGenericArgumentValues());
	}

	protected void visitBeanClassName(BeanDefinition beanDefinition) {
		String beanClassName = beanDefinition.getBeanClassName();
		if (beanClassName != null) {
			String resolvedName = resolveStringValue(beanClassName);
			if (!beanClassName.equals(resolvedName)) {
				beanDefinition.setBeanClassName(resolvedName);
			}
		}
	}

	protected void visitScope(BeanDefinition beanDefinition) {
		String scope = beanDefinition.getScope();
		if (scope != null) {
			String resolvedScope = resolveStringValue(scope);
			if (!scope.equals(resolvedScope)) {
				beanDefinition.setScope(resolvedScope);
			}
		}
	}

	protected void visitPropertyValues(MutablePropertyValues pvs) {
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			PropertyValue pv = pvArray[i];
			Object newVal = resolveValue(pv.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
				pvs.addPropertyValue(pv.getName(), newVal);
			}
		}
	}

	protected void visitIndexedArgumentValues(Map ias) {
		for (Iterator it = ias.values().iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) it.next();
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}

	protected void visitGenericArgumentValues(List gas) {
		for (Iterator it = gas.iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) it.next();
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}

	protected Object resolveValue(Object value) {
		if (value instanceof BeanDefinition) {
			visitBeanDefinition((BeanDefinition) value);
		}
		else if (value instanceof BeanDefinitionHolder) {
			visitBeanDefinition(((BeanDefinitionHolder) value).getBeanDefinition());
		}
		else if (value instanceof RuntimeBeanReference) {
      RuntimeBeanReference ref = (RuntimeBeanReference) value;
      String newBeanName = resolveStringValue(ref.getBeanName());
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanReference(newBeanName);
			}
		}
		else if (value instanceof List) {
			visitList((List) value);
		}
		else if (value instanceof Set) {
			visitSet((Set) value);
		}
		else if (value instanceof Map) {
			visitMap((Map) value);
		}
		else if (value instanceof TypedStringValue) {
			TypedStringValue typedStringValue = (TypedStringValue) value;
			String stringValue = typedStringValue.getValue();
			if (stringValue != null) {
				String visitedString = resolveStringValue(stringValue);
				typedStringValue.setValue(visitedString);
			}
		}
		else if (value instanceof String) {
			return resolveStringValue((String) value);
		}
		return value;
	}

	protected void visitList(List listVal) {
		for (int i = 0; i < listVal.size(); i++) {
			Object elem = listVal.get(i);
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				listVal.set(i, newVal);
			}
		}
	}

	protected void visitSet(Set setVal) {
		for (Iterator it = new HashSet(setVal).iterator(); it.hasNext();) {
			Object elem = it.next();
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				setVal.remove(elem);
				setVal.add(newVal);
			}
		}
	}

	protected void visitMap(Map mapVal) {
		for (Iterator it = new HashMap(mapVal).entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object newKey = resolveValue(key);
			boolean isNewKey = !ObjectUtils.nullSafeEquals(key, newKey);
			Object val = entry.getValue();
			Object newVal = resolveValue(val);
			if (isNewKey) {
				mapVal.remove(key);
			}
			if (isNewKey || !ObjectUtils.nullSafeEquals(newVal, val)) {
				mapVal.put(newKey, newVal);
			}
		}
	}

	/**
	 * Resolve the given String value, for example parsing placeholders.
	 * @param strVal the original String value
	 * @return the resolved String value
	 */
	protected abstract String resolveStringValue(String strVal);

}
