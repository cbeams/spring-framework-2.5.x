package org.springframework.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used on handler methods to allow for automatic URL mappings.
 * <p/>
 * The url(s) should be the path in the current application, such as /foo.cgi. If there is no leading "/", one will be
 * prepended.
 *
 * @author Arjen Poutsma
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Url {

    /**
     * The path mapping URL(s).
     */
    String[] value();

}
