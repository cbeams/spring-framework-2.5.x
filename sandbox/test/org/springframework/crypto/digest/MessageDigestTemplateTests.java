package org.springframework.crypto.digest;

import junit.framework.TestCase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Rob Harrop
 */
public class MessageDigestTemplateTests extends TestCase {

    private static final String TEST_DATA = "The quick brown fox jumped over the lazy dog";

    public void testDigestWithClone() throws Exception {
        MessageDigest md = getDigest();

        byte[] input = TEST_DATA.getBytes();

        md.update(input);

        byte[] desiredOutput = md.digest();

        MessageDigestTemplate template = new MessageDigestTemplate();
        template.setMessageDigest(getDigest());
        template.afterPropertiesSet();

        byte[] actualOutput = template.digest(TEST_DATA.getBytes());

        assertTrue("Output is incorrect", Arrays.equals(desiredOutput, actualOutput));
    }

    public void testNonCloneableDigest() throws Exception {
        NonCloneableDigest md = new NonCloneableDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();
        template.setMessageDigest(md);
        template.afterPropertiesSet();

        byte[] input = TEST_DATA.getBytes();

        byte[] output = template.digest(input);

        assertTrue("Reset method wasn't invoked.", md.isResetCalled());
    }

    public void testCallbackWithCloneable() throws Exception {
        final MessageDigest md = getDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();

        template.setMessageDigest(md);
        template.afterPropertiesSet();

        Boolean called = (Boolean) template.execute(new MessageDigestCallback() {
            public Object doWithMessageDigest(MessageDigest messageDigest) {
                assertTrue("Should have different instances of MessageDigest", md != messageDigest);

                return Boolean.TRUE;
            }
        });

        assertTrue("execute() was not called.", called.booleanValue());
    }

    public void testCallbackWithNonCloneable() throws Exception {
        final NonCloneableDigest md = new NonCloneableDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();

        template.setMessageDigest(md);
        template.afterPropertiesSet();

        Boolean called = (Boolean) template.execute(new MessageDigestCallback() {
            public Object doWithMessageDigest(MessageDigest messageDigest) {
                assertTrue("Should have same instances of MessageDigest", md == messageDigest);

                return Boolean.TRUE;
            }
        });

        assertTrue("execute() was not called.", called.booleanValue());
    }

    public void testDigestString() throws Exception {
        MessageDigest md = getDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();

        template.setMessageDigest(md);
        template.afterPropertiesSet();

        md.update(TEST_DATA.getBytes());
        byte[] desiredOutput = md.digest();

        byte[] actualOutput = template.digest(TEST_DATA);

        assertTrue("Digest output incorrect.", Arrays.equals(desiredOutput, actualOutput));

    }

    public void testDigestStringWithSpecificEncoding() throws Exception {
        String encoding = "ASCII";

        MessageDigest md = getDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();

        template.setMessageDigest(md);
        template.afterPropertiesSet();

        md.update(TEST_DATA.getBytes(encoding));
        byte[] desiredOutput = md.digest();

        byte[] actualOutput = template.digest(TEST_DATA, encoding);

        assertTrue("Digest output incorrect.", Arrays.equals(desiredOutput, actualOutput));
    }

    public void testInvalidEncoding() throws Exception {
        String encoding = "FOO";

        MessageDigest md = getDigest();
        MessageDigestTemplate template = new MessageDigestTemplate();

        template.setMessageDigest(md);
        template.afterPropertiesSet();

        try {
            byte[] bytes = template.digest(TEST_DATA, encoding);
            fail("Invalid encoding should throw IllegalArgumentException");
        }   catch(IllegalArgumentException ex) {
            // success
        }
    }

    private MessageDigest getDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA");
    }

    private class NonCloneableDigest extends MessageDigest {

        public NonCloneableDigest() {
            super("SHA");
        }

        protected NonCloneableDigest(String s) {
            super(s);
        }

        private boolean resetCalled = false;

        protected void engineUpdate(byte b) {

        }

        protected void engineUpdate(byte[] bytes, int i, int i1) {

        }

        protected byte[] engineDigest() {
            return new byte[0];
        }

        protected void engineReset() {
            resetCalled = true;
        }

        public boolean isResetCalled() {
            return resetCalled;
        }

        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }
}
