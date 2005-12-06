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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class Pluralizer {

	private static final Log logger = LogFactory.getLog(Pluralizer.class);

	private static List pluralizationRules = Collections.synchronizedList(new ArrayList());

	private static Map pluralizationCache = Collections.synchronizedMap(new HashMap());

	static {
		// register default rules

		// y -> ies
		registerPluralizationRule(new RegexPluralizationRule("([^aeiouy])y$", "ies", 1));

		// sxz -> [sxz]es
		registerPluralizationRule(new RegexPluralizationRule("([sxz])$", "es", 1));

		// hard h -> hes
		registerPluralizationRule(new RegexPluralizationRule("([^aeioudgkprt]h$)", "es", 1));
	}

	public static String pluralize(String term) {

		String pluralForm = (String) pluralizationCache.get(term);

		if (pluralForm == null) {
			PluralizationRule rule = lookupPluralizationRule(term);

			if (rule != null) {
				pluralForm = rule.apply(term);
			}
			else {
				pluralForm = applyDefaultRule(term);

				if (logger.isDebugEnabled()) {
					logger.debug("Located pluralization [" + pluralForm + "] for term [" + term + "] using default rules.");
				}
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Located pluralization [" + pluralForm + "] for term [" + term + "] in the cache.");
			}
		}

		return pluralForm;
	}

	public static void registerPluralizationRule(PluralizationRule rule) {
		pluralizationRules.add(rule);
	}

	/**
	 * Attempts to locate and return a {@link PluralizationRule} for the specified term using the
	 * set of configured {@link PluralizationRule PluralizationRules}. Returns <code>null</code>
	 * if no rule can be found.
	 */
	private static PluralizationRule lookupPluralizationRule(String term) {
		for (int i = 0; i < pluralizationRules.size(); i++) {
			PluralizationRule rule = (PluralizationRule) pluralizationRules.get(i);
			if (rule.appliesTo(term)) {
				return rule;
			}
		}

		return null;
	}

	/**
	 * Applies default English pluralization rules adding &quot;s&quot; to the end of the term.
	 */
	private static String applyDefaultRule(String term) {
		return term + "s";
	}

	public static interface PluralizationRule {

		boolean appliesTo(String term);

		String apply(String term);
	}

	private static class RegexPluralizationRule implements PluralizationRule {

		private Pattern pattern;

		private String replacement;

		private int includeGroup = -1;

		public RegexPluralizationRule(String pattern, String replacement) {
			this(pattern, replacement, -1);
		}

		public RegexPluralizationRule(String pattern, String replacement, int includeGroup) {
			this.pattern = Pattern.compile(pattern);
			this.replacement = replacement;
			this.includeGroup = includeGroup;
		}

		public boolean appliesTo(String term) {
			return pattern.matcher(term).find();
		}

		public String apply(String term) {
			Matcher m = pattern.matcher(term);
			if (m.find()) {
				String replace = (this.includeGroup > -1) ? m.group(this.includeGroup) : "";
				replace += this.replacement;
				return m.replaceFirst(replace);
			}
			else {
				return term;
			}
		}
	}

	public static void main(String[] args) {
		Pattern p = Pattern.compile("([^aeiouy]|qu)y$");
		Matcher m = p.matcher("ability");
		if (m.find()) {
			System.out.println(m.replaceFirst("\\1ies"));
		}
	}
}
