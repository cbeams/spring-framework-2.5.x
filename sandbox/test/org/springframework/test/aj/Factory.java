package org.springframework.test.aj;

/**
 * This class is only for testing purposes. Obviously the <code>getInstance</code>
 * method is not thread-safe.
 */
public class Factory {
    private static Factory INSTANCE;

    private Factory() {

    }

    public static Factory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Factory();
        }
        return INSTANCE;
    }
}
