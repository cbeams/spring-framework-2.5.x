package org.springframework.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method parameter should be bound to a Web request parameter.
 * <p/>
 * The parameter might be required; by default it is not.
 *
 * @author Arjen Poutsma
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebParam {

    /**
     * The request parameter to bind to.
     */
    String value();

    /**
     * Whether the parameter is required.
     */
    boolean required() default false;


}
