package org.springframework.jms.listener.adapter;

/**
 * Stub extension of the {@link MessageListenerAdapter102} class for use in testing.
 *
 * @author Rick Evans
 */
public class StubMessageListenerAdapter102 extends MessageListenerAdapter102 {

    private boolean wasCalled;


    public boolean wasCalled() {
        return this.wasCalled;
    }


    public void handleMessage(String message) {
        this.wasCalled = true;
    }


    protected void handleListenerException(Throwable ex) {
        System.out.println(ex);
    }

}
