/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Miscellaneous string utility methods. Mainly for internal use within
 * the framework; consider Jakarta's Commons Lang for a more comprehensive
 * suite of string utilities.
 *
 * <p>This class delivers some simple functionality that should really be
 * provided by the core Java String and StringBuffer classes, such as the
 * ability to replace all occurrences of a given substring in a target
 * string. It also provides easy-to-use methods to convert between
 * delimited strings, such as CSV strings, and collections and arrays.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @version $Id: StringUtils.java,v 1.7 2004-03-01 09:19:48 jhoeller Exp $
 * @see org.apache.commons.lang.StringUtils
 */
public abstract class StringUtils {

	/**
	 * Determine if the given Strings are equal, returning true if both
	 * are null respectively false if only one is null.
	 * @param s1 first String to compare
	 * @param s2 second String to compare
	 * @return whether the given Strings are equal
	 */
	public static boolean nullSafeEquals(String s1, String s2) {
		return (s1 == s2 || (s1 != null && s1.equals(s2)));
	}

	/**
	 * Count the occurrences of the substring in string s
	 * @param s string to search in. Returns 0 if this is null
	 * @param sub string to search for. Return 0 if this is null.
	 */
	public static int countOccurrencesOf(String s, String sub) {
		if (s == null || sub == null || "".equals(sub))
			return 0;
		int count = 0, pos = 0, idx = 0;
		while ((idx = s.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * Replaces all occurences of a substring within a string with another string.
	 * @param inString String to examine
	 * @param oldPattern String to replace
	 * @param newPattern String to insert
	 * @return a String with the replacements
	 */
	public static String replace(String inString, String oldPattern, String newPattern) {
		// Pick up error conditions
		if (inString == null)
			return null;
		if (oldPattern == null || newPattern == null)
			return inString;

		StringBuffer sbuf = new StringBuffer();      // Output StringBuffer we'll build up
		int pos = 0;                        // Our position in the old string
		int index = inString.indexOf(oldPattern); // The index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sbuf.append(inString.substring(pos, index));
			sbuf.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sbuf.append(inString.substring(pos));     // Remember to append any characters to the right of a match
		return sbuf.toString();
	}

	/**
	 * Delete all occurrences of the given substring.
	 * @param pattern pattern to delete all occurrences of
	 */
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}

	/**
	 * Delete any character in a given string.
	 * @param chars characters to delete e.g. az\n will delete as, zs and new lines
	 */
	public static String deleteAny(String inString, String chars) {
		if (inString == null || chars == null)
			return inString;
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (chars.indexOf(c) == -1) {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * @param s the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String
	 * @param trimTokens trim the tokens via String.trim
	 * @param ignoreEmptyTokens omit empty tokens from the result array
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see java.lang.String#trim
	 */
	public static String[] tokenizeToStringArray(String s, String delimiters,
	                                             boolean trimTokens, boolean ignoreEmptyTokens) {
		StringTokenizer st = new StringTokenizer(s, delimiters);
		List tokens = new ArrayList();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!(ignoreEmptyTokens && token.length() == 0)) {
				tokens.add(token);
			}
		}
		return (String[]) tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array
	 * @param s String
	 * @param delimiter delimiter. This will not be returned.
	 * @return an array of the tokens in the list
	 */
	public static String[] delimitedListToStringArray(String s, String delimiter) {
		if (s == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[]{s};
		}

		List l = new LinkedList();
		int pos = 0;
		int delPos = 0;
		while ((delPos = s.indexOf(delimiter, pos)) != -1) {
			l.add(s.substring(pos, delPos));
			pos = delPos + delimiter.length();
		}
		if (pos <= s.length()) {
			// add rest of String
			l.add(s.substring(pos));
		}

		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * Convert a CSV list into an array of Strings.
	 * @param s CSV list
	 * @return an array of Strings. Returns the empty array if
	 * s is null.
	 */
	public static String[] commaDelimitedListToStringArray(String s) {
		return delimitedListToStringArray(s, ",");
	}

	/**
	 * Convenience method to convert a CSV string list to a set.
	 * Note that this will suppress duplicates.
	 * @param s CSV String
	 * @return a Set of String entries in the list
	 */
	public static Set commaDelimitedListToSet(String s) {
		Set set = new TreeSet();
		String[] tokens = commaDelimitedListToStringArray(s);
		for (int i = 0; i < tokens.length; i++)
			set.add(tokens[i]);
		return set;
	}

	/**
	 * Convenience method to return a String array as a delimited (e.g. CSV)
	 * String. Useful for toString() implementations
	 * @param arr array to display. Elements may be of any type (toString() will be
	 * called on each element).
	 * @param delim delimiter to use (probably a ,)
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (arr == null)
			return "null";
		else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < arr.length; i++) {
				if (i > 0)
					sb.append(delim);
				sb.append(arr[i]);
			}
			return sb.toString();
		}
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. Useful for toString() implementations.
	 * @param c Collection to display
	 * @param delim delimiter to use (probably a ",")
	 */
	public static String collectionToDelimitedString(Collection c, String delim) {
		if (c == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		Iterator itr = c.iterator();
		int i = 0;
		while (itr.hasNext()) {
			if (i++ > 0)
				sb.append(delim);
			sb.append(itr.next());
		}
		return sb.toString();
	}

	/**
	 * Convenience method to return a String array as a CSV String.
	 * Useful for toString() implementations.
	 * @param arr array to display. Elements may be of any type (toString() will be
	 * called on each element).
	 */
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}

	/**
	 * Convenience method to return a Collection as a CSV String.
	 * Useful for toString() implementations.
	 * @param c Collection to display
	 */
	public static String collectionToCommaDelimitedString(Collection c) {
		return collectionToDelimitedString(c, ",");
	}

	/**
	 * Append the given String to the given String array, returning a new
	 * array consisting of the input array contents plus the given String.
	 * @param arr the array to append to
	 * @param s the String to append
	 * @return the new array
	 */
	public static String[] addStringToArray(String[] arr, String s) {
		String[] newArr = new String[arr.length + 1];
		System.arraycopy(arr, 0, newArr, 0, arr.length);
		newArr[arr.length] = s;
		return newArr;
	}

}
