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
package org.springframework.jms;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Converts between a JavaBean and a MapMessage.
 * @author Mark Pollack
 * @author Jawaid Hakim
 */
public class MapMessageConverter implements MessageConverter, InitializingBean {

    //TODO refactor to use Spring bean package classes...

    private static Map primitiveTypes = new HashMap();
    private static Map primitiveWrapperTypes = new HashMap();
    private static Map primitiveWrapperTypesArray = new HashMap();
    private static Map primitiveTypesArray = new HashMap();
    private static final Object[] EMPTY_PARAMS = {
    };
    private static final ThreadLocal DATETIME_FORMAT_THREADLOCAL =
        new ThreadLocal();
    private static final ThreadLocal BIGDECIMAL_FORMAT_THREADLOCAL =
        new ThreadLocal();

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

    private Map beanInfo = new HashMap();
    private String dateFormatPattern = "EEE, d MMM yyyy HH:mm:ss z";
    private String extensionPropertyName = "JMS_TIBCO_MSG_EXT";
    private String packageName;
    private String unqualifiedClassnameFieldName = "uqn__";
    private boolean arraySupportEnabled = false;
    private boolean lenient = true;
    private boolean nestedMessageSupportEnabled = true;

    /**
     * Constructor for normal bean usage.
     * @param packageName The package name to prepend to the unqualified name contained in the
     * MapMessage.
     */
    public MapMessageConverter(String packageName) {
        setPackageName(packageName);
    }

    /**
     * Set the support level for Arrays.
     * @param b if true Arrays are supported.
     */
    public void setArraySupportEnabled(boolean b) {
        arraySupportEnabled = b;
    }

    /**
     * Does the JMS provider support use of Array types in a MapMessage.
     * Default value is false.
     * @return true if provider supprts Array types, false otherwise.
     */
    public boolean isArraySupportEnabled() {
        return arraySupportEnabled;
    }

    /**
     * Set the dateformat pattern.
     * @param pattern pattern to use.
     */
    public void setDateFormatPatter(String pattern) {
        dateFormatPattern = pattern;
    }

    /**
     * Get the data format pattern. The default date format pattern is based on
     * the Internet Engineering Task Force (IETF) Request for Comments (RFC)
     * 1123 and is <code>EEE, d MMM yyyy HH:mm:ss z</code>.
     *
     * @return Date format pattern
     */
    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    /**
     * @param string
     */
    public void setExtensionPropertyName(String string) {
        extensionPropertyName = string;
    }

    /**
     * Set the name of a boolean JMS property to set when using MapMessage features
     * that are not specified in the specification but supported by providers, such as
     * nested map messages or arrays.
     * @return Name of the JMS Property to use to flag the message as an extended message.
     */
    public String getExtensionPropertyName() {
        return extensionPropertyName;
    }

    /**
     * Set the lenient setting for the date formatter.
     * @param b true or false.
     */
    public void setLenient(boolean b) {
        lenient = b;
    }

    /**
     * Get the lenient setting for the date formatter. Default locale is <tt>true</tt>.
     *
     * @return Lenient setting.
     */
    public boolean getLenient() {
        return lenient;
    }

    /**
     * Set the support level for nested MapMessages
     * @param b if true, nested messages are supported.
     */
    public void setNestedMessageSupportEnabled(boolean b) {
        nestedMessageSupportEnabled = b;
    }

    /**
     * Does the JMS provider support the use of nested MapMessages.
     * Default value is true.
     * @return true if provider supports nested MapMessages, false otherwise.
     */
    public boolean isNestedMessageSupportEnabled() {
        return nestedMessageSupportEnabled;
    }

    /**
     * Set the package name that will be used to construct a fully
     * qualified classname from the unqualified name contained in
     * the message during unmarshalling.  A trailing '.' is removed if
     * provided.
     *
     * @param name The name of the package to construct a FQN.
     */
    public void setPackageName(String name) {
        if (name.endsWith(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }

        packageName = name;
    }

    /**
     * Get the name of the package that is used to construct a FQN
     * when unmarshalling.
     * @return package name used during unmarshalling.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Set the name of the field that will be used to identify the
     * unqualified classname of the marshalled Java object
     * @param string fieldname containing unqualified classname.
     */
    public void setUnqualifiedClassnameFieldName(String string) {
        unqualifiedClassnameFieldName = string;
    }

    /**
     * Get the name of the field used to idientify the
     * unqualified classname of the marshalled Java object.
     * @return
     */
    public String getUnqualifiedClassnameFieldName() {
        return unqualifiedClassnameFieldName;
    }

    public void afterPropertiesSet() throws Exception {
        if (getPackageName() == null) {
            throw new IllegalArgumentException("packageName is required");
        }
    }

    public MapMessageConverter.RBeanInfo createBeanInfo(
        String name,
        Class bean) {
        try {
            RBeanInfo info = (RBeanInfo) beanInfo.get(name);

            if (info != null) {
                return info;
            }

            if (bean == null) {
                return null;
            }

            BeanInfo bi = Introspector.getBeanInfo(bean);

            Map getters = new HashMap();
            Map setters = new HashMap();
            PropertyDescriptor[] props = bi.getPropertyDescriptors();

            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                String propName = prop.getName();
                Method readMethod = prop.getReadMethod();
                Method writeMethod = prop.getWriteMethod();

                if ((readMethod == null)
                    || (writeMethod == null)
                    || !(!propName.startsWith("get")
                        && !propName.startsWith("set"))) {
                    continue;
                }

                String ucPropName =
                    propName.substring(0, 1).toUpperCase()
                        + propName.substring(1);
                getters.put(ucPropName, readMethod);
                setters.put(ucPropName, writeMethod);
            }

            return new RBeanInfo(name, getters, setters);
        } catch (IntrospectionException e) {
            return new RBeanInfo(
                bean.getClass().getName(),
                new HashMap(),
                new HashMap());
        }
    }

    public Object fromMessage(Message message) {
        if (!(message instanceof MapMessage)) {
            throw new MessageConversionException("Did not provide MapMessageConverter with a MapMessage");
        }

        return unmarshal((MapMessage) message);
    }

    public Message toMessage(Object object, Session session) {
        try {
            Class beanClass = object.getClass();
            RBeanInfo beanInfo = createBeanInfo(beanClass.getName(), beanClass);

            MapMessage nestedMsg = session.createMapMessage();
            nestedMsg.setString(
                getUnqualifiedClassnameFieldName(),
                getBeanClassName(beanInfo.getName()));

            for (Iterator getters = beanInfo.getGetters().values().iterator();
                getters.hasNext();
                ) {
                Method method = (Method) getters.next();
                Object val = method.invoke(object, EMPTY_PARAMS);

                if (val != null) {
                    String fldName = method.getName().substring(3);
                    Class valType = val.getClass();

                    if (valType.equals(String.class)) {
                        nestedMsg.setObject(fldName, val.toString());
                    } else if (isPrimitiveWrapperType(valType)) {
                        nestedMsg.setObject(fldName, val);
                    } else if (valType.equals(BigDecimal.class)) {
                        nestedMsg.setObject(
                            fldName,
                            getBigDecimalFormatter().format(val));
                    } else if (valType.equals(Date.class)) {
                        String sVal = getDateTimeFormatter().format((Date) val);
                        nestedMsg.setObject(fldName, sVal);
                    } else if (valType.equals(Timestamp.class)) {
                        Timestamp realVal = (Timestamp) val;
                        String sVal =
                            getDateTimeFormatter().format(
                                new Date(
                                    realVal.getTime()
                                        + (realVal.getNanos() / 1000000)));
                        nestedMsg.setObject(fldName, sVal);
                    } else if (valType.isArray()) {
                        if (isArraySupportEnabled()) {

                            int len = Array.getLength(val);
                            nestedMsg.setBooleanProperty(
                                getExtensionPropertyName(),
                                true);

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
                                    } else if (
                                        valType.equals(BigDecimal.class)) {
                                        arrayMsg.setObject(
                                            index,
                                            getBigDecimalFormatter().format(
                                                elem));
                                    } else if (
                                        elem.getClass().equals(Date.class)) {
                                        arrayMsg.setObject(
                                            index,
                                            getDateTimeFormatter().format(
                                                (Date) elem));
                                    } else if (
                                        elem.getClass().equals(
                                            Timestamp.class)) {
                                        Timestamp realVal = (Timestamp) elem;
                                        arrayMsg.setObject(
                                            index,
                                            getDateTimeFormatter().format(
                                                new Date(
                                                    realVal.getTime()
                                                        + (realVal.getNanos()
                                                            / 1000000))));
                                    } else {
                                        // TODO: assumes that this is a bean
                                        if (realLen == 0) {
                                            arrayMsg.setBooleanProperty(
                                                getExtensionPropertyName(),
                                                true);
                                        }

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
                    } else if (val instanceof Message) {
                        if (isNestedMessageSupportEnabled()) {
                            nestedMsg.setBooleanProperty(
                                getExtensionPropertyName(),
                                true);
                            nestedMsg.setObject(fldName, val);
                        }
                    } else if (val instanceof java.util.Collection) {
                        // TODO
                        // throw new ConverterException("Cannot marshal
                        // collection classes.");
                    } else {
                        // Handle everything else like a bean
                        nestedMsg.setBooleanProperty(
                            getExtensionPropertyName(),
                            true);
                        nestedMsg.setObject(fldName, toMessage(val, session));
                    }
                }
            }

            return nestedMsg;
        } catch (Exception ex) {
            throw new MessageConversionException("error", ex);
        }
    }

    /**
     * Get the number formatter. This returns a Threadsafe <tt>NumberFormat</tt>
     * instance.
     *
     * @return Threadsafe <tt>DateFormat</tt> instance. The <tt>TimeZone</tt>
     *         of the <tt>DateFormat</tt> instance if set to <tt>GMT</tt>
     *         and the parsing is set to <tt>lenient</tt>.
     */
    protected NumberFormat getBigDecimalFormatter() {
        NumberFormat numberFmt =
            (NumberFormat) BIGDECIMAL_FORMAT_THREADLOCAL.get();

        if (numberFmt == null) {
            numberFmt = NumberFormat.getNumberInstance();
            BIGDECIMAL_FORMAT_THREADLOCAL.set(numberFmt);
        }

        return numberFmt;
    }

    /**
     * Get the date formatter. This returns a Threadsafe <tt>DateFormat</tt>
     * instance.
     *
     * @return Threadsafe <tt>DateFormat</tt> instance. The <tt>TimeZone</tt>
     *         of the <tt>DateFormat</tt> instance if set to <tt>GMT</tt>
     *         and the parsing is set to <tt>lenient</tt>.
     */
    protected DateFormat getDateTimeFormatter() {
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

    private Object unmarshal(MapMessage source) {
        String unqualifiedName;

        try {
            unqualifiedName =
                source.getString(getUnqualifiedClassnameFieldName());
        } catch (JMSException e) {
            throw new MessageConversionException(
                "Could not find field named "
                    + this.unqualifiedClassnameFieldName
                    + "in message when unmarshalling",
                e);
        }

        if (unqualifiedName == null) {
            throw new MessageConversionException("Unqualified classname field not found");
        }

        String fullName = getPackageName() + "." + unqualifiedName;

        return unmarshal(source, unqualifiedName, fullName);
    }

    /**
     * Unmarshal a bean from a JMS message.
     *
     * @param source
     *            JMS message.
     * @param beanName
     *            Short name of bean.
     * @param fullName
     *            Full name of bean (includes the package).
     * @return New bean instance.
     * @throws ConverterException
     */
    private Object unmarshal(
        MapMessage source,
        String beanName,
        String fullName)
        throws MessageConversionException {
        try {
            Object bean = Class.forName(fullName).newInstance();

            // Set bean properties
            RBeanInfo beanInfo = createBeanInfo(beanName, bean.getClass());

            for (Iterator setters = beanInfo.getSetters().values().iterator();
                setters.hasNext();
                ) {
                Method method = (Method) setters.next();
                String fldName = method.getName().substring(3);

                if (source.itemExists(fldName)) {
                    Object val = source.getObject(fldName);
                    Class[] methodParameters = method.getParameterTypes();
                    Class valType = methodParameters[0];

                    if (valType.equals(String.class)) {
                        Object[] parameters = { val.toString()};
                        method.invoke(bean, parameters);
                    } else if (isPrimitiveWrapperType(valType)) {
                        Object[] parameters = { val };
                        method.invoke(bean, parameters);
                    } else if (valType.equals(BigDecimal.class)) {
                        Object[] parameters = { new BigDecimal(val.toString())};
                        method.invoke(bean, parameters);
                    } else if (valType.equals(Date.class)) {
                        Object[] parameters =
                            { getDateTimeFormatter().parse(val.toString())};
                        method.invoke(bean, parameters);
                    } else if (valType.equals(Timestamp.class)) {
                        Object[] parameters =
                            {
                                new Timestamp(
                                    getDateTimeFormatter()
                                        .parse(val.toString())
                                        .getTime())};
                        method.invoke(bean, parameters);
                    } else if (valType.isArray()) {
                        if (isPrimitiveArray(valType)) {
                            if (isArraySupportEnabled()) {
                                Object[] parameters = { val };
                                method.invoke(bean, parameters);
                            }
                        } else if (isPrimitiveWrapperArray(valType)) {
                            if (isArraySupportEnabled()) {
                                Object[] parameters = { val };
                                method.invoke(bean, parameters);
                            }
                        } else {
                            // BigDecimal, String, Date, or Bean array
                            MapMessage arrayMsg = (MapMessage) val;
                            int len = arrayMsg.getIntProperty("length");
                            String arrayClsName = valType.getName();
                            Object newArray =
                                Array.newInstance(
                                    Class.forName(
                                        arrayClsName.substring(
                                            2,
                                            arrayClsName.indexOf(';'))),
                                    len);

                            for (int i = 0; i < len; ++i) {
                                Object elem =
                                    arrayMsg.getObject(String.valueOf(i));

                                if (arrayClsName
                                    .equals("[Ljava.lang.String;")) {
                                    Array.set(newArray, i, elem);
                                } else if (
                                    arrayClsName.equals(
                                        "[Ljava.math.BigDecimal;")) {
                                    Array.set(
                                        newArray,
                                        i,
                                        new BigDecimal(elem.toString()));
                                } else if (
                                    arrayClsName.equals("[Ljava.util.Date;")) {
                                    Array.set(
                                        newArray,
                                        i,
                                        getDateTimeFormatter().parse(
                                            elem.toString()));
                                } else if (
                                    arrayClsName.equals(
                                        "[Ljava.sql.Timestamp;")) {
                                    Array.set(
                                        newArray,
                                        i,
                                        new Timestamp(
                                            getDateTimeFormatter()
                                                .parse(elem.toString())
                                                .getTime()));
                                } else if (elem instanceof MapMessage) {
                                    //TODO assumes that this is a bean
                                    Array.set(
                                        newArray,
                                        i,
                                        unmarshal((MapMessage) elem));
                                }
                            }

                            Object[] parameters = { newArray };
                            method.invoke(bean, parameters);
                        }
                    } else if (val instanceof MapMessage) {
                        if (isNestedMessageSupportEnabled()) {
                            MapMessage msg = (MapMessage) val;
                            String beanFieldName =
                                msg.getString(
                                    getUnqualifiedClassnameFieldName());

                            if (beanFieldName != null) {
                                Object[] parameters = { unmarshal(msg)};
                                method.invoke(bean, parameters);
                            } else {
                                Object[] parameters = { msg };
                                method.invoke(bean, parameters);
                            }
                        }
                    } else {
                        Object[] parameters = { val };
                        method.invoke(bean, parameters);
                    }
                }
            }

            return bean;
        } catch (Exception ex) {
            throw new MessageConversionException(
                "Exception unmarshalling message",
                ex);
        }
    }

    public class RBeanInfo {
        private final Map getters_;
        private final Map setters_;
        private final String name_;

        public RBeanInfo(String name, Map getters, Map setters) {
            name_ = name;
            getters_ = getters;
            setters_ = setters;
        }

        public Map getGetters() {
            return getters_;
        }

        public String getName() {
            return name_;
        }

        public Map getSetters() {
            return setters_;
        }
    }
}
