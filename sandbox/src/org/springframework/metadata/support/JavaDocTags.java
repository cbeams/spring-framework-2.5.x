/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class that contains all the legal javadoc tags.
 *
 * @author Mark Pollack
 * @since Oct 14, 2003
 */
public class JavaDocTags {
	/**
	 * The set of all javadoc tags that can appear in the documentation
	 * for a class/interface
	 */
	private static Set _classTags = new HashSet();

	/**
	 * The set of all javadoc tags that can appear in the documentation
	 * for a method.
	 */
	private static Set _methodTags = new HashSet();

	/**
	 * The set of all javadoc tags that can appear in the documentation
	 * for a field.
	 */
	private static Set _fieldTags = new HashSet();

	static {
		_classTags = new HashSet();
		String cTags[] =
			{
				"see",
				"since",
				"deprecated",
				"serial",
				"author",
				"version",
				"{@link}",
				"{@linkplain}",
				"{@docRoot}" };

		_classTags.addAll(Arrays.asList(cTags));

		_methodTags = new HashSet();
		String mTags[] =
			{
				"see",
				"since",
				"deprecated",
				"param",
				"return",
				"throws",
				"exception",
				"serialData",
				"{@link}",
				"{@linkplain}",
				"{@inheritdoc}",
				"{@inheritDoc}",
				"{@docRoot}" };

		_methodTags.addAll(Arrays.asList(mTags));

		_fieldTags = new HashSet();
		String fTags[] =
			{
				"see",
				"since",
				"deprecated",
				"serial",
				"serialField",
				"{@link}",
				"{@linkplain}",
				"{@docRoot}",
				"{@value}" };

		_fieldTags.addAll(Arrays.asList(fTags));
	}

	/**
	 * Return the set of all javadoc tags used for documentation
	 * of classes/interfaces
	 * @return Set The set of all javadoc tags used for documentation.
	 * of classes/interfaces
	 */
	public static Set getJavadocClassTags() {
		return _classTags;
	}

	/**
	 * Return the set of all javadoc tags used for documentation
	 * of methods.
	 * @return Set The set of all javadoc tags used for documentation
	 * of methods
	 */
	public static Set getJavadocMethodTags() {
		return _methodTags;
	}

	/**
	 * Return the set of all javadoc tags used for documentation
	 * of fields
	 * @return Set The set of all javadoc tags used for documentation
	 * of fields
	 */
	public static Set getJavadocFieldTags() {
		return _fieldTags;
	}
}
