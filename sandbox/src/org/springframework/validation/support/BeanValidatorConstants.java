/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

/**
 * Constant properties used to store bean validators in bean info descriptors.
 * 
 * @author  Keith Donald
 */
public class BeanValidatorConstants {
    public static final String VALIDATED_PROPERTY = "isValidated";
    public static final String VALIDATOR_PROPERTY = "validator";
    
    private BeanValidatorConstants() {
        
    }
}
