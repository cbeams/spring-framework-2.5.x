/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans;

import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.PropertyValuesEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the BeanWrapper interface that should be sufficient
 * for all normal uses. Caches introspection results for efficiency.
 *
 * <p>Note: this class never tries to load a class by name, as this can pose
 * class loading problems in J2EE applications with multiple deployment modules.
 * For example, loading a class by name won't work in some application servers
 * if the class is used in a WAR but was loaded by the EJB class loader and the
 * class to be loaded is in the WAR. (This class would use the EJB class loader,
 * which couldn't see the required class.) We don't attempt to solve such problems
 * by obtaining the classloader at runtime, because this violates the EJB
 * programming restrictions.
 *
 * <p>Note: Regards property editors in org.springframework.beans.propertyeditors.
 * Also explictly register the default ones to care for JREs that do not use
 * the thread context class loader for editor search paths.
 * Applications can either use a standard PropertyEditorManager to register a
 * custom editor before using a BeanWrapperImpl instance, or call the instance's
 * registerCustomEditor method to register an editor for the particular instance.
 *
 * <p>Collections custom property editors can be written against comma delimited String
 * as String arrays are converted in such a format if the array itself is not assignable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 15 April 2001
 * @version $Id: BeanWrapperImpl.java,v 1.17 2003-11-25 16:19:32 johnsonr Exp $
 * @see #registerCustomEditor
 * @see java.beans.PropertyEditorManager
 */
public class BeanWrapperImpl implements BeanWrapper {

	/** We'll create a lot of these objects, so we don't want a new logger every time */
	private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

	/** Registry for default PropertyEditors */
	private static final Map defaultEditors = new HashMap();

	static {
		// install default property editors
		try {
			// this one can't apply the <ClassName>Editor naming pattern
			PropertyEditorManager.registerEditor(String[].class, StringArrayPropertyEditor.class);
			// register all editors in our standard package
			PropertyEditorManager.setEditorSearchPath(new String[]{
				"sun.beans.editors",
				"org.springframework.beans.propertyeditors"
			});
		}
		catch (SecurityException ex) {
			// e.g. in applets -> log and proceed
			logger.warn("Cannot register property editors with PropertyEditorManager", ex);
		}

		// register default editors in this class, for restricted environments
		// where the above threw a SecurityException, and for JDKs that don't
		// use the thread context class loader for property editor lookup
		defaultEditors.put(String[].class, new StringArrayPropertyEditor());
		defaultEditors.put(PropertyValues.class, new PropertyValuesEditor());
		defaultEditors.put(Properties.class, new PropertiesEditor());
		defaultEditors.put(Class.class, new ClassEditor());
		defaultEditors.put(Locale.class, new LocaleEditor());
	}


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** The wrapped object */
	private Object object;

	/** The nested path of the object */
	private String nestedPath = "";

	/* Map with cached nested BeanWrappers */
	private Map nestedBeanWrappers;

	/** Map with custom PropertyEditor instances */
	private Map customEditors;

	/**
	 * Cached introspections results for this object, to prevent encountering the cost
	 * of JavaBeans introspection every time.
	 */
	private CachedIntrospectionResults cachedIntrospectionResults;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
	 * @see #setWrappedInstance
	 */
	public BeanWrapperImpl() {
	}

	/**
	 * Create new BeanWrapperImpl for the given object.
	 * @param object object wrapped by this BeanWrapper.
	 * @throws BeansException if the object cannot be wrapped by a BeanWrapper
	 */
	public BeanWrapperImpl(Object object) throws BeansException {
		setWrappedInstance(object);
	}

	/**
	 * Create new BeanWrapperImpl for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper.
	 * @param nestedPath the nested path of the object
	 * @throws BeansException if the object cannot be wrapped by a BeanWrapper
	 */
	public BeanWrapperImpl(Object object, String nestedPath) throws BeansException {
		setWrappedInstance(object);
		this.nestedPath = nestedPath;
	}

	/**
	 * Create new BeanWrapperImpl, wrapping a new instance of the specified class.
	 * @param clazz class to instantiate and wrap
	 * @throws BeansException if the class cannot be wrapped by a BeanWrapper
	 */
	public BeanWrapperImpl(Class clazz) throws BeansException {
		setWrappedInstance(BeanUtils.instantiateClass(clazz));
	}


	//---------------------------------------------------------------------
	// Implementation of BeanWrapper
	//---------------------------------------------------------------------

	/**
	 * Switches the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 * @throws BeansException if the object cannot be changed
	 */
	public void setWrappedInstance(Object object) throws BeansException {
		if (object == null)
			throw new FatalBeanException("Cannot set BeanWrapperImpl target to a null object", null);
		this.object = object;
		if (this.cachedIntrospectionResults == null ||
		    !this.cachedIntrospectionResults.getBeanClass().equals(object.getClass())) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(object.getClass());
		}
		// assert: cachedIntrospectionResults != null
	}

	public void newWrappedInstance() throws BeansException {
		this.object = BeanUtils.instantiateClass(getWrappedClass());
	}

	public Class getWrappedClass() {
		return object.getClass();
	}

	public Object getWrappedInstance() {
		return object;
	}


	public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
		if (propertyPath != null) {
			List bws = getBeanWrappersForPropertyPath(propertyPath);
			for (Iterator it = bws.iterator(); it.hasNext();) {
				BeanWrapperImpl bw = (BeanWrapperImpl) it.next();
				bw.doRegisterCustomEditor(requiredType, getFinalPath(propertyPath), propertyEditor);
			}
		}
		else {
			doRegisterCustomEditor(requiredType, propertyPath, propertyEditor);
		}
	}

	private synchronized void doRegisterCustomEditor(Class requiredType, String propertyName, PropertyEditor propertyEditor) {
		if (this.customEditors == null) {
			this.customEditors = new HashMap();
		}
		if (propertyName != null) {
			// consistency check
			PropertyDescriptor descriptor = getPropertyDescriptor(propertyName);
			if (requiredType != null && !descriptor.getPropertyType().isAssignableFrom(requiredType)) {
				throw new IllegalArgumentException("Types do not match: required [" + requiredType.getName() +
																					 "], found [" + descriptor.getPropertyType().getName() + "]");
			}
			this.customEditors.put(propertyName, propertyEditor);
		}
		else {
			if (requiredType == null) {
				throw new IllegalArgumentException("No propertyName and no requiredType specified");
			}
			this.customEditors.put(requiredType, propertyEditor);
		}
	}

	public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
		if (propertyPath != null) {
			BeanWrapperImpl bw = getBeanWrapperForPropertyPath(propertyPath);
			return bw.doFindCustomEditor(requiredType, getFinalPath(propertyPath));
		}
		else {
			return doFindCustomEditor(requiredType, propertyPath);
		}
	}

	private synchronized PropertyEditor doFindCustomEditor(Class requiredType, String propertyName) {
		if (this.customEditors == null) {
			return null;
		}
		if (propertyName != null) {
			// check property-specific editor first
			PropertyDescriptor descriptor = getPropertyDescriptor(propertyName);
			PropertyEditor editor = (PropertyEditor) this.customEditors.get(propertyName);
			if (editor != null) {
				// consistency check
				if (requiredType != null) {
					if (!descriptor.getPropertyType().isAssignableFrom(requiredType)) {
						throw new IllegalArgumentException("Types do not match: required=" + requiredType.getName() +
																							 ", found=" + descriptor.getPropertyType());
					}
				}
				return editor;
			}
			else {
				if (requiredType == null) {
					// try property type
					requiredType = descriptor.getPropertyType();
				}
			}
		}
		// no property-specific editor -> check type-specific editor
		return (PropertyEditor) this.customEditors.get(requiredType);
	}


	/**
	 * Is the property nested? That is, does it contain the nested
	 * property separator (usually ".").
	 * @param path property path
	 * @return boolean is the property nested
	 */
	private boolean isNestedProperty(String path) {
		return path.indexOf(NESTED_PROPERTY_SEPARATOR) != -1;
	}

	/**
	 * Get the last component of the path. Also works if not nested.
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
	private String getFinalPath(String nestedPath) {
		String finalPath = nestedPath.substring(nestedPath.lastIndexOf(NESTED_PROPERTY_SEPARATOR) + 1);
		if (logger.isDebugEnabled() && !nestedPath.equals(finalPath)) {
			logger.debug("Final path in nested property value '" + nestedPath + "' is '" + finalPath + "'");
		}
		return finalPath;
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	private BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) {
		int pos = propertyPath.indexOf(NESTED_PROPERTY_SEPARATOR);
		// Handle nested properties recursively
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			logger.debug("Navigating to nested property '" + nestedProperty + "' of property path '" + propertyPath + "'");
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
		}
		else {
			return this;
		}
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	private List getBeanWrappersForPropertyPath(String propertyPath) {
		List beanWrappers = new ArrayList();
		int pos = propertyPath.indexOf(NESTED_PROPERTY_SEPARATOR);
		// Handle nested properties recursively
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			if (nestedProperty.indexOf('[') == -1) {
				Class propertyType = getPropertyDescriptor(nestedProperty).getPropertyType();
				if (propertyType.isArray()) {
					Object[] array = (Object[]) getPropertyValue(nestedProperty);
					for (int i = 0; i < array.length; i++) {
						beanWrappers.addAll(
								getBeanWrappersForNestedProperty(propertyPath, nestedProperty + "[" + i + "]", nestedPath));
					}
					return beanWrappers;
				}
				else if (List.class.isAssignableFrom(propertyType)) {
					List list = (List) getPropertyValue(nestedProperty);
					for (int i = 0; i < list.size(); i++) {
						beanWrappers.addAll(
								getBeanWrappersForNestedProperty(propertyPath, nestedProperty + "[" + i + "]", nestedPath));
					}
					return beanWrappers;
				}
				else if (Map.class.isAssignableFrom(propertyType)) {
					Map map = (Map) getPropertyValue(nestedProperty);
					for (Iterator it = map.keySet().iterator(); it.hasNext();) {
						beanWrappers.addAll(
								getBeanWrappersForNestedProperty(propertyPath, nestedProperty + "[" + it.next() + "]", nestedPath));
					}
					return beanWrappers;
				}
			}
			beanWrappers.addAll(getBeanWrappersForNestedProperty(propertyPath, nestedProperty, nestedPath));
			return beanWrappers;
		}
		else {
			beanWrappers.add(this);
			return beanWrappers;
		}
	}

	private List getBeanWrappersForNestedProperty(String propertyPath, String nestedProperty, String nestedPath) {
		logger.debug("Navigating to nested property '" + nestedProperty + "' of property path '" + propertyPath + "'");
		BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
		return nestedBw.getBeanWrappersForPropertyPath(nestedPath);
	}

	/**
	 * Retrieve a BeanWrapper for the given nested property.
	 * Create a new one if not found in the cache.
	 * <p>Note: Caching nested BeanWrappers is necessary now,
	 * to keep registered custom editors for nested properties.
	 * @param nestedProperty property to create the BeanWrapper for
	 * @return the BeanWrapper instance, either cached or newly created
	 */
	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) {
		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap();
		}
		// get value of bean property
		String[] tokens = getPropertyNameTokens(nestedProperty);
		Object propertyValue = getPropertyValue(tokens[0], tokens[1], tokens[2]);
		String canonicalName = tokens[0];
		if (propertyValue == null) {
			throw new NullValueInNestedPathException(getWrappedClass(), canonicalName);
		}

		// lookup cached sub-BeanWrapper, create new one if not found
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(canonicalName);
		if (nestedBw == null) {
			logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
			nestedBw = new BeanWrapperImpl(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
			// inherit all type-specific PropertyEditors
			if (this.customEditors != null) {
				for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					if (key instanceof Class) {
						Class requiredType = (Class) key;
						PropertyEditor propertyEditor = (PropertyEditor) this.customEditors.get(key);
						nestedBw.registerCustomEditor(requiredType, null, propertyEditor);
					}
				}
			}
			this.nestedBeanWrappers.put(canonicalName, nestedBw);
		}
		else {
			logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
		}
		return nestedBw;
	}

	private String[] getPropertyNameTokens(String propertyName) {
		String actualName = propertyName;
		String key = null;
		int keyStart = propertyName.indexOf('[');
		if (keyStart != -1 && propertyName.endsWith("]")) {
			actualName = propertyName.substring(0, keyStart);
			key = propertyName.substring(keyStart + 1, propertyName.length() - 1);
			if (key.startsWith("'") && key.endsWith("'")) {
				key = key.substring(1, key.length() - 1);
			}
			else if (key.startsWith("\"") && key.endsWith("\"")) {
				key = key.substring(1, key.length() - 1);
			}
		}
		String canonicalName = actualName;
		if (key != null) {
			canonicalName += "[" + key + "]";
		}
		return new String[] {canonicalName, actualName, key};
	}


	public Object getPropertyValue(String propertyName) throws BeansException {
		if (isNestedProperty(propertyName)) {
			BeanWrapper nestedBw = getBeanWrapperForPropertyPath(propertyName);
			return nestedBw.getPropertyValue(getFinalPath(propertyName));
		}

		String[] tokens = getPropertyNameTokens(propertyName);
		String canonicalName = tokens[0];
		String actualName = tokens[1];
		String key = tokens[2];

		return getPropertyValue(canonicalName, actualName, key);
	}

	private Object getPropertyValue(String propertyName, String actualName, String key) {
		PropertyDescriptor pd = getPropertyDescriptor(actualName);
		Method readMethod = pd.getReadMethod();
		if (readMethod == null) {
			throw new FatalBeanException("Cannot get property '" + actualName + "': not readable", null);
		}
		if (logger.isDebugEnabled())
			logger.debug("About to invoke read method [" + readMethod +
			             "] on object of class [" + this.object.getClass().getName() + "]");
		try {
			Object value = readMethod.invoke(this.object, null);
			if (key != null) {
				if (value == null) {
					throw new FatalBeanException("Cannot access indexed value in property referenced in indexed property path '" +
					                             propertyName + "': returned null");
				}
				else if (value.getClass().isArray()) {
					Object[] array = (Object[]) value;
					return array[Integer.parseInt(key)];
				}
				else if (value instanceof List) {
					List list = (List) value;
					return list.get(Integer.parseInt(key));
				}
				else if (value instanceof Map) {
					Map map = (Map) value;
					return map.get(key);
				}
				else {
					throw new FatalBeanException("Property referenced in indexed property path '" + propertyName +
					                             "' is neither an array nor a List nor a Map; returned value was [" + value + "]");
				}
			}
			else {
				return value;
			}
		}
		catch (InvocationTargetException ex) {
			throw new FatalBeanException("Getter for property '" + actualName + "' threw exception", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to get property '" + actualName + "' threw exception", ex);
		}
		catch (IndexOutOfBoundsException ex) {
			throw new FatalBeanException("Index of out of bounds in property path '" + propertyName + "'", ex);
		}
		catch (NumberFormatException ex) {
			throw new FatalBeanException("Invalid index in property path '" + propertyName + "'");
		}
	}

	public void setPropertyValue(String propertyName, Object value) throws PropertyVetoException, BeansException {
		setPropertyValue(new PropertyValue(propertyName, value));
	}

	/**
	 * Set an individual field. All other setters go through this.
	 * @param pv property value to use for update
	 * @throws PropertyVetoException if a listeners throws a JavaBeans API veto
	 * @throws BeansException if there's a low-level, fatal error
	 */
	public void setPropertyValue(PropertyValue pv) throws PropertyVetoException, BeansException {
		if (isNestedProperty(pv.getName())) {
			try {
				BeanWrapper nestedBw = getBeanWrapperForPropertyPath(pv.getName());
				nestedBw.setPropertyValue(new PropertyValue(getFinalPath(pv.getName()), pv.getValue()));
				return;
			}
			catch (NullValueInNestedPathException ex) {
				// let this through
				throw ex;
			}
			catch (FatalBeanException ex) {
				// error in the nested path
				throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
			}
		}

		if (!isWritableProperty(pv.getName())) {
			throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
		}
		PropertyDescriptor pd = getPropertyDescriptor(pv.getName());
		Method writeMethod = pd.getWriteMethod();
		Object newValue = null;

		try {
			// old value may still be null
			newValue = doTypeConversionIfNecessary(pv.getName(), null, pv.getValue(), pd.getPropertyType());

			if (pd.getPropertyType().isPrimitive() &&
					(newValue == null || "".equals(newValue))) {
				throw new IllegalArgumentException("Invalid value [" + pv.getValue() + "] for property '" +
							pd.getName() + "' of primitive type [" + pd.getPropertyType() + "]");
			}

			if (logger.isDebugEnabled()) {
				logger.debug("About to invoke write method [" + writeMethod +
				             "] on object of class [" + object.getClass().getName() + "]");
			}
			
			writeMethod.invoke(this.object, new Object[] { newValue });
			if (logger.isDebugEnabled()) {
				String msg = "Invoked write method [" + writeMethod + "] with value ";
				// only cause toString invocation of new value in case of simple property
				if (newValue == null || BeanUtils.isSimpleProperty(pd.getPropertyType())) {
					logger.debug(msg + "[" + newValue + "]");
				}
				else {
					logger.debug(msg + "of type [" + pd.getPropertyType().getName() + "]");
				}
			}
		}
		catch (InvocationTargetException ex) {
			// Create this lazily
			// TODO could consider getting rid of PropertyChangeEvents as exception parameters
			// as they can never contain anything but null for the old value as we no longer
			// support event propagation
			PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.object, this.nestedPath + pv.getName(), null, newValue);
			if (ex.getTargetException() instanceof PropertyVetoException) {
				throw (PropertyVetoException) ex.getTargetException();
			}
			else if (ex.getTargetException() instanceof ClassCastException) {
				throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
			}
			else {
				throw new MethodInvocationException(ex.getTargetException(), propertyChangeEvent);
			}
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to set property [" + pv + "] threw exception", ex);
		}
		catch (IllegalArgumentException ex) {
			PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.object, this.nestedPath + pv.getName(), null, newValue);
			throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
		}
	}

	/**
	 * Bulk update from a Map.
	 * Bulk updates from PropertyValues are more powerful: this method is
	 * provided for convenience.
	 * @param map map containing properties to set, as name-value pairs.
	 * The map may include nested properties.
	 * @throws BeansException if there's a fatal, low-level exception
	 */
	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, null);
	}

	public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown,
									PropertyValuesValidator pvsValidator) throws BeansException {
		// Create only if needed
		PropertyVetoExceptionsException propertyVetoExceptionsException = new PropertyVetoExceptionsException(this);

		if (pvsValidator != null) {
			try {
				pvsValidator.validatePropertyValues(propertyValues);
			}
			catch (InvalidPropertyValuesException ipvex) {
				propertyVetoExceptionsException.addMissingFields(ipvex);
			}
		}

		PropertyValue[] pvs = propertyValues.getPropertyValues();
		for (int i = 0; i < pvs.length; i++) {
			try {
				// This method may throw ReflectionException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions
				setPropertyValue(pvs[i]);
			}
					// Fatal ReflectionExceptions will just be rethrown
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown)
					throw ex;
				// Otherwise, just ignore it and continue...
			}
			catch (PropertyVetoException ex) {
				propertyVetoExceptionsException.addPropertyVetoException(ex);
			}
			catch (TypeMismatchException ex) {
				propertyVetoExceptionsException.addTypeMismatchException(ex);
			}
			catch (MethodInvocationException ex) {
				propertyVetoExceptionsException.addMethodInvocationException(ex);
			}
		}

		// If we encountered individual exceptions, throw the composite exception
		if (propertyVetoExceptionsException.getExceptionCount() > 0) {
			throw propertyVetoExceptionsException;
		}
	}


	private PropertyChangeEvent createPropertyChangeEvent(String propertyName, Object oldValue, Object newValue)
			throws BeansException {
		return new PropertyChangeEvent((this.object != null ? this.object : "constructor"),
		                               (propertyName != null ? this.nestedPath + propertyName : null),
																	 oldValue, newValue);
	}

	/**
	 * Convert the value to the required type (if necessary from a String).
	 * Conversions from String to any type use the setAsText() method of
	 * the PropertyEditor class. Note that a PropertyEditor must be registered
	 * for this class for this to work. This is a standard Java Beans API.
	 * A number of property editors are automatically registered by this class.
	 * @param propertyName name of the property
	 * @param oldValue previous value, if available. May be null.
	 * @param newValue proposed change value.
	 * @param requiredType type we must convert to
	 * @throws BeansException if there is an internal error
	 * @return new value, possibly the result of type convertion.
	 */
	public Object doTypeConversionIfNecessary(String propertyName, Object oldValue, Object newValue,
																						Class requiredType) throws BeansException {
		if (newValue instanceof List && requiredType.isArray()) {
			List list = (List) newValue;
			Class componentType = requiredType.getComponentType();
			try {
				Object[] arr = (Object[]) Array.newInstance(componentType, list.size());
				for (int i = 0; i < list.size(); i++) {
					arr[i] = doTypeConversionIfNecessary(propertyName, null, list.get(i), componentType);
				}
				return arr;
			}
			catch (ArrayStoreException ex) {
				throw new TypeMismatchException(createPropertyChangeEvent(propertyName, oldValue, newValue), requiredType, ex);
			}
		}

		else if (newValue != null) {
			// Custom editor for this type?
			PropertyEditor pe = findCustomEditor(requiredType, propertyName);
			// Value not of required type?
			if ((pe != null || !requiredType.isAssignableFrom(newValue.getClass()))) {

				if (newValue instanceof String[]) {
					// Convert String array to String
					newValue = StringUtils.arrayToCommaDelimitedString((String[])newValue);
					if (logger.isDebugEnabled())
						logger.debug("Convert: StringArray to CommaDelimitedString [" + newValue + "]");
				}

				if (newValue instanceof String) {
					// Use PropertyEditor's setAsText in case of a String value
					if (logger.isDebugEnabled())
						logger.debug("Convert: String to [" + requiredType + "]");
					if (pe == null) {
						// No custom editor -> check BeanWrapper's default editors
						pe = (PropertyEditor) defaultEditors.get(requiredType);
						if (pe == null) {
							// No BeanWrapper default editor -> check standard JavaBean editors
							pe = PropertyEditorManager.findEditor(requiredType);
						}
					}
					if (logger.isDebugEnabled())
						logger.debug("Using property editor [" + pe + "]");
					if (pe != null) {
						try {
							pe.setAsText((String) newValue);
							newValue = pe.getValue();
						}
						catch (IllegalArgumentException ex) {
							throw new TypeMismatchException(createPropertyChangeEvent(propertyName, oldValue, newValue), requiredType, ex);
						}
					}
				}

				else if (pe != null) {
					// Not a String -> use PropertyEditor's setValue
					// With standard PropertyEditors, this will return the very same object;
					// we just want to allow special PropertyEditors to override setValue
					// for type conversion from non-String values to the required type.
					try {
						pe.setValue(newValue);
						newValue = pe.getValue();
					}
					catch (IllegalArgumentException ex) {
						throw new TypeMismatchException(createPropertyChangeEvent(propertyName, oldValue, newValue), requiredType, ex);
					}
				}
			}
		}
		return newValue;
	}


	public PropertyDescriptor[] getPropertyDescriptors() {
		return cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		if (propertyName == null)
			throw new FatalBeanException("Can't find property descriptor for null property");
			
		if (isNestedProperty(propertyName)) {
			BeanWrapper nestedBw = getBeanWrapperForPropertyPath(propertyName);
			return nestedBw.getPropertyDescriptor(getFinalPath(propertyName));
		}
		return this.cachedIntrospectionResults.getPropertyDescriptor(propertyName);
	}

	public boolean isReadableProperty(String propertyName) {
		// This is a programming error, although asking for a property
		// that doesn't exist is not
		if (propertyName == null)
			throw new FatalBeanException("Can't find readability status for null property");
		
		try {
			return getPropertyDescriptor(propertyName).getReadMethod() != null;
		}
		catch (BeansException ex) {
			// Doesn't exist, so can't be readable
			return false;
		}
	}

	public boolean isWritableProperty(String propertyName) {
		// This is a programming error, although asking for a property
		// that doesn't exist is not
		if (propertyName == null)
			throw new FatalBeanException("Can't find writability status for null property");
			 
		try {
			return getPropertyDescriptor(propertyName).getWriteMethod() != null;
		}
		catch (BeansException ex) {
			// Doesn't exist, so can't be writable
			return false;
		}
	}


	public Object invoke(String methodName, Object[] args) throws BeansException {
		try {
			MethodDescriptor md = this.cachedIntrospectionResults.getMethodDescriptor(methodName);
			if (logger.isDebugEnabled())
				logger.debug("About to invoke method '" + methodName + "'");
			Object returnVal = md.getMethod().invoke(this.object, args);
			if (logger.isDebugEnabled())
				logger.debug("Successfully invoked method '" + methodName + "'");
			return returnVal;
		}
		catch (InvocationTargetException ex) {
			//if (ex.getTargetException() instanceof ClassCastException)
			//	throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
			throw new MethodInvocationException(ex.getTargetException(), methodName);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to invoke method '" + methodName + "' threw exception", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new FatalBeanException("Illegal argument to method '" + methodName + "' threw exception", ex);
		}
	}


	//---------------------------------------------------------------------
	// Diagnostics
	//---------------------------------------------------------------------

	/**
	 * This method is expensive! Only call for diagnostics and debugging reasons,
	 * not in production.
	 * @return a string describing the state of this object
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("BeanWrapperImpl:"
								+ " wrapping class [" + getWrappedInstance().getClass().getName() + "]; ");
			PropertyDescriptor pds[] = getPropertyDescriptors();
			if (pds != null) {
				for (int i = 0; i < pds.length; i++) {
					Object val = getPropertyValue(pds[i].getName());
					String valStr = (val != null) ? val.toString() : "null";
					sb.append(pds[i].getName() + "={" + valStr + "}");
				}
			}
		}
		catch (Exception ex) {
			sb.append("exception encountered: " + ex);
		}
		return sb.toString();
	}

}
