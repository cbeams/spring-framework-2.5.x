/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.util;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * Utilities for testing serializability of objects.
 * Exposes static methods for use in other test cases.
 * Extends TestCase only to test itself.
 * @author Rod Johnson
 * @version $Id: SerializationTestUtils.java,v 1.1 2004-06-11 08:05:10 johnsonr Exp $
 */
public class SerializationTestUtils extends TestCase {
	
	public static void testSerialization(Object o) throws IOException {
		OutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
	}
	
	public static Object serializeAndDeserialize(Object o) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.flush();
		baos.flush();
		byte[] bytes = baos.toByteArray();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(is);
		Object o2 = ois.readObject();
		
		return o2;
	}
	
	public SerializationTestUtils(String s) {
		super(s);
	}
	
	public void testWithNonSerializableObject() throws IOException {
		TestBean o = new TestBean();
		assertFalse(o instanceof Serializable);
		try {
			testSerialization(o);
			fail();
		}
		catch (NotSerializableException ex) {
			// Ok
		}
	}
	
	public void testWithSerializableObject() throws Exception {
		int x = 5;
		int y = 10;
		Point p = new Point(x, y);
		assertTrue(p instanceof Serializable);
	
		testSerialization(p);
		
		Point p2 = (Point) serializeAndDeserialize(p);
		assertNotSame(p, p2);
		assertEquals(x, (int) p2.getX());
		assertEquals(y, (int) p2.getY());
	}

}
