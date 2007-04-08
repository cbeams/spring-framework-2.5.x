package org.springframework.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a MVC Controller.
 * <p/>
 * A class with this annotation typically has methods capable of handling web request, such as methods annotated with
 * {@link Url} and {@link WebParam}.
 *
 * @author Arjen Poutsma
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
/**
 * @author Arjen Poutsma
 */
public @interface Controller {

}
