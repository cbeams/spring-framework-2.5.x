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

package org.springframework.web.servlet.view.xslt;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.NestedRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Xalan extension functions to provide date and currency formatting
 * beyond the capabilities of XSLT 1.0 or 1.1.
 *
 * <p>Note that all extension functions are static.
 * These extension functions must be declared to use this class.
 *
 * <p>Based on an example by Taylor Cowan.
 * 
 * @author Rod Johnson
 * @see AbstractXsltView
 */
public class FormatHelper {

	/**
	 * Creates a formatted-date node with the given ISO language and country strings.
	 * If either the language or country parameters are null, the system default
	 * <code>Locale</code> will be used.
	 * 
     * @param time the time in ms since the epoch that should be used to obtain
     * the formatted date
     * @param language the two character language code required to construct a 
     * <code>java.util.Locale</code>
     * @param country the two character country code required to construct a 
     * <code>java.util.Locale</code>
     * @return a W3C Node containing child Elements with the formatted date-time fragments
     */
	public static Node dateTimeElement(long time, String language, String country) {
		Locale locale = getLocale(language, country);
		return dateTimeElement(time, locale);
	}

	/**
	 * Creates a formatted-date node with system default <code>Locale</code>
	 * 
     * @param time the time in ms since the epoch that should be used to obtain
     * the formatted date
     * @return a W3C Node containing child Elements with the formatted date-time fragments
     */
	public static Node dateTimeElement(long time) {
		return dateTimeElement(time, Locale.getDefault());
	}

	/**
	 * Creates a formatted-date node with the given <code>Locale</code>
	 * 
     * @param time the time in ms since the epoch that should be used to obtain
     * the formatted date
     * @param locale the <code>java.util.Locale</code> to determine the date formatting
     * @return a W3C Node containing child Elements with the formatted date-time fragments
     */
	public static Node dateTimeElement(long time, Locale locale) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element dateNode = doc.createElement("formatted-date");

			// Works in most locales
			SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);

			Date d = new Date(time);
			df.applyPattern("MMMM");
			addChild(dateNode, "month", df.format(d));
			df.applyPattern("EEEE");
			addChild(dateNode, "day-of-week", df.format(d));
			df.applyPattern("yyyy");
			addChild(dateNode, "year", df.format(d));
			df.applyPattern("dd");
			addChild(dateNode, "day-of-month", df.format(d));
			df.applyPattern("h");
			addChild(dateNode, "hours", df.format(d));
			df.applyPattern("mm");
			addChild(dateNode, "minutes", df.format(d));
			df.applyPattern("a");
			addChild(dateNode, "am-pm", df.format(d));
			return dateNode;
		}
		catch (Exception ex) {
			throw  new XsltFormattingException("Failed to create XML date element", ex);
		}
	}

	/**
	 * Format a currency amount in a given locale.
	 * 
     * @param amount the currency value to format
     * @param locale the <code>java.util.Locale</code> to use to format the amount
     * @return a formatted <code>String</code> representing the amount
     */
	public static String currency(double amount, Locale locale) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
		return nf.format(amount);
	}

	/**
	 * Format a currency amount for a given language and country.
	 * 
     * @param amount the currency value to format
     * @param language the two character language code required to construct a 
     * <code>java.util.Locale</code>
     * @param country the two character country code required to construct a 
     * <code>java.util.Locale</code>
     * @return a formatted <code>String</code> representing the amount
     */
	public static String currency(double amount, String language, String country) {
		Locale locale = getLocale(language, country);
		return currency(amount, locale);
	}

	/**
	 * Utility method for adding text nodes.
	 */
	private static void addChild(Node parent, String name, String text) {
		Element child = parent.getOwnerDocument().createElement(name);
		child.appendChild(parent.getOwnerDocument().createTextNode(text));
		parent.appendChild(child);
	}
	
	/**
	 * Utility method to guarantee a Locale.
	 */
	private static Locale getLocale(String language, String country) {
		Locale locale = null;
		if (language == null || country == null) {
			locale = Locale.getDefault();
		}
		else {
			locale = new Locale(language, country);
		}
		return locale;
	}


	/**
     * XsltFormattingException
     * 
     * @author Rod Johnson
     */
    public static class XsltFormattingException extends NestedRuntimeException {		
		public XsltFormattingException(String msg, Throwable ex) {
			super(msg, ex);
		}
	}

}
