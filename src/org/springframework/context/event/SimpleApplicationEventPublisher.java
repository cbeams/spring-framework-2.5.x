package org.springframework.context.event;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * Simple {@link ApplicationEventPublisher}that uses ApplicationContext to publish events.
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 * @see org.springframework.context.ApplicationContext
 */
public class SimpleApplicationEventPublisher implements ApplicationEventPublisher, ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * @see org.springframework.context.event.ApplicationEventPublisher#publishEvent(org.springframework.context.ApplicationEvent)
     */
    public final void publishEvent(ApplicationEvent e) {
        if(e == null){
            throw new IllegalArgumentException("Cannot publish null ApplicationEvent.");
        }
        this.applicationContext.publishEvent(e);
    }
}