/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util.visitor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.Cache;

/**
 * Helper implementation of a reflective visitor.
 * <p>
 * <p>
 * To use, call <code>invokeVisitor</code>, passing a Visitor object and the
 * data argument to accept (double-dispatch.) For example:
 * 
 * <pre>
 * 
 *   public String ToStringStyler.styleValue(Object value) { // visit&lt;valueType&gt; callback will be invoked using reflection reflectiveVistorSupport.invokeVisit(this, value) }
 *  
 * </pre>
 * 
 * @author Keith Donald
 * @author Bob Lee
 */
public class ReflectiveVisitorSupport {
    private static final Log logger = LogFactory
            .getLog(ReflectiveVisitorSupport.class);
    private static final String VISIT_METHOD = "visit";
    private static final String VISIT_NULL = "visitNull";

    Cache visitorClassVisitMethods = new Cache() {
        public Object create(Object key) {
            return new ClassVisitMethods((Class)key);
        }
    };

    /** Maps parameter class names to visitor methods. */
    private static final class ClassVisitMethods {
        Cache visitMethodCache = new Cache() {
            public Object create(Object key) {
                if (key == null) {
                    return findNullVisitorMethod();
                }
                Method method = findVisitMethodByClass((Class)key);
                if (method == null) {
                    method = findDefaultVisitMethod();
                }
                return method;
            }
        };
        private Class visitorClass;
        private Method defaultVisitMethod;

        /** Constructor. */
        private ClassVisitMethods(Class visitorClass) {
            this.visitorClass = visitorClass;
        }

        /** Is the method a visitor? */
        public boolean isVisitor(Method method) {
            Class[] params = method.getParameterTypes();
            return (method.getName().equals(VISIT_METHOD)
                    && (params.length == 1) && (params[0] != Object.class));
        }

        /** Gets visitDefault() method. */
        private Method findDefaultVisitMethod() {
            if (defaultVisitMethod != null) {
                return defaultVisitMethod;
            }
            final Class[] args = { Object.class };
            for (Class clazz = visitorClass; clazz != null; clazz = clazz
                    .getSuperclass()) {
                try {
                    return clazz.getDeclaredMethod(VISIT_METHOD, args);
                } catch (NoSuchMethodException e) {
                }
            }
            logger.warn("No default '" + VISIT_METHOD
                    + "' method found.  Returning <null>");
            return null;
        }

        private Method findNullVisitorMethod() {
            for (Class clazz = visitorClass; clazz != null; clazz = clazz
                    .getSuperclass()) {
                try {
                    return clazz.getDeclaredMethod(VISIT_NULL, null);
                } catch (NoSuchMethodException e) {
                }
            }
            return findDefaultVisitMethod();
        }

        /**
         * Gets a visitor method for the specified argument type.
         * 
         * @param clazz
         *            Method parameter type.
         * @return Visitor method with parameter type.
         */
        public Method getVisitMethod(Class argumentClass) {
            // get mapped visit() method.
            return (Method)visitMethodCache.get(argumentClass);
        }

        /**
         * Traverses class hierarchy looking for applicable visit() method.
         */
        private Method findVisitMethodByClass(Class rootClass) {
            // this works by queueing up classes to be processed.
            // not the fastest, but fast enough and very straightforward.
            if (rootClass == Object.class) {
                return null;
            }
            LinkedList classQueue = new LinkedList();
            classQueue.addFirst(rootClass);

            while (!classQueue.isEmpty()) {
                Class clazz = (Class)classQueue.removeLast();
                // check for a visitor method matching this type.
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Looking for method " + VISIT_METHOD + "("
                                + clazz + ")");
                    }
                    return findVisitMethod(visitorClass, clazz);
                } catch (NoSuchMethodException e) {
                    // queue up the super class if it's not of type Object.
                    if (!clazz.isInterface()
                            && (clazz.getSuperclass() != Object.class)) {
                        classQueue.addFirst(clazz.getSuperclass());
                    }

                    // queue up interfaces.
                    Class[] interfaces = clazz.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        classQueue.addFirst(interfaces[i]);
                    }
                }
            }
            // none found, return the default.
            return findDefaultVisitMethod();
        }

        private Method findVisitMethod(Class visitorClass, Class clazz)
                throws NoSuchMethodException {
            try {
                return visitorClass.getDeclaredMethod(VISIT_METHOD,
                        new Class[] { clazz });
            } catch (NoSuchMethodException e) {
                if (visitorClass.getSuperclass() != Object.class) {
                    return findVisitMethod(visitorClass.getSuperclass(), clazz);
                } else {
                    throw e;
                }
            }
        }
    }

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
     *             if the visitor parameter is null.
     * @see org.springframework.utils.visitor.Visitor#visit(java.lang.Object)
     */
    public final Object invokeVisit(Visitor visitor, Object argument) {
        Assert.notNull(visitor);
        if (argument instanceof Visitable) {
            callAccept((Visitable)argument, visitor);
            return null;
        } else {
            Method method = getMethod(visitor.getClass(), argument);
            if (method == null) {
                logger
                        .warn("No method found by reflection for visitor class '"
                                + visitor.getClass()
                                + "' and argument of type '"
                                + (argument != null ? argument.getClass()
                                        : null) + "'");
                return null;
            }
            try {
                Object[] args = null;
                if (argument != null) {
                    args = new Object[] { argument };
                }
                if (!Modifier.isPublic(method.getModifiers())
                        && method.isAccessible() == false) {
                    method.setAccessible(true);
                }
                return method.invoke(visitor, args);

            } catch (Exception e) {
                logger.error("Exception occured dispatching to visitor " + e);
                throw new RuntimeException(e);
            }
        }
    }

    private Method getMethod(Class visitorClass, Object argument) {
        ClassVisitMethods visitMethods = (ClassVisitMethods)visitorClassVisitMethods
                .get(visitorClass);
        return visitMethods.getVisitMethod((argument != null ? argument
                .getClass() : null));
    }

    /**
     * Call the accept(visitor) method on the visitable object, passing in the
     * visitor (the first of the double-dispatch.)
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