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
package org.springframework.jms.converter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Converts between a JavaBean and a MapMessage.
 *
 * @author Mark Pollack
 * @author Jawaid Hakim
 */
public class MapMessageConverter implements Converter {

	//TODO refactoring.

	private static Map beanInfo_ = new HashMap();

	private static Map primitiveTypes = new HashMap();
	private static Map primitiveWrapperTypes = new HashMap();
	private static Map primitiveWrapperTypesArray = new HashMap();
	private static Map primitiveTypesArray = new HashMap();

	private static final Object[] EMPTY_PARAMS = {
	};
	private static String BEANPACKAGE;
	private static final String BEANNAME_FIELD = "csmbeanname__";

	private static final ThreadLocal DATETIME_FORMAT_THREADLOCAL = new ThreadLocal();
	private static final String DEFAULT_PATTERN = "EEE, d MMM yyyy HH:mm:ss z";
	private static String PATTERN = DEFAULT_PATTERN;
	private static boolean lenient = true;

	static {
		primitiveTypes.put("short", "");
		primitiveTypes.put("int", "");
		primitiveTypes.put("long", "");
		primitiveTypes.put("float", "");
		primitiveTypes.put("double", "");
		primitiveTypes.put("byte", "");
		primitiveTypes.put("boolean", "");
		primitiveTypes.put("char", "");

		primitiveWrapperTypes.put("java.lang.Short", "");
		primitiveWrapperTypes.put("java.lang.Integer", "");
		primitiveWrapperTypes.put("java.lang.Long", "");
		primitiveWrapperTypes.put("java.lang.Float", "");
		primitiveWrapperTypes.put("java.lang.Double", "");
		primitiveWrapperTypes.put("java.lang.Byte", "");
		primitiveWrapperTypes.put("java.lang.Boolean", "");
		primitiveWrapperTypes.put("java.lang.Char", "");

		primitiveTypesArray.put("[S", "");
		primitiveTypesArray.put("[I", "");
		primitiveTypesArray.put("[L", "");
		primitiveTypesArray.put("[F", "");
		primitiveTypesArray.put("[D", "");
		primitiveTypesArray.put("[B", "");
		primitiveTypesArray.put("[Z", "");
		primitiveTypesArray.put("[C", "");

		primitiveWrapperTypesArray.put("[Ljava.lang.Short;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Integer;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Long;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Float;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Double;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Byte;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Boolean;", "");
		primitiveWrapperTypesArray.put("[Ljava.lang.Char;", "");
	}

	public Message toMessage(Object object, Session session) {
		try {
			Class beanClass = object.getClass();
			RBeanInfo beanInfo = createBeanInfo(beanClass.getName(), beanClass);

			MapMessage nestedMsg = session.createMapMessage();
			nestedMsg.setString(
			    BEANNAME_FIELD,
			    getBeanClassName(beanInfo.getName()));

			for (Iterator getters = beanInfo.getGetters().values().iterator();
			     getters.hasNext();
			    ) {
				Method method = (Method) getters.next();
				Object val = method.invoke(object, EMPTY_PARAMS);
				if (val != null) {
					String fldName = method.getName().substring(3);
					Class valType = val.getClass();
					String valTypeName = valType.getName();
					if (valType.equals(String.class)) {
						nestedMsg.setObject(fldName, val.toString());
					}
					else if (isPrimitiveWrapperType(valType)) {
						nestedMsg.setObject(fldName, val);
					}
					else if (valType.equals(BigDecimal.class)) {
						nestedMsg.setObject(fldName, val.toString());
					}
					else if (valType.equals(Date.class)) {
						String sVal = getDateTimeFormatter().format((Date) val);
						nestedMsg.setObject(fldName, sVal);
					}
					else if (valType.equals(Timestamp.class)) {
						Timestamp realVal = (Timestamp) val;
						String sVal =
						    getDateTimeFormatter().format(
						        new Date(
						            realVal.getTime()
						            + (realVal.getNanos() / 1000000)));
						nestedMsg.setObject(fldName, sVal);
					}
					else if (valType.isArray()) {
						int len = Array.getLength(val);
						nestedMsg.setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
						Class arrayType = val.getClass();
						if (isPrimitiveArray(arrayType)
						    || isPrimitiveWrapperArray(arrayType)) {
							// TODO: assumes that arrays of primitives and
							// primitive wrappers are supported by the JMS
							// provider
							nestedMsg.setObject(fldName, val);
							continue;
						}
						MapMessage arrayMsg = session.createMapMessage();
						int realLen = 0;
						for (int i = 0; i < len; ++i) {
							Object elem = Array.get(val, i);
							if (elem != null) {
								String index = String.valueOf(i);
								if (elem.getClass().equals(String.class)) {
									arrayMsg.setObject(index, elem);
								}
								else if (valType.equals(BigDecimal.class)) {
									arrayMsg.setObject(index, val.toString());
								}
								else if (
								    elem.getClass().equals(Date.class)) {
									arrayMsg.setObject(
									    index,
									    getDateTimeFormatter().format(
									        (Date) elem));
								}
								else if (
								    elem.getClass().equals(Timestamp.class)) {
									Timestamp realVal = (Timestamp) elem;
									arrayMsg.setObject(
									    index,
									    getDateTimeFormatter().format(
									        new Date(
									            realVal.getTime()
									            + (realVal.getNanos()
									               / 1000000))));
								}
								else {
									// TODO: assumes that this is a bean
									if (realLen == 0)
										arrayMsg.setBooleanProperty(
										    "JMS_TIBCO_MSG_EXT",
										    true);
									arrayMsg.setObject(
									    String.valueOf(realLen),
									    toMessage(elem, session));
								}
								++realLen;
							}
						}
						arrayMsg.setIntProperty("length", realLen);
						nestedMsg.setObject(fldName, arrayMsg);
					}
					else if (val instanceof Message) {
						nestedMsg.setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
						nestedMsg.setObject(fldName, val);
					}
					else if (val instanceof java.util.Collection) {
						// TODO: check why User does not lazy load collections
						// throw new ConverterException("Cannot marshal
						// collection classes");
					}
					else {
						// Handle everything else like a bean
						nestedMsg.setBooleanProperty("JMS_TIBCO_MSG_EXT", true);
						nestedMsg.setObject(fldName, toMessage(val, session));
					}
				}
			}

			return nestedMsg;
		}
		catch (Exception ex) {
			throw new ConversionException("error", ex);
		}
	}

	public Object fromMessage(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	public MapMessageConverter.RBeanInfo createBeanInfo(
	    String name,
	    Class bean) {
		try {
			RBeanInfo info = (RBeanInfo) beanInfo_.get(name);
			if (info != null)
				return info;

			if (bean == null)
				return null;

			BeanInfo bi = Introspector.getBeanInfo(bean);

			Map getters = new HashMap();
			Map setters = new HashMap();
			PropertyDescriptor[] props = bi.getPropertyDescriptors();
			for (int i = 0; i < props.length; ++i) {
				PropertyDescriptor prop = props[i];
				String propName = prop.getName();
				Method readMethod = prop.getReadMethod();
				Method writeMethod = prop.getWriteMethod();
				if (readMethod == null
				    || writeMethod == null
				    || !(!propName.startsWith("get")
				    && !propName.startsWith("set")))
					continue;

				String ucPropName =
				    propName.substring(0, 1).toUpperCase()
				    + propName.substring(1);
				getters.put(ucPropName, readMethod);
				setters.put(ucPropName, writeMethod);
			}

			return new RBeanInfo(name, getters, setters);
		}
		catch (IntrospectionException e) {
			return new RBeanInfo(
			    bean.getClass().getName(),
			    new HashMap(),
			    new HashMap());
		}
	}

	public class RBeanInfo {

		public RBeanInfo(String name, Map getters, Map setters) {
			name_ = name;
			getters_ = getters;
			setters_ = setters;
		}

		public String getName() {
			return name_;
		}

		public Map getGetters() {
			return getters_;
		}

		public Map getSetters() {
			return setters_;
		}

		private final String name_;
		private final Map getters_;
		private final Map setters_;
	}


	/**
	 * Get the date formatter. This returns a Threadsafe <tt>DateFormat</tt>
	 * instance.
	 *
	 * @return Threadsafe <tt>DateFormat</tt> instance. The <tt>TimeZone</tt>
	 *         of the <tt>DateFormat</tt> instance if set to <tt>GMT</tt>
	 *         and the parsing is set to <tt>lenient</tt>.
	 */
	protected static DateFormat getDateTimeFormatter() {
		DateFormat dateFmt = (DateFormat) DATETIME_FORMAT_THREADLOCAL.get();
		if (dateFmt == null) {
			dateFmt = new SimpleDateFormat(getDateFormatPattern());
			dateFmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFmt.setLenient(getLenient());
			DATETIME_FORMAT_THREADLOCAL.set(dateFmt);
		}
		return dateFmt;
	}

	/**
	 * Get the data format pattern. The defaul date format pattern is based on
	 * the Internet Engineering Task Force (IETF) Request for Comments (RFC)
	 * 1123 and is <code>EEE, d MMM yyyy HH:mm:ss z</code>.
	 *
	 * @return Date format pattern
	 */
	public static String getDateFormatPattern() {
		return PATTERN;
	}

	/**
	 * Get the lenient setting for the date formatter. Default locale is <tt>true</tt>.
	 *
	 * @return Lenient setting.
	 */
	public static boolean getLenient() {
		return lenient;
	}

	/**
	 * Determine if a bean class is a primitive wrapper type. A primitive
	 * wrapper type can be directly inserted into a <tt>MapMessage</tt>.
	 *
	 * @param cls
	 *            Bean class.
	 * @return Returns <tt>true</tt> if the bean class is a supported
	 *         non-primitive type. Otherwise, returns <tt>false</tt>.
	 */
	private static boolean isPrimitiveWrapperType(Class cls) {
		String name = cls.getName();
		return primitiveTypes.containsKey(name)
		    || primitiveWrapperTypes.containsKey(name);
	}

	/**
	 * Determine if a bean attribute is an array of <tt>primitive</tt> types.
	 * A primitive array type can be directly inserted into a
	 * <tt>MapMessage</tt>.
	 *
	 * @param cls
	 *            Bean class.
	 * @return Returns <tt>true</tt> if the bean class is an array of
	 *         <tt>primitive</tt> types. Otherwise, returns <tt>false</tt>.
	 */
	private static boolean isPrimitiveArray(Class cls) {
		String name = cls.getName();
		return primitiveTypesArray.containsKey(name);
	}

	/**
	 * Determine if a bean class is an array of primitive wrapper type. A
	 * primitive wrapper array type can be directly inserted into a
	 * <tt>MapMessage</tt>.
	 *
	 * @param cls
	 *            Bean class.
	 * @return Returns <tt>true</tt> if the bean class is an array of
	 *         primitive wrapper types. Otherwise, returns <tt>false</tt>.
	 */
	private static boolean isPrimitiveWrapperArray(Class cls) {
		String name = cls.getName();
		return primitiveWrapperTypesArray.containsKey(name);
	}

	/**
	 * Determine if a bean class is a <tt>date</tt> type. A date type is
	 * marshalled as a <tt>String<<tt/>.
	 *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       *       * @param cls Bean class.
	 * @return Returns <tt>true</tt> if the bean class is a <tt>date</tt>
	 * type. Otherwise, returns <tt>false</tt>.
	 */
	private static boolean isDateType(Class cls) {
		return (cls.equals(Date.class));
	}

	/**
	 * Get the unqualified name of the bean class.
	 *
	 * @param fullName
	 *            Fully qualified name of the bean class. This can also be the
	 *            unqualified name.
	 * @return Unqualified name of the bean class.
	 */
	private static String getBeanClassName(String fullName) {
		int index = fullName.lastIndexOf(".");
		return fullName.substring(index + 1);
	}

}
