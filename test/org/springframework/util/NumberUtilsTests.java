/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.util;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public class NumberUtilsTests extends TestCase {

	public void testParseNumber() {
		String aByte = "" + Byte.MAX_VALUE;
		String aShort = "" + Short.MAX_VALUE;
		String anInteger = "" + Integer.MAX_VALUE;
		String aLong = "" + Long.MAX_VALUE;
		String aFloat = "" + Float.MAX_VALUE;
		String aDouble = "" + Double.MAX_VALUE;

		assertEquals("Byte did not parse", new Byte(Byte.MAX_VALUE), NumberUtils.parseNumber(aByte, Byte.class));
		assertEquals("Short did not parse", new Short(Short.MAX_VALUE), NumberUtils.parseNumber(aShort, Short.class));
		assertEquals("Integer did not parse", new Integer(Integer.MAX_VALUE), NumberUtils.parseNumber(anInteger, Integer.class));
		assertEquals("Long did not parse", new Long(Long.MAX_VALUE), NumberUtils.parseNumber(aLong, Long.class));
		assertEquals("Float did not parse", new Float(Float.MAX_VALUE), NumberUtils.parseNumber(aFloat, Float.class));
		assertEquals("Double did not parse", new Double(Double.MAX_VALUE), NumberUtils.parseNumber(aDouble, Double.class));
	}

	public void testParseWithTrim() {
		String aByte = " " + Byte.MAX_VALUE + " ";
		String aShort = " " + Short.MAX_VALUE + " ";
		String anInteger = " " + Integer.MAX_VALUE + " ";
		String aLong = " " + Long.MAX_VALUE + " ";
		String aFloat = " " + Float.MAX_VALUE + " ";
		String aDouble = " " + Double.MAX_VALUE + " ";

		assertEquals("Byte did not parse", new Byte(Byte.MAX_VALUE), NumberUtils.parseNumber(aByte, Byte.class));
		assertEquals("Short did not parse", new Short(Short.MAX_VALUE), NumberUtils.parseNumber(aShort, Short.class));
		assertEquals("Integer did not parse", new Integer(Integer.MAX_VALUE), NumberUtils.parseNumber(anInteger, Integer.class));
		assertEquals("Long did not parse", new Long(Long.MAX_VALUE), NumberUtils.parseNumber(aLong, Long.class));
		assertEquals("Float did not parse", new Float(Float.MAX_VALUE), NumberUtils.parseNumber(aFloat, Float.class));
		assertEquals("Double did not parse", new Double(Double.MAX_VALUE), NumberUtils.parseNumber(aDouble, Double.class));
	}

	public void testParseAsHex() {
		String aByte = "0x" + Integer.toHexString(new Byte(Byte.MAX_VALUE).intValue());
		String aShort = "0x" + Integer.toHexString(new Short(Short.MAX_VALUE).intValue());
		String anInteger = "0x" + Integer.toHexString(Integer.MAX_VALUE);
		String aLong = "0x" + Long.toHexString(Long.MAX_VALUE);

		assertEquals("Byte did not parse", new Byte(Byte.MAX_VALUE), NumberUtils.parseNumber(aByte, Byte.class));
		assertEquals("Short did not parse", new Short(Short.MAX_VALUE), NumberUtils.parseNumber(aShort, Short.class));
		assertEquals("Integer did not parse", new Integer(Integer.MAX_VALUE), NumberUtils.parseNumber(anInteger, Integer.class));
		assertEquals("Long did not parse", new Long(Long.MAX_VALUE), NumberUtils.parseNumber(aLong, Long.class));
	}

	public void testIsHexString() {
		assertTrue("Should be a hex string", NumberUtils.isHexString("0XFF"));
		assertTrue("Should be a hex string", NumberUtils.isHexString("0xFF"));
		assertFalse("Should not be a hex string", NumberUtils.isHexString("0FF"));

	}

}
