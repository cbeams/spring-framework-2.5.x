/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.enums;

import java.util.Comparator;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.comparator.ComparableComparator;
import org.springframework.util.comparator.CompoundComparator;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * A interface for objects that are enumerations. Each enum instance has the
 * following characteristics:
 *
 * <p>
 * A type that identifies the enum's class. For example, "fileFormat".
 * <p>
 * A code that uniquely identifies the enum within the context of its type. For
 * example, "CSV". Different classes of codes are possible (Character, Integer,
 * String.)
 * <p>
 * A descriptive label. For example, "the CSV File Format".
 * <p>
 * A uniquely identifying key that identifies the enum in the context of all
 * other enums (of potentially different types.) For example, "fileFormat.CSV".
 *
 * @author Keith Donald
 */
public interface CodedEnum extends MessageSourceResolvable, Comparable {

	/**
	 * Comparator that sorts enumerations by <code>CODE_ORDER</code>
	 */
	public static final Comparator CODE_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Object c1 = ((CodedEnum) o1).getCode();
			Object c2 = ((CodedEnum) o2).getCode();
			return ComparableComparator.instance().compare(c1, c2);
		}
	};

	/**
	 * Comparator that sorts enumerations by <code>LABEL_ORDER</code>
	 */
	public static final Comparator LABEL_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			CodedEnum e1 = (CodedEnum) o1;
			CodedEnum e2 = (CodedEnum) o2;
			Comparator c = new NullSafeComparator(String.CASE_INSENSITIVE_ORDER);
			return c.compare(e1.getLabel(), e2.getLabel());
		}
	};

	/**
	 * Comparator that sorts enumerations by <code>LABEL_ORDER</code>, then
	 * natural order.
	 */
	public static final Comparator DEFAULT_ORDER = new CompoundComparator(new Comparator[]{LABEL_ORDER, CODE_ORDER});

	/**
	 * Returns this enumeration's type. Each type should be unique.
	 *
	 * @return The type.
	 */
	public String getType();

	/**
	 * Returns this enumeration's code. Each code should be unique within
	 * enumeration's of the same type.
	 *
	 * @return The code.
	 */
	public Comparable getCode();

	/**
	 * Returns a descriptive, optional label.
	 *
	 * @return The label.
	 */
	public String getLabel();

	/**
	 * Returns a uniquely indentifying key string. A key generally consists of
	 * the <type>.
	 * <code> composite and should globally uniquely identify this enumeration.
	 *
	 * @return The unique key.
	 */
	public String getKey();

}
