/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

/**
 * Constant properties used to store bean property validators in BeanInfo
 * descriptors.
 * 
 * @author Keith Donald
 */
public class BeanValidatorConstants {

    /**
     * Property indicating whether this feature (bean or property) is
     * validated.
     */
    public static final String VALIDATED_PROPERTY = "isValidated";

    /**
     * Property where the actual PropertyValidator is stored.
     */
    public static final String VALIDATOR_PROPERTY = "validator";

    private BeanValidatorConstants() {

    }
}
