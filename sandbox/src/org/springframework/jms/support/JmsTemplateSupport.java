package org.springframework.jms.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jndi.JndiTemplate;
import org.springframework.jms.JmsException;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * This abstract class is to be extended by the actual Jms templates.
 * The purpose of this clas is to consolidate all the common functionality and avoid
 * code duplication.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public abstract class JmsTemplateSupport {
    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------
    protected final Log logger = LogFactory.getLog(getClass());

    private final JndiTemplate jndiTemplate;

    //---------------------------------------------------------------------
    // End of Instance data
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------
    protected JmsTemplateSupport() {
        this.jndiTemplate = new JndiTemplate();
    }

    protected JmsTemplateSupport(final Properties env) {
        this.jndiTemplate = new JndiTemplate(env);
    }
    //---------------------------------------------------------------------
    // End of Constructors
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Protected methods
    //---------------------------------------------------------------------

    /**
     * Looks up any object in the Jndi context. The consumer of this method is responsible
     * for the casting to the actual type expected.
     *
     * @param name Jndi key that will be used to query the context.
     * @return Object that needs to be cast to the actual type.
     * @throws org.springframework.jms.JmsException if nothing is found under the name passed.
     */
    protected final Object lookupJndiResource(final String name) throws JmsException {
        final Object result;

        try {
            result = this.jndiTemplate.lookup(name);
        } catch (NamingException e) {
            throw new JmsException("Could not find jndi resource bound to name '" + name + "'", e);
        }

        return result;
    }

    //---------------------------------------------------------------------
    // End of Protected methods
    //---------------------------------------------------------------------

    /**
     * Inner class to be extended by those who send a message using
     * one of the Jms templates.
     * If extra parameters need to be set, such as the delivery mode, priority or time
     * to live, override any of the getters with your own implementation.
     */
    public abstract static class MessageCreator {
        /**
         * Implement this method to return a message to be sent.
         *
         * @param session
         * @return
         * @throws JMSException
         */
        public abstract Message createMessage(Session session) throws JMSException;

        /**
         * Override this method to return a custom delivery mode.
         *
         * @return delivery mode used by the jms provider to deliver a message.
         */
        public int getDeliveryMode() {
            return Message.DEFAULT_DELIVERY_MODE;
        }

        /**
         * Override this method to return a custom delivery priority.
         *
         * @return priority used by the jms provider to deliver a message.
         */
        public int getPriority() {
            return Message.DEFAULT_PRIORITY;
        }

        /**
         * Override this method to return a custom time to live for a message.
         *
         * @return time to live, how long a message will stay undelivered.
         */
        public long getTimeToLive() {
            return Message.DEFAULT_TIME_TO_LIVE;
        }
    }
}
