/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 *          Exp $
 */
public class StringUtilsTestSuite extends TestCase {

    public StringUtilsTestSuite(String name) {
        super(name);
    }

    /** Run for each test */
    protected void setUp() throws Exception {
    }

    public void testCountOccurrencesOf() {
        assertTrue(
            "nullx2 = 0",
            StringUtils.countOccurrencesOf(null, null) == 0);
        assertTrue(
            "null string = 0",
            StringUtils.countOccurrencesOf("s", null) == 0);
        assertTrue(
            "null substring = 0",
            StringUtils.countOccurrencesOf(null, "s") == 0);
        String s = "erowoiueoiur";
        assertTrue(
            "not found = 0",
            StringUtils.countOccurrencesOf(s, "WERWER") == 0);
        assertTrue(
            "not found char = 0",
            StringUtils.countOccurrencesOf(s, "x") == 0);
        assertTrue(
            "not found ws = 0",
            StringUtils.countOccurrencesOf(s, " ") == 0);
        assertTrue(
            "not found empty string = 0",
            StringUtils.countOccurrencesOf(s, "") == 0);
        assertTrue("found char=2", StringUtils.countOccurrencesOf(s, "e") == 2);
        assertTrue(
            "found substring=2",
            StringUtils.countOccurrencesOf(s, "oi") == 2);
        assertTrue(
            "found substring=2",
            StringUtils.countOccurrencesOf(s, "oiu") == 2);
        assertTrue(
            "found substring=3",
            StringUtils.countOccurrencesOf(s, "oiur") == 1);
        assertTrue("test last", StringUtils.countOccurrencesOf(s, "r") == 2);
    }

    public void testCommaDelimitedListToStringArrayNullProducesEmptyArray() {
        String[] sa = StringUtils.commaDelimitedListToStringArray(null);
        assertTrue("String array isn't null with null input", sa != null);
        assertTrue("String array length == 0 with null input", sa.length == 0);
    }

    private void testCommaDelimitedListToStringArrayLegalMatch(String[] components) {
        StringBuffer sbuf = new StringBuffer();
        // Build String array
        for (int i = 0; i < components.length; i++) {
            if (i != 0)
                sbuf.append(",");
            sbuf.append(components[i]);
        }
        //System.out.println("STRING IS " + sbuf);

        String[] sa =
            StringUtils.commaDelimitedListToStringArray(sbuf.toString());
        assertTrue("String array isn't null with legal match", sa != null);
        assertTrue(
            "String array length is correct with legal match: returned "
                + sa.length
                + " when expecting "
                + components.length
                + " with String ["
                + sbuf.toString()
                + "]",
            sa.length == components.length);
        assertTrue("Output equals input", Arrays.equals(sa, components));
    }

    private void testStringArrayReverseTransformationMatches(String[] sa) {
        String[] reverse =
            StringUtils.commaDelimitedListToStringArray(
                StringUtils.arrayToCommaDelimitedString(sa));
        assertEquals(
            "Reverse transformation is equal",
            Arrays.asList(sa),
            Arrays.asList(reverse));
    }

    public void testCommaDelimitedListToStringArrayMatchWords() {
        // Could read these from files
        String[] sa = new String[] { "foo", "bar", "big" };
        testCommaDelimitedListToStringArrayLegalMatch(sa);
        testStringArrayReverseTransformationMatches(sa);

        sa = new String[] { "a", "b", "c" };
        testCommaDelimitedListToStringArrayLegalMatch(sa);
        testStringArrayReverseTransformationMatches(sa);

        // Test same words
        sa = new String[] { "AA", "AA", "AA", "AA", "AA" };
        testCommaDelimitedListToStringArrayLegalMatch(sa);
        testStringArrayReverseTransformationMatches(sa);
    }

    public void testCommaDelimitedListToStringArraySingleString() {
        // Could read these from files
        String s = "woeirqupoiewuropqiewuorpqiwueopriquwopeiurqopwieur";
        String[] sa = StringUtils.commaDelimitedListToStringArray(s);
        assertTrue("Found one String with no delimiters", sa.length == 1);
        assertTrue(
            "Single array entry matches input String with no delimiters",
            sa[0].equals(s));
    }

    public void testCommaDelimitedListToStringArrayWithOtherPunctuation() {
        // Could read these from files
        String[] sa =
            new String[] { "xcvwert4456346&*.", "///", ".!", ".", ";" };
        testCommaDelimitedListToStringArrayLegalMatch(sa);
    }

    /** We expect to see the empty Strings in the output */
    public void testCommaDelimitedListToStringArrayEmptyStrings() {
        // Could read these from files
        String[] ss = StringUtils.commaDelimitedListToStringArray("a,,b");
        assertTrue(
            "a,,b produces array length 3, not " + ss.length,
            ss.length == 3);
        assertTrue(
            "components are correct",
            ss[0].equals("a") && ss[1].equals("") && ss[2].equals("b"));

        String[] sa = new String[] { "", "", "a", "" };
        testCommaDelimitedListToStringArrayLegalMatch(sa);
    }

    public void testReplace() throws Exception {
        String inString = "a6AazAaa77abaa";
        String oldPattern = "aa";
        String newPattern = "foo";

        // Simple replace
        String s = StringUtils.replace(inString, oldPattern, newPattern);
        assertTrue("Replace 1 worked", s.equals("a6AazAfoo77abfoo"));

        // Non match: no change
        s =
            StringUtils.replace(
                inString,
                "qwoeiruqopwieurpoqwieur",
                newPattern);
        assertTrue("Replace non matched is equal", s.equals(inString));

        // Null new pattern: should ignore
        s = StringUtils.replace(inString, oldPattern, null);
        assertTrue("Replace non matched is equal", s.equals(inString));

        // Null old pattern: should ignore
        s = StringUtils.replace(inString, null, newPattern);
        assertTrue("Replace non matched is equal", s.equals(inString));
    }

    public void testDelete() throws Exception {
        String inString = "The quick brown fox jumped over the lazy dog";

        String noThe = StringUtils.delete(inString, "the");
        assertTrue(
            "Result has no the [" + noThe + "]",
            noThe.equals("The quick brown fox jumped over  lazy dog"));

        String nohe = StringUtils.delete(inString, "he");
        assertTrue(
            "Result has no he [" + nohe + "]",
            nohe.equals("T quick brown fox jumped over t lazy dog"));

        String nosp = StringUtils.delete(inString, " ");
        assertTrue(
            "Result has no spaces",
            nosp.equals("Thequickbrownfoxjumpedoverthelazydog"));

        String killEnd = StringUtils.delete(inString, "dog");
        assertTrue(
            "Result has no dog",
            killEnd.equals("The quick brown fox jumped over the lazy "));

        String mismatch = StringUtils.delete(inString, "dxxcxcxog");
        assertTrue("Result is unchanged", mismatch.equals(inString));
    }

    public void testDeleteAny() throws Exception {
        String inString = "Able was I ere I saw Elba";

        String res = StringUtils.deleteAny(inString, "I");
        assertTrue(
            "Result has no Is [" + res + "]",
            res.equals("Able was  ere  saw Elba"));

        res = StringUtils.deleteAny(inString, "AeEba!");
        assertTrue(
            "Result has no Is [" + res + "]",
            res.equals("l ws I r I sw l"));

        String mismatch = StringUtils.deleteAny(inString, "#@$#$^");
        assertTrue("Result is unchanged", mismatch.equals(inString));

        String whitespace =
            "This is\n\n\n    \t   a messagy string with whitespace\n";
        assertTrue("Has CR", whitespace.indexOf("\n") != -1);
        assertTrue("Has tab", whitespace.indexOf("\t") != -1);
        assertTrue("Has  sp", whitespace.indexOf(" ") != -1);
        String cleaned = StringUtils.deleteAny(whitespace, "\n\t ");
        assertTrue("Has no CR", cleaned.indexOf("\n") == -1);
        assertTrue("Has no tab", cleaned.indexOf("\t") == -1);
        assertTrue("Has no sp", cleaned.indexOf(" ") == -1);
        assertTrue("Still has chars", cleaned.length() > 10);
    }

    public void testHasTextBlank() throws Exception {
        String blank = "          ";
        assertEquals(false, StringUtils.hasText(blank));
    }

    public void testHasTextNullEmpty() throws Exception {
        assertEquals(false, StringUtils.hasText(null));
        assertEquals(false, StringUtils.hasText(""));
    }

    public void testHasTextValid() throws Exception {
        assertEquals(true, StringUtils.hasText("t"));
    }

    public void testUnqualify() throws Exception {
        String qualified = "i.am.not.unqualified";
        assertEquals("unqualified", StringUtils.unqualify(qualified));
    }
    
    public void testUncapitalize() throws Exception {
        String capitalized = "I am capitalized";
        assertEquals("i am capitalized", StringUtils.uncapitalize(capitalized));
    }

    public void testCapitalize() throws Exception {
        String capitalized = "i am not capitalized";
        assertEquals("I am not capitalized", StringUtils.capitalize(capitalized));
    }


	public void testPathEquals() {
		assertTrue(
			"Must be true for the same strings",
			StringUtils.pathEquals(
				"/dummy1/dummy2/dummy3", 
				"/dummy1/dummy2/dummy3"));
		assertTrue(
			"Must be true for the same win strings",
			StringUtils.pathEquals(
				"C:\\dummy1\\dummy2\\dummy3", 
				"C:\\dummy1\\dummy2\\dummy3"));
		assertTrue(
			"Must be true for one top path on 1",
			StringUtils.pathEquals(
				"/dummy1/bin/../dummy2/dummy3", 
				"/dummy1/dummy2/dummy3"));
		assertTrue(
			"Must be true for one win top path on 2",
			StringUtils.pathEquals(
				"C:\\dummy1\\dummy2\\dummy3", 
				"C:\\dummy1\\bin\\..\\dummy2\\dummy3"));
		assertTrue(
			"Must be true for two top paths on 1",
			StringUtils.pathEquals(
				"/dummy1/bin/../dummy2/bin/../dummy3", 
				"/dummy1/dummy2/dummy3"));
		assertTrue(
			"Must be true for two win top paths on 2",
			StringUtils.pathEquals(
				"C:\\dummy1\\dummy2\\dummy3", 
				"C:\\dummy1\\bin\\..\\dummy2\\bin\\..\\dummy3"));
		assertTrue(
			"Must be true for double top paths on 1",
			StringUtils.pathEquals(
				"/dummy1/bin/tmp/../../dummy2/dummy3", 
				"/dummy1/dummy2/dummy3"));
		assertTrue(
			"Must be true for double top paths on 2 with similarity",
			StringUtils.pathEquals(
				"/dummy1/dummy2/dummy3",
				"/dummy1/dum/dum/../../dummy2/dummy3"));
		assertTrue(
			"Must be true for current paths",
			StringUtils.pathEquals(
				"./dummy1/dummy2/dummy3",
				"dummy1/dum/./dum/../../dummy2/dummy3"));
		assertFalse(
			"Must be false for relative/absolute paths",
			StringUtils.pathEquals(
				"./dummy1/dummy2/dummy3",
				"/dummy1/dum/./dum/../../dummy2/dummy3"));
		assertFalse(
			"Must be false for different strings",
			StringUtils.pathEquals(
				"/dummy1/dummy2/dummy3", 
				"/dummy1/dummy4/dummy3"));
		assertFalse(
			"Must be false for one false path on 1",
			StringUtils.pathEquals(
				"/dummy1/bin/tmp/../dummy2/dummy3", 
				"/dummy1/dummy2/dummy3"));
		assertFalse(
			"Must be false for one false win top path on 2",
			StringUtils.pathEquals(
				"C:\\dummy1\\dummy2\\dummy3", 
				"C:\\dummy1\\bin\\tmp\\..\\dummy2\\dummy3"));
		assertFalse(
			"Must be false for top path on 1 + difference",
			StringUtils.pathEquals(
				"/dummy1/bin/../dummy2/dummy3", 
				"/dummy1/dummy2/dummy4"));
	}

}
