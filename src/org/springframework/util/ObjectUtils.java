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

package org.springframework.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Miscellaneous object utility methods. Mainly for internal use within the
 * framework; consider Jakarta's Commons Lang for a more comprehensive suite
 * of object utilities.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 19.03.2004
 * @see org.apache.commons.lang.ObjectUtils
 */
public abstract class ObjectUtils {

	/**
	 * Determine if the given objects are equal, returning <code>true</code>
	 * if both are <code>null</code> or <code>false</code> if only one is
	 * <code>null</code>.
	 * <p>Compares arrays with <code>Arrays.equals</code>, performing an equality
	 * check based on the array elements rather than the array reference.
	 * @param o1 first Object to compare
	 * @param o2 second Object to compare
	 * @return whether the given objects are equal
	 * @see java.util.Arrays#equals
	 */
	public static boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1.equals(o2)) {
			return true;
		}

		if (o1 instanceof Object[] && o2 instanceof Object[]) {
			return Arrays.equals((Object[]) o1, (Object[]) o2);
		}
		else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
			return Arrays.equals((boolean[]) o1, (boolean[]) o2);
		}
		else if (o1 instanceof byte[] && o2 instanceof byte[]) {
			return Arrays.equals((byte[]) o1, (byte[]) o2);
		}
		else if (o1 instanceof char[] && o2 instanceof char[]) {
			return Arrays.equals((char[]) o1, (char[]) o2);
		}
		else if (o1 instanceof double[] && o2 instanceof double[]) {
			return Arrays.equals((double[]) o1, (double[]) o2);
		}
		else if (o1 instanceof float[] && o2 instanceof float[]) {
			return Arrays.equals((float[]) o1, (float[]) o2);
		}
		else if (o1 instanceof int[] && o2 instanceof int[]) {
			return Arrays.equals((int[]) o1, (int[]) o2);
		}
		else if (o1 instanceof long[] && o2 instanceof long[]) {
			return Arrays.equals((long[]) o1, (long[]) o2);
		}
		else if (o1 instanceof short[] && o2 instanceof short[]) {
			return Arrays.equals((short[]) o1, (short[]) o2);
		}

		return false;
	}

	/**
	 * Returns the value of <code>obj.toString()</code> is <code>obj</code> is
	 * not <code>null</code> otherwise returns an empty <code>String</code>.
	 */
	public static String nullSafeToString(Object obj) {
		return (obj != null ? obj.toString() : "");
	}

	/**
	 * Return a hex string form of an object's identity hash code.
	 * @param obj the object
	 * @return the object's identity code in hex
	 */
	public static String getIdentityHexString(Object obj) {
		return Integer.toHexString(System.identityHashCode(obj));
	}

	/**
	 * Return whether the given throwable is a checked exception:
	 * that is, neither a RuntimeException nor an Error.
	 * @param ex the throwable to check
	 * @return whether the throwable is a checked exception
	 * @see java.lang.Exception
	 * @see java.lang.RuntimeException
	 * @see java.lang.Error
	 */
	public static boolean isCheckedException(Throwable ex) {
		return !(ex instanceof RuntimeException || ex instanceof Error);
	}

	/**
	 * Check whether the given exception is compatible with the exceptions
	 * declared in a throws clause.
	 * @param ex the exception to checked
	 * @param declaredExceptions the exceptions declared in the throws clause
	 * @return whether the given exception is compatible
	 */
	public static boolean isCompatibleWithThrowsClause(Throwable ex, Class[] declaredExceptions) {
		if (!isCheckedException(ex)) {
			return true;
		}
		if (declaredExceptions != null) {
			for (int i = 0; i < declaredExceptions.length; i++) {
				if (declaredExceptions[i].isAssignableFrom(ex.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return whether the given array is empty: that is, null or of zero length.
	 * @param array the array to check
	 */
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * Append the given Object to the given array, returning a new array
	 * consisting of the input array contents plus the given Object.
	 * @param array the array to append to (can be <code>null</code>)
	 * @param obj the Object to append
	 * @return the new array (of the same component type; never <code>null</code>)
	 */
	public static Object[] addObjectToArray(Object[] array, Object obj) {
		Class compType = Object.class;
		if (array != null) {
			compType = array.getClass().getComponentType();
		}
		else if (obj != null) {
			compType = obj.getClass();
		}
		int newArrLength = (array != null ? array.length + 1 : 1);
		Object[] newArr = (Object[]) Array.newInstance(compType, newArrLength);
		if (array != null) {
			System.arraycopy(array, 0, newArr, 0, array.length);
		}
		newArr[newArr.length - 1] = obj;
		return newArr;
	}

	/**
	 * Convert a primitive array to an object array of primitive wrapper objects.
	 * @param primitiveArray the primitive array
	 * @return the object array
	 * @throws IllegalArgumentException if the parameter is not a primitive array
	 */
	public static Object[] toObjectArray(Object primitiveArray) {
		if (primitiveArray == null) {
			return new Object[0];
		}
		Class clazz = primitiveArray.getClass();
		if (!clazz.isArray() || !clazz.getComponentType().isPrimitive()) {
			throw new IllegalArgumentException("The specified parameter is not a primitive array");
		}
		int length = Array.getLength(primitiveArray);
		if (length == 0) {
			return new Object[0];
		}
		Class wrapperType = Array.get(primitiveArray, 0).getClass();
		Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			newArray[i] = Array.get(primitiveArray, i);
		}
		return newArray;
	}

}
