/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

/**
 * Encapsulates a method of loading bean validators from a source. Sources are
 * expected to access their configuration medium when requested and configure
 * all validators for a bean and place them in the bean's corresponding <code>BeanInfo</code>.
 * <p>
 * <p>
 * Specifically, a source should mark each <code>BeanDescriptor</code> with a
 * <code>BeanConstants.IS_VALIDATED=(true||false)</code> property value.
 * 
 * If a bean is validateable, each validated <code>PropertyDescriptor</code>
 * should also have a <code>BeanConstants.IS_VALIDATED=(true||false)</code>
 * value. For validateable properties, each <code>PropertyValidator</code>
 * should be stored in the <code>BeanConstants.VALIDATOR</code> property of
 * the corresponding PropertyDescriptor.
 * 
 * @author Keith Donald
 * @see javax.beans.BeanInfo#getBeanDescriptor()
 * @see javax.beans.PropertyDescriptor#getPropertyDescriptor()
 */
public interface BeanValidatorSource {

    /**
     * Loads all validators for the specified bean class. This should reload
     * validation information from the underlying configuration source on each
     * call.
     * 
     * @param beanType
     *            The bean type (class or interface.)
     */
    public void loadValidators(Class beanType);
}
