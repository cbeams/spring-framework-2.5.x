package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;

/**
 * Helper interface to make publishing application events easier. e.g. decouple code that needs to publish an event
 * programmaticaly from <code>ApplicationContext</code>
 * 
 * <p>
 * An alternative way to publish an ApplicationEvent declaratively is to use <code>EventPublicationInterceptor</code>
 * 
 * @author Dmitriy Kopylenko
 * @since 1.2
 * @see org.springframework.context.event.EventPublicationInterceptor
 * @see org.springframework.context.ApplicationEvent
 */
public interface ApplicationEventPublisher {
    
    /**
     * Publish an application event.
     * @param ApplicationEvent to publish.
     */
    void publishEvent(ApplicationEvent e);

}