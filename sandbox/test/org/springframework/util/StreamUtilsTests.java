/*
 * Created on Mar 3, 2004
 */
package org.springframework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests the various functionality of the StreamUtils class.
 * 
 * @author R.J. Lorimer
 */
public class StreamUtilsTests extends TestCase {

    public void testInputStreamIntoByteArray() {
        ensureStreamToByteArray(new byte[] { 0, 1, 2, 3 });
    }

    public void testEmptyByteArrayStreamIntoByteArray() {
        ensureStreamToByteArray(new byte[] {});
    }

    public void testStreamToStream() {
        ensureStreamToStream(new byte[] { 0, 1, 2, 3 });
    }

    public void testEmptyStreamToStream() {
        ensureStreamToStream(new byte[] {});
    }

    public void testNullReadStreamIntoStreamArg1() {
        try {
            StreamUtils.readStreamIntoStream(null, new ByteArrayOutputStream());
            fail("Null input stream accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior
        } catch (Exception e) {
            fail("null input stream was not handled properly");
        }
    }

    public void testNullReadStreamIntoStreamArg2() {
        try {
            StreamUtils.readStreamIntoStream(new ByteArrayInputStream(
                    new byte[] {}), null);
            fail("Null output stream accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior
        } catch (Exception e) {
            fail("null output stream was not handled properly");
        }
    }

    public void testNullReadStreamIntoByteArrayArg() {
        try {
            StreamUtils.readStreamIntoByteArray(null);
            fail("null input stream accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior
        } catch (Exception e) {
            fail("null input stream was not handled properly");
        }
    }

    private void ensureStreamToByteArray(byte[] input) {
        try {
            byte[] output = StreamUtils
                    .readStreamIntoByteArray(new ByteArrayInputStream(input));

            ensureByteArrayIntegrity(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureStreamToStream(byte[] input) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamUtils.readStreamIntoStream(inputStream, outputStream);
            byte[] output = outputStream.toByteArray();
            ensureByteArrayIntegrity(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureByteArrayIntegrity(byte[] input, byte[] output) {
        assertEquals("the byte[] lengths differ!", input.length, output.length);
        for (int i = 0; i < input.length; i++) {
            assertEquals("element at location: " + i + " is different!",
                    input[i], output[i]);
        }
    }

}
