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

import junit.framework.TestCase;

import org.springframework.util.enums.LabeledEnum;
import org.springframework.util.enums.support.ShortCodedLabeledEnum;
import org.springframework.util.enums.support.StaticLabeledEnumResolver;

/**
 * @author Rod Johnson
 */
public class EnumTests extends TestCase {

	public void testForCodeFound() {
		Dog golden = (Dog) StaticLabeledEnumResolver.instance().getEnum(Dog.class, new Short((short) 11));
		Dog borderCollie = (Dog) StaticLabeledEnumResolver.instance().getEnum(Dog.class, new Short((short) 13));
		assertSame(golden, Dog.GOLDEN_RETRIEVER);
		assertSame(borderCollie, Dog.BORDER_COLLIE);
	}

	public void testDoesNotMatchWrongClass() {
		LabeledEnum none = StaticLabeledEnumResolver.instance().getEnum(Dog.class, new Short((short) 1));
		assertEquals(null, none);
	}

	public void testEquals() {
		assertEquals("Code equality means equals", Dog.GOLDEN_RETRIEVER, new Dog(11, "Golden Retriever"));
		assertFalse("Code inequality means notEquals", Dog.GOLDEN_RETRIEVER.equals(new Dog(12, "Golden Retriever")));
	}


	public static class Other extends ShortCodedLabeledEnum {

		public static Other THING1 = new Other(1, "Thing1");

		public static Other THING2 = new Other(2, "Thing2");

		public Other(int code, String name) {
			super(code, name);
		}
	}


	public static class Dog extends ShortCodedLabeledEnum {

		public static final Dog GOLDEN_RETRIEVER = new Dog(11, null) {
			// this shouldn't be neccessary
			public String getType() {
				return Dog.class.getName();
			}

			public String getLabel() {
				return "Golden Retriever";
			}
		};

		public static final Dog BORDER_COLLIE = new Dog(13, "Border Collie");

		public static final Dog WHIPPET = new Dog(14, "Whippet");

		// Ignore this
		public static final Other THING1 = Other.THING1;

		private Dog(int code, String name) {
			super(code, name);
		}
	}

}
