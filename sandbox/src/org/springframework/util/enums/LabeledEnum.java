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
package org.springframework.util.enums;

import java.util.Comparator;

import org.springframework.util.comparator.ComparableComparator;
import org.springframework.util.comparator.CompoundComparator;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * A interface for objects that are enumerations. Each enum instance has the
 * following characteristics:
 * <p>
 * A type that identifies the enum's class. For example,
 * <code>com.mycompany.util.FileFormat</code>.
 * <p>
 * A code that uniquely identifies the enum within the context of its type. For
 * example, <code>CSV</code>. Different classes of codes are possible
 * (Character, Integer, String.)
 * <p>
 * A descriptive label. For example, <code>the CSV File Format</code>.
 * <p>
 * A uniquely identifying key that identifies the enum in the context of all
 * other enums (of potentially different types.) For example,
 * <code>com.mycompany.util.FileFormat.CSV</code>.
 * @author Keith Donald
 */
public interface LabeledEnum extends Comparable {

	/**
	 * Comparator that sorts enumerations by <code>CODE_ORDER</code>
	 */
	public static final Comparator CODE_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			Object c1 = ((LabeledEnum)o1).getCode();
			Object c2 = ((LabeledEnum)o2).getCode();
			return ComparableComparator.instance().compare(c1, c2);
		}
	};

	/**
	 * Comparator that sorts enumerations by <code>LABEL_ORDER</code>
	 */
	public static final Comparator LABEL_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			LabeledEnum e1 = (LabeledEnum)o1;
			LabeledEnum e2 = (LabeledEnum)o2;
			Comparator c = new NullSafeComparator(String.CASE_INSENSITIVE_ORDER);
			return c.compare(e1.getLabel(), e2.getLabel());
		}
	};

	/**
	 * Comparator that sorts enumerations by <code>LABEL_ORDER</code>, then
	 * natural order.
	 */
	public static final Comparator DEFAULT_ORDER = new CompoundComparator(new Comparator[] { LABEL_ORDER, CODE_ORDER });

	/**
	 * Returns this enumeration's type.
	 * @return The type.
	 */
	public Class getType();

	/**
	 * Returns this enumeration's code. Each code should be unique within
	 * enumeration's of the same type.
	 * @return The code.
	 */
	public Comparable getCode();

	/**
	 * Returns a descriptive, optional label.
	 * @return The label.
	 */
	public String getLabel();
}