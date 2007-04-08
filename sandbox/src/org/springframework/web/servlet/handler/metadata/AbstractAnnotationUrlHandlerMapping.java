package org.springframework.web.servlet.handler.metadata;

import java.lang.annotation.Annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.annotation.Controller;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;

/**
 * Abstract base for {@link org.springframework.web.servlet.HandlerMapping} implementations that map classes tagged with
 * an annotation.
 * <p/>
 * By default the annotation is {@link Controller}, but this can be overriden in subclasses.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractAnnotationUrlHandlerMapping extends AbstractHandlerMethodMapping
        implements BeanPostProcessor {

    public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getAnnotation(getHandlerAnnotationType()) != null) {
            registerHandlerMethods(bean);
        }
        return bean;
    }

    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Returns the 'handler' annotation type. Default is {@link Controller}.
     */
    protected Class<? extends Annotation> getHandlerAnnotationType() {
        return Controller.class;
    }
}
