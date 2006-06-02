/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.listener.adapter;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;

/**
 * Unit tests for the {@link MessageListenerAdapter} class.
 *
 * @author Rick Evans
 */
public final class MessageListenerAdapterTests extends TestCase {

    private final String TEXT = "I fancy a good cuppa right now";


    public void testWithMessageContentsDelegateForTextMessage() throws Exception {

        MockControl mockTextMessage = MockControl.createControl(TextMessage.class);
        TextMessage textMessage = (TextMessage) mockTextMessage.getMock();
        // TextMessage contents must be unwrapped...
        textMessage.getText();
        mockTextMessage.setReturnValue(TEXT);
        mockTextMessage.replay();

        MockControl mockDelegate = MockControl.createControl(MessageContentsDelegate.class);
        MessageContentsDelegate delegate = (MessageContentsDelegate) mockDelegate.getMock();
        delegate.handleMessage(TEXT);
        mockDelegate.setVoidCallable();
        mockDelegate.replay();

        MessageListenerAdapter adapter = new MessageListenerAdapter(delegate);
        adapter.onMessage(textMessage);

        mockDelegate.verify();
        mockTextMessage.verify();
    }

    public void testWithMessageContentsDelegateForBytesMessage() throws Exception {

        MockControl mockBytesMessage = MockControl.createControl(BytesMessage.class);
        BytesMessage bytesMessage = (BytesMessage) mockBytesMessage.getMock();
        // BytesMessage contents must be unwrapped...
        bytesMessage.getBodyLength();
        mockBytesMessage.setReturnValue(TEXT.getBytes().length);
        bytesMessage.readBytes(null);
        mockBytesMessage.setMatcher(MockControl.ALWAYS_MATCHER);
        mockBytesMessage.setReturnValue(TEXT.getBytes().length);
        mockBytesMessage.replay();

        MockControl mockDelegate = MockControl.createControl(MessageContentsDelegate.class);
        MessageContentsDelegate delegate = (MessageContentsDelegate) mockDelegate.getMock();
        delegate.handleMessage(TEXT.getBytes());
        mockDelegate.setMatcher(MockControl.ALWAYS_MATCHER);
        mockDelegate.setVoidCallable();
        mockDelegate.replay();

        MessageListenerAdapter adapter = new MessageListenerAdapter(delegate);
        adapter.onMessage(bytesMessage);

        mockDelegate.verify();
        mockBytesMessage.verify();
    }

    public void testWithMessageDelegate() throws Exception {

        MockControl mockTextMessage = MockControl.createControl(TextMessage.class);
        TextMessage textMessage = (TextMessage) mockTextMessage.getMock();
        mockTextMessage.replay();

        MockControl mockDelegate = MockControl.createControl(MessageDelegate.class);
        MessageDelegate delegate = (MessageDelegate) mockDelegate.getMock();
        delegate.handleMessage(textMessage);
        mockDelegate.setVoidCallable();
        mockDelegate.replay();

        MessageListenerAdapter adapter = new MessageListenerAdapter(delegate);
        // we DON'T want the default SimpleMessageConversion happening...
        adapter.setMessageConverter(null);
        adapter.onMessage(textMessage);

        mockDelegate.verify();
        mockTextMessage.verify();
    }

    public void testWhenTheAdapterItselfIsTheDelegate() throws Exception {

        MockControl mockTextMessage = MockControl.createControl(TextMessage.class);
        TextMessage textMessage = (TextMessage) mockTextMessage.getMock();
        // TextMessage contents must be unwrapped...
        textMessage.getText();
        mockTextMessage.setReturnValue(TEXT);
        mockTextMessage.replay();

        StubMessageListenerAdapter adapter = new StubMessageListenerAdapter();
        adapter.onMessage(textMessage);
        assertTrue(adapter.wasCalled());

        mockTextMessage.verify();
    }

    public void testRainyDayWithNoApplicableHandlingMethods() throws Exception {

        MockControl mockTextMessage = MockControl.createControl(TextMessage.class);
        TextMessage textMessage = (TextMessage) mockTextMessage.getMock();
        // TextMessage contents must be unwrapped...
        textMessage.getText();
        mockTextMessage.setReturnValue(TEXT);
        mockTextMessage.replay();

        StubMessageListenerAdapter adapter = new StubMessageListenerAdapter();
        adapter.setDefaultListenerMethod("walnutsRock");
        adapter.onMessage(textMessage);
        assertFalse(adapter.wasCalled());

        mockTextMessage.verify();
    }

    public void testThatAnExceptionThrownFromTheHandlingMethodIsSimplySwallowedByDefault() throws Exception {

        final IllegalArgumentException exception = new IllegalArgumentException();

        MockControl mockTextMessage = MockControl.createControl(TextMessage.class);
        TextMessage textMessage = (TextMessage) mockTextMessage.getMock();
        mockTextMessage.replay();

        MockControl mockDelegate = MockControl.createControl(MessageDelegate.class);
        MessageDelegate delegate = (MessageDelegate) mockDelegate.getMock();
        delegate.handleMessage(textMessage);
        mockDelegate.setThrowable(exception);
        mockDelegate.replay();

        MessageListenerAdapter adapter = new MessageListenerAdapter(delegate) {
            protected void handleListenerException(Throwable ex) {
                assertNotNull("The Throwable passed to the handleListenerException(..) method must never be null.", ex);
                assertTrue("The Throwable passed to the handleListenerException(..) method must be of type [ListenerExecutionFailedException].", ex instanceof ListenerExecutionFailedException);
                ListenerExecutionFailedException lefx = (ListenerExecutionFailedException) ex;
                Throwable cause = lefx.getCause();
                assertNotNull("The cause of a ListenerExecutionFailedException must be preserved.", cause);
                assertSame(exception, cause);
            }
        };
        // we DON'T want the default SimpleMessageConversion happening...
        adapter.setMessageConverter(null);
        adapter.onMessage(textMessage);

        mockDelegate.verify();
        mockTextMessage.verify();
    }

    public void testThatTheDefaultMessageConverterisIndeedTheSimpleMessageConverter() throws Exception {
        MessageListenerAdapter adapter = new MessageListenerAdapter();
        assertNotNull("The default [MessageConverter] must never be null.", adapter.getMessageConverter());
        assertTrue("The default [MessageConverter] must be of the type [SimpleMessageConverter]; if you've just changed it, then change this test to reflect your change.", adapter.getMessageConverter() instanceof SimpleMessageConverter);
    }

    public void testThatWhenNoDelegateIsSuppliedTheDelegateIsAssumedToBeTheMessageListenerAdapterItself() throws Exception {
        MessageListenerAdapter adapter = new MessageListenerAdapter();
        assertSame(adapter, adapter.getDelegate());
    }

    public void testThatTheDefaultMessageHandlingMethodNameIsTheConstantDefault() throws Exception {
        MessageListenerAdapter adapter = new MessageListenerAdapter();
        assertEquals(MessageListenerAdapter.ORIGINAL_DEFAULT_LISTENER_METHOD, adapter.getDefaultListenerMethod());
    }

}
