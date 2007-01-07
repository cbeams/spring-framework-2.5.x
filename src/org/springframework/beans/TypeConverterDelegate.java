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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.CollectionFactory;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.JdkVersion;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Internal helper class for converting property values to target types.
 *
 * <p>Works on a given {@link PropertyEditorRegistrySupport} instance.
 * Used as a delegate by {@link BeanWrapperImpl} and {@link SimpleTypeConverter}.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapperImpl
 * @see SimpleTypeConverter
 */
class TypeConverterDelegate {

	/**
	 * We'll create a lot of these objects, so we don't want a new logger every time.
	 */
	private static final Log logger = LogFactory.getLog(TypeConverterDelegate.class);

	private final PropertyEditorRegistrySupport propertyEditorRegistry;

	private final Object targetObject;


	/**
	 * Create a new TypeConverterDelegate for the given editor registry.
	 * @param propertyEditorRegistry the editor registry to use
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
		this(propertyEditorRegistry, null);
	}

	/**
	 * Create a new TypeConverterDelegate for the given editor registry and bean instance.
	 * @param propertyEditorRegistry the editor registry to use
	 * @param targetObject the target object to work on (as context that can be passed to editors)
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, Object targetObject) {
		Assert.notNull(propertyEditorRegistry, "Property editor registry must not be null");
		this.propertyEditorRegistry = propertyEditorRegistry;
		this.targetObject = targetObject;
	}


	/**
	 * Convert the value to the specified required type.
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	public Object convertIfNecessary(Object newValue, Class requiredType) throws IllegalArgumentException {
		return convertIfNecessary(null, null, newValue, requiredType, null, null);
	}

	/**
	 * Convert the value to the specified required type.
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @param methodParam the method parameter that is the target of the conversion
	 * (may be <code>null</code>)
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	public Object convertIfNecessary(Object newValue, Class requiredType, MethodParameter methodParam)
			throws IllegalArgumentException {

		return convertIfNecessary(null, null, newValue, requiredType, null, methodParam);
	}

	/**
	 * Convert the value to the required type for the specified property.
	 * @param propertyName name of the property
	 * @param oldValue previous value, if available (may be <code>null</code>)
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	public Object convertIfNecessary(
			String propertyName, Object oldValue, Object newValue, Class requiredType)
			throws IllegalArgumentException {

		return convertIfNecessary(propertyName, oldValue, newValue, requiredType, null, null);
	}

	/**
	 * Convert the value to the required type for the specified property.
	 * @param oldValue previous value, if available (may be <code>null</code>)
	 * @param newValue proposed change value
	 * @param descriptor the JavaBeans descriptor for the property
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	public Object convertIfNecessary(Object oldValue, Object newValue, PropertyDescriptor descriptor)
			throws IllegalArgumentException {

		Assert.notNull(descriptor, "PropertyDescriptor must not be null");
		return convertIfNecessary(
				descriptor.getName(), oldValue, newValue, descriptor.getPropertyType(), descriptor,
				new MethodParameter(descriptor.getWriteMethod(), 0));
	}


	/**
	 * Convert the value to the required type (if necessary from a String),
	 * for the specified property.
	 * @param propertyName name of the property
	 * @param oldValue previous value, if available (may be <code>null</code>)
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @param methodParam the method parameter that is the target of the conversion
	 * (may be <code>null</code>)
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	protected Object convertIfNecessary(
			String propertyName, Object oldValue, Object newValue, Class requiredType,
			PropertyDescriptor descriptor, MethodParameter methodParam)
			throws IllegalArgumentException {

		Object convertedValue = newValue;

		// Custom editor for this type?
		PropertyEditor pe = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);

		// Value not of required type?
		if (pe != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
			if (pe == null && descriptor != null) {
				if (JdkVersion.isAtLeastJava15()) {
					pe = descriptor.createPropertyEditor(this.targetObject);
				}
				else {
					Class editorClass = descriptor.getPropertyEditorClass();
					if (editorClass != null) {
						pe = (PropertyEditor) BeanUtils.instantiateClass(editorClass);
					}
				}
			}
			if (pe == null && requiredType != null) {
				// No custom editor -> check BeanWrapperImpl's default editors.
				pe = (PropertyEditor) this.propertyEditorRegistry.getDefaultEditor(requiredType);
				if (pe == null) {
					// No BeanWrapper default editor -> check standard JavaBean editors.
					pe = PropertyEditorManager.findEditor(requiredType);
				}
			}
			convertedValue = convertValue(convertedValue, requiredType, pe, oldValue);
		}

		if (requiredType != null) {
			// Try to apply some standard type conversion rules if appropriate.

			if (convertedValue != null && requiredType.isArray()) {
				// Array required -> apply appropriate conversion of elements.
				return convertToTypedArray(convertedValue, propertyName, requiredType.getComponentType());
			}
			else if (convertedValue instanceof Collection && Collection.class.isAssignableFrom(requiredType)) {
				// Convert elements to target type, if determined.
				return convertToTypedCollection((Collection) convertedValue, propertyName, methodParam);
			}
			else if (convertedValue instanceof Map && Map.class.isAssignableFrom(requiredType)) {
				// Convert keys and values to respective target type, if determined.
				return convertToTypedMap((Map) convertedValue, propertyName, methodParam);
			}
			else if (convertedValue instanceof String && !requiredType.isInstance(convertedValue)) {
				// Try field lookup as fallback: for JDK 1.5 enum or custom enum
				// with values defined as static fields. Resulting value still needs
				// to be checked, hence we don't return it right away.
				try {
					Field enumField = requiredType.getField((String) convertedValue);
					convertedValue = enumField.get(null);
				}
				catch (Throwable ex) {
					if (logger.isTraceEnabled()) {
						logger.trace("Field [" + convertedValue + "] isn't an enum value", ex);
					}
				}
			}

			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				// Definitely doesn't match: throw IllegalArgumentException.
				throw new IllegalArgumentException("Cannot convert value of type [" +
						(newValue != null ? ClassUtils.getQualifiedName(newValue.getClass()) : null) +
						"] to required type [" + ClassUtils.getQualifiedName(requiredType) + "] for property '" +
						propertyName + "': no matching editors or conversion strategy found");
			}
		}

		return convertedValue;
	}

	protected Object convertValue(Object newValue, Class requiredType, PropertyEditor pe, Object oldValue) {
		Object convertedValue = newValue;

		if (pe != null && !(convertedValue instanceof String)) {
			// Not a String -> use PropertyEditor's setValue.
			// With standard PropertyEditors, this will return the very same object;
			// we just want to allow special PropertyEditors to override setValue
			// for type conversion from non-String values to the required type.
			pe.setValue(convertedValue);
			Object newConvertedValue = pe.getValue();
			if (newConvertedValue != convertedValue) {
				convertedValue = newConvertedValue;
				// Reset PropertyEditor: It already did a proper conversion.
				// Don't use it again for a setAsText call.
				pe = null;
			}
		}

		if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
			// Convert String array to a comma-separated String.
			// Only applies if no PropertyEditor converted the String array before.
			// The CSV String will be passed into a PropertyEditor's setAsText method, if any.
			if (logger.isTraceEnabled()) {
				logger.trace("Converting String array to comma-delimited String [" + convertedValue + "]");
			}
			convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
		}

		if (pe != null && convertedValue instanceof String) {
			// Use PropertyEditor's setAsText in case of a String value.
			if (logger.isTraceEnabled()) {
				logger.trace("Converting String to [" + requiredType + "] using property editor [" + pe + "]");
			}
			pe.setValue(oldValue);
			pe.setAsText((String) convertedValue);
			convertedValue = pe.getValue();
		}

		return convertedValue;
	}

	protected Object convertToTypedArray(Object input, String propertyName, Class componentType) {
		if (input instanceof Collection) {
			// Convert Collection elements to array elements.
			Collection coll = (Collection) input;
			Object result = Array.newInstance(componentType, coll.size());
			int i = 0;
			for (Iterator it = coll.iterator(); it.hasNext(); i++) {
				Object value = convertIfNecessary(
						buildIndexedPropertyName(propertyName, i), null, it.next(), componentType);
				Array.set(result, i, value);
			}
			return result;
		}
		else if (input.getClass().isArray()) {
			// Convert array elements, if necessary.
			if (componentType.equals(input.getClass().getComponentType()) &&
					!this.propertyEditorRegistry.hasCustomEditorForElement(componentType, propertyName)) {
				return input;
			}
			int arrayLength = Array.getLength(input);
			Object result = Array.newInstance(componentType, arrayLength);
			for (int i = 0; i < arrayLength; i++) {
				Object value = convertIfNecessary(
						buildIndexedPropertyName(propertyName, i), null, Array.get(input, i), componentType);
				Array.set(result, i, value);
			}
			return result;
		}
		else {
			// A plain value: convert it to an array with a single component.
			Object result = Array.newInstance(componentType, 1);
			Object value = convertIfNecessary(
					buildIndexedPropertyName(propertyName, 0), null, input, componentType);
			Array.set(result, 0, value);
			return result;
		}
	}

	protected Collection convertToTypedCollection(
			Collection original, String propertyName, MethodParameter methodParam) {

		Class elementType = null;
		if (methodParam != null && JdkVersion.isAtLeastJava15()) {
			elementType = GenericCollectionTypeResolver.getCollectionParameterType(methodParam);
		}
		Collection convertedCopy = null;
		Iterator it = null;
		try {
			convertedCopy =
					CollectionFactory.createApproximateCollection(original, original.size());
			it = original.iterator();
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot access Collection of type [" + original.getClass().getName() +
						"] - injecting original Collection as-is", ex);
			}
			return original;
		}
		boolean actuallyConverted = false;
		int i = 0;
		for (; it.hasNext(); i++) {
			Object element = it.next();
			String indexedPropertyName = buildIndexedPropertyName(propertyName, i);
			if (methodParam != null) {
				methodParam.increaseNestingLevel();
			}
			Object convertedElement =
					convertIfNecessary(indexedPropertyName, null, element, elementType, null, methodParam);
			if (methodParam != null) {
				methodParam.decreaseNestingLevel();
			}
			convertedCopy.add(convertedElement);
			actuallyConverted = actuallyConverted || (element != convertedElement);
		}
		return (actuallyConverted ? convertedCopy : original);
	}

	protected Map convertToTypedMap(Map original, String propertyName, MethodParameter methodParam) {
		Class keyType = null;
		Class valueType = null;
		if (methodParam != null && JdkVersion.isAtLeastJava15()) {
			keyType = GenericCollectionTypeResolver.getMapKeyParameterType(methodParam);
			valueType = GenericCollectionTypeResolver.getMapValueParameterType(methodParam);
		}
		Map convertedCopy = null;
		Iterator it = null;
		try {
			convertedCopy = CollectionFactory.createApproximateMap(original, original.size());
			it = original.entrySet().iterator();
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot access Map of type [" + original.getClass().getName() +
						"] - injecting original Map as-is", ex);
			}
			return original;
		}
		boolean actuallyConverted = false;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			String keyedPropertyName = buildKeyedPropertyName(propertyName, key);
			if (methodParam != null) {
				methodParam.increaseNestingLevel();
			}
			Object convertedKey = convertIfNecessary(keyedPropertyName, null, key, keyType, null, methodParam);
			Object convertedValue = convertIfNecessary(keyedPropertyName, null, value, valueType, null, methodParam);
			if (methodParam != null) {
				methodParam.decreaseNestingLevel();
			}
			convertedCopy.put(convertedKey, convertedValue);
			actuallyConverted = actuallyConverted || (key != convertedKey) || (value != convertedValue);
		}
		return (actuallyConverted ? convertedCopy : original);
	}

	private String buildIndexedPropertyName(String propertyName, int index) {
		return (propertyName != null ?
				propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + index + PropertyAccessor.PROPERTY_KEY_SUFFIX :
				null);
	}

	private String buildKeyedPropertyName(String propertyName, Object key) {
		return (propertyName != null ?
				propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + key + PropertyAccessor.PROPERTY_KEY_SUFFIX :
				null);
	}

}
