/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.support;

import org.springframework.util.Assert;
import org.springframework.validation.BeanValidationService;

/**
 * @author Keith Donald
 */
public class ValidationServiceSingletonLocator {
    private static BeanValidationService instance;

    private ValidationServiceSingletonLocator() {

    }

    public static BeanValidationService getInstance() {
        Assert.notNull(instance,
                "The singleton validator service has not been loaded.");
        return instance;
    }

    public static void load(BeanValidationService registry) {
        instance = registry;
    }
}
