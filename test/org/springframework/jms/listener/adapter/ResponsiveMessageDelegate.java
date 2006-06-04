package org.springframework.jms.listener.adapter;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.io.Serializable;
import java.util.Map;

/**
 * See the MessageListenerAdapterTests class for usage.
 *
 * @author Rick Evans
 */
public interface ResponsiveMessageDelegate {

    String handleMessage(TextMessage message);

    Map handleMessage(MapMessage message);

    byte[] handleMessage(BytesMessage message);

    Serializable handleMessage(ObjectMessage message);

}
