package org.springframework.util.comparator;

import java.util.Comparator;

import junit.framework.TestCase;

public class ComparatorTests extends TestCase {

	public void testComparableComparator() {
		Comparator c = ComparableComparator.instance();
		String s1 = "abc";
		String s2 = "cde";
		assertTrue(c.compare(s1, s2) < 0);
	}

	public void testComparableComparatorIllegalArgs() {
		Comparator c = ComparableComparator.instance();
		Object o1 = new Object();
		Object o2 = new Object();
		try {
			c.compare(o1, o2);
		}
		catch (IllegalArgumentException e) {
			return;
		}
		fail("Comparator should have thrown a cce");
	}

	public void testBooleanComparatorTrueLow() {
		Comparator c = BooleanComparator.instance(true);
		assertTrue(c.compare(new Boolean(true), new Boolean(false)) < 0);
	}

	public void testBooleanComparatorTrueHigh() {
		Comparator c = BooleanComparator.instance(false);
		assertTrue(c.compare(new Boolean(true), new Boolean(false)) > 0);
		assertTrue(c.compare(Boolean.TRUE, Boolean.TRUE) == 0);
	}

	public void testPropertyComparatorNoProperties() {
		Dog dog = new Dog();
		dog.setNickName("mace");

		Dog dog2 = new Dog();
		dog2.setNickName("biscy");

		PropertyComparator c = new PropertyComparator();
		assertTrue(c.compare(dog, dog2) > 0);
		assertTrue(c.compare(dog, dog) == 0);
		assertTrue(c.compare(dog2, dog) < 0);
	}

	public void testPropertyComparator() {
		Dog dog = new Dog();
		dog.setNickName("mace");

		Dog dog2 = new Dog();
		dog2.setNickName("biscy");

		PropertyComparator c = new PropertyComparator("nickName");
		assertTrue(c.compare(dog, dog2) > 0);
		assertTrue(c.compare(dog, dog) == 0);
		assertTrue(c.compare(dog2, dog) < 0);
	}

	public void testPropertyComparatorNulls() {
		Dog dog = new Dog();
		Dog dog2 = new Dog();
		PropertyComparator c = new PropertyComparator("nickName");
		assertTrue(c.compare(dog, dog2) == 0);
	}

	public void testNullSafeComparatorNullsLow() {
		Comparator c = NullSafeComparator.instance();
		assertTrue(c.compare(null, "boo") < 0);
	}

	public void testNullSafeComparatorNullsHigh() {
		Comparator c = NullSafeComparator.instance(false);
		assertTrue(c.compare(null, "boo") > 0);
		assertTrue(c.compare(null, null) == 0);
	}

	public void testCompoundComparatorEmpty() {
		Comparator c = new CompoundComparator();
		try {
			c.compare("foo", "bar");
		}
		catch (IllegalStateException e) {
			return;
		}
		fail("illegal state should've been thrown on empty list");
	}

	public void testCompoundComparator() {
		CompoundComparator c = new CompoundComparator();
		c.addComparator(new PropertyComparator("lastName"));
		Dog dog1 = new Dog();
		dog1.setFirstName("macy");
		dog1.setLastName("grayspots");

		Dog dog2 = new Dog();
		dog2.setFirstName("biscuit");
		dog2.setLastName("grayspots");

		assertTrue(c.compare(dog1, dog2) == 0);
		c.addComparator(new PropertyComparator("firstName"));
		assertTrue(c.compare(dog1, dog2) > 0);

		dog2.setLastName("konikk dog");
		assertTrue(c.compare(dog2, dog1) > 0);
	}

	public void testCompoundComparatorFlip() {
		CompoundComparator c = new CompoundComparator();
		c.addComparator(new PropertyComparator("lastName"));
		c.addComparator(new PropertyComparator("firstName"));
		Dog dog1 = new Dog();
		dog1.setFirstName("macy");
		dog1.setLastName("grayspots");

		Dog dog2 = new Dog();
		dog2.setFirstName("biscuit");
		dog2.setLastName("grayspots");

		assertTrue(c.compare(dog1, dog2) > 0);
		c.flipOrder();
		assertTrue(c.compare(dog1, dog2) < 0);
	}

	public void testStaticFactoryMethods() {
		CompoundComparator c = new CompoundComparator(SortDefinition
				.createSortDefinitionList(new Comparator[]{
					new PropertyComparator("lastName"),
					new PropertyComparator("firstName")}));
		Dog dog1 = new Dog();
		dog1.setFirstName("macy");
		dog1.setLastName("grayspots");

		Dog dog2 = new Dog();
		dog2.setFirstName("biscuit");
		dog2.setLastName("grayspots");

		assertTrue(c.compare(dog1, dog2) > 0);
	}


	private static class Dog implements Comparable {

		private String nickName;

		private String firstName;

		private String lastName;

		public int compareTo(Object o) {
			return nickName.compareTo(((Dog) o).nickName);
		}

		public String getNickName() {
			return nickName;
		}

		public void setNickName(String nickName) {
			this.nickName = nickName;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}

}
