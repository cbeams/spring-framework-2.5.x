/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util.visitor;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.Cache;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositeKey;
import org.springframework.util.StringUtils;

/**
 * Helper implementation of a reflective visitor.
 * <p>
 * <p>
 * To use, call <code>invokeVisitor</code>, passing a Visitor object and the
 * data argument to accept (double-dispatch.) For example:
 * 
 * <pre>
 *  public String ToStringStyler.styleValue(Object value) {
 *      // visit&lt;valueType&gt; callback will be invoked
 *         using reflection
 *      reflectiveVistorSupport.invokeVisit(this, value)
 *  }
 * </pre>
 * 
 * @author Keith Donald
 */
public class ReflectiveVisitorSupport {
    private static final Log logger =
        LogFactory.getLog(ReflectiveVisitorSupport.class);
    private static final String VISIT_PREFIX = "visit";
    private static final String VISIT_DEFAULT = "visitObject";
    private static final String VISIT_NULL = "visitNull";
    
    /**
     * Use reflection to call the appropriate visit method on the provided
     * visitor, passing in the specified argument.
     * 
     * If the arg is Visitable, accept(Visitor) will be called instead, and the
     * it will be expected to perform the double-dispatch. Otherwise the visit
     * method will be called directly on the visitor based on the class of the
     * argument.
     * 
     * @param visitor,
     *            The visitor encapsulating the logic to process the argument.
     * @param argument,
     *            The argument to dispatch, or a instanceof Vistable.
     * @throws IllegalArgumentException
     *            if the visitor parameter is null.
     * @see org.springframework.utils.visitor.Visitor#visit(java.lang.Object)
     */
    public final Object invokeVisit(Visitor visitor, Object argument) {
        Assert.notNull(visitor);
        try {
            if (argument instanceof Visitable) {
                callAccept((Visitable)argument, visitor);
                return null;
            } else {
                Method method = getMethod(visitor.getClass(), argument);
                if (method == null) {
                    logger.warn(
                        "No method found by reflection for visitor class "
                            + visitor.getClass()
                            + " and argument '"
                            + (argument != null ? argument.getClass() : null)
                            + "'");
                    return null;
                }
                try {
                    Object[] args = null;
                    if (argument != null) {
                        args = new Object[] { argument };
                    }
                    return method.invoke(visitor, args);
                } catch (Exception e) {
                    throw e;
                }
            }
        } catch (Exception e) {
            logger.error("Exception occured dispatching to visitor " + e);
            throw new RuntimeException(e);
        }
    }
    
    private Method getMethod(Class visitorClass, Object argument) {
        CompositeKey key =
            new CompositeKey(
                    visitorClass,
                    (argument != null ? argument.getClass() : null));
        return (Method)visitorClassVisitMethods.get(key);
    }

    Cache visitorClassVisitMethods = new Cache() {
        public Object create(Object key) {
            Iterator parts = ((CompositeKey)key).parts();
            Class visitorClass = (Class)parts.next();
            Class argumentClass = (Class)parts.next();
            return findMethod(visitorClass, argumentClass);
        }
    };

    private Method findMethod(Class visitorClass, Class argumentClass) {
        if (argumentClass == null) {
            return findNullVisitorMethod(visitorClass);
        }
        Class tempClass = argumentClass;
        Method m = null;
        // Try the superclasses
        while (m == null && tempClass != Object.class) {
            String method = VISIT_PREFIX + ClassUtils.getShortName(tempClass);
            if (tempClass.getDeclaringClass() != null) {
                method = StringUtils.delete(method, ".");
            }
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Looking for class method '"
                        + method
                        + "("
                        + ClassUtils.getShortName(tempClass)
                        + ")");
            }
            try {
                m = visitorClass.getMethod(method, new Class[] { tempClass });
            } catch (NoSuchMethodException e) {
                tempClass = tempClass.getSuperclass();
            }
        }
        // Try the interfaces. If necessary, you
        // can sort them first to define 'visitable' interface wins
        // in case an object implements more than one.
        if (m == null) {
            Class[] interfaces = argumentClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                String method =
                    VISIT_PREFIX + ClassUtils.getShortName(interfaces[i]);
                if (interfaces[i].getDeclaringClass() != null) {
                    method = StringUtils.delete(method, ".");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Looking for interface method '"
                            + method
                            + "("
                            + ClassUtils.getShortName(interfaces[i])
                            + ")");
                }
                try {
                    m =
                        visitorClass.getMethod(
                                method,
                                new Class[] { interfaces[i] });
                } catch (NoSuchMethodException e) {
                }
            }
        }
        if (m == null) {
            m = findDefaultVisitorMethod(visitorClass);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning method '" + m + "'");
        }
        return m;
    }

    private Method findNullVisitorMethod(Class visitorClass) {
        try {
            return visitorClass.getMethod(VISIT_NULL, null);
        } catch (Exception e) {
            return findDefaultVisitorMethod(visitorClass);
        }
    }

    private Method findDefaultVisitorMethod(Class visitorClass) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Looking for default visitor method '"
                        + VISIT_DEFAULT
                        + "'"
                        + "' on class "
                        + visitorClass);
            }
            return visitorClass.getMethod(
                    VISIT_DEFAULT,
                    new Class[] { Object.class });
        } catch (Exception e) {
            logger.warn(
                    "No default '"
                    + VISIT_DEFAULT
                    + "' method found.  Returning <null>");
            return null;
        }
    }
    
    /**
     * Call the accept(visitor) method on the visitable object, passing in
     * the visitor (the first of the double-dispatch.)
     * 
     * @param visitable
     *            The vistable (the type)
     * @param visitor
     *            The visitor (the algorithm)
     */
    protected void callAccept(Visitable visitable, Visitor visitor) {
        visitable.accept(visitor);
    }

}
