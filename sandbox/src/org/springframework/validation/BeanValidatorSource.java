/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

/**
 * Encapsulates a method of loading bean validators from a source. Sources
 * are expected to access their configuration medium when requested and
 * configure all validators for a bean and place them in the bean's
 * corresponding <code>BeanInfo</code>.
 * <p>
 * <p>
 * Specifically, a source should mark each <code>BeanDescriptor</code> with a
 * <code>validated=(true|false)</code> value. If a bean is validated, each
 * validated <code>PropertyDescriptor</code> should also have a <code>validated=(true|false)</code>
 * value. For validated propeties, the validator should be stored in the <code>validator</code>
 * value of the corresponding PropertyDescriptor.
 * 
 * @author Keith Donald
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
