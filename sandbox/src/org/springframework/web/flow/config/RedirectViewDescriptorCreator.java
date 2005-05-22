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
package org.springframework.web.flow.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.binding.expression.support.StaticEvaluator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.SimpleViewDescriptorCreator;
import org.springframework.web.flow.ViewDescriptor;

/**
 * View descriptor creator that creates view descriptors requesting a
 * client side redirect.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class RedirectViewDescriptorCreator extends SimpleViewDescriptorCreator {

	private Map expressions = new HashMap();

	/**
	 * Create a new redirecting view descriptor creator.
	 * @param encodedView the encoded view
	 */
	public RedirectViewDescriptorCreator(String encodedView) {
		parseEncodedView(encodedView, new OgnlExpressionParser());
	}

	/**
	 * Create a new redirecting view descriptor creator.
	 * @param encodedView the encoded view
	 * @param parser the parser to use to decode the view
	 */
	public RedirectViewDescriptorCreator(String encodedView, ExpressionParser parser) {
		parseEncodedView(encodedView, parser);
	}
	
	/**
	 * Helper method to parse an encoded view with given parser.
	 * @param encodedView the view to decode
	 * @param parser the parser to use
	 */
	protected void parseEncodedView(String encodedView, ExpressionParser parser) {
		//TODO improve this - possibly reuse url decoding logic
		Assert.hasText(encodedView, "The encoded view is required for a redirect view");
		// encoded view is of the form "/viewName?name=value&name=value"
		String[] array = StringUtils.delimitedListToStringArray(encodedView, "?");
		setViewName(array[0]);
		if (array.length == 2) {
			array = StringUtils.delimitedListToStringArray(array[1], "&");
			for (int i = 0; i < array.length; i++) {
				String[] nameValue = StringUtils.delimitedListToStringArray(array[i], "=");
				String name = nameValue[0];
				ExpressionEvaluator value = getValue((String)nameValue[1], parser);
				expressions.put(name, value);
			}
		}
	}
	
	/**
	 * Parse given string value and return an expression evaluator which
	 * evaluated the value.
	 * @param valueString the value to parse
	 * @param parser the parser to use
	 * @return the corresponding expression evaluator
	 */
	protected ExpressionEvaluator getValue(String valueString, ExpressionParser parser) {
		if (parser.isExpression(valueString)) {
			return parser.parseExpression(valueString);
		}
		else {
			return new StaticEvaluator(valueString);
		}
	}

	public ViewDescriptor createViewDescriptor(RequestContext context) {
		ViewDescriptor viewDescriptor = new ViewDescriptor(getViewName());
		viewDescriptor.setRedirect(true);
		Iterator it = expressions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String attributeName = (String)entry.getKey();
			ExpressionEvaluator evaluator = (ExpressionEvaluator)entry.getValue();
			viewDescriptor.addObject(attributeName, evaluator.evaluate(context, getEvaluationContext(context)));
		}
		return viewDescriptor;
	}

	/**
	 * Setup the expression evaluation context.
	 */
	protected Map getEvaluationContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}
}