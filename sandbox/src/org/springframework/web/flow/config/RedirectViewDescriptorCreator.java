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

public class RedirectViewDescriptorCreator extends SimpleViewDescriptorCreator {

	private Map expressions = Collections.EMPTY_MAP;

	public RedirectViewDescriptorCreator(String encodedView) {
		super();
		parseEncodedView(encodedView, new OgnlExpressionParser());
	}

	public RedirectViewDescriptorCreator(String encodedView, ExpressionParser parser) {
		super();
		parseEncodedView(encodedView, parser);
	}
	
	protected void parseEncodedView(String encodedView, ExpressionParser parser) {
		//TODO improve this - possibly reuse url decoding logic
		Assert.hasText(encodedView, "The encoded view is required for a redirect view");
		String[] array = StringUtils.delimitedListToStringArray(encodedView, "?");
		setViewName(array[0]);
		if (array.length == 2) {
			array = StringUtils.delimitedListToStringArray(array[1], "&");
			for (int i = 0; i < array.length; i++) {
				String[] nameValue = StringUtils.delimitedListToStringArray(array[i], "=");
				expressions.put(nameValue[0], getValue((String)nameValue[1], parser));
			}
		}
	}
	
	protected ExpressionEvaluator getValue(String valueString, ExpressionParser parser) {
		if (parser.isExpression(valueString)) {
			return parser.parseExpression(valueString);
		} else {
			return new StaticEvaluator(valueString);
		}
	}

	public ViewDescriptor createViewDescriptor(RequestContext context) {
		Map model = null;
		if (!expressions.isEmpty()) {
			Iterator it = expressions.entrySet().iterator();
			model = new HashMap(expressions.size());
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String attributeName = (String)entry.getKey();
				ExpressionEvaluator evaluator = (ExpressionEvaluator)entry.getValue();
				model.put(attributeName, evaluator.evaluate(context, getEvaluationContext(context)));
			}
		}
		return new ViewDescriptor(getViewName(), model) {
			public boolean isRedirect() {
				return true;
			}
		};
	}

	protected Map getEvaluationContext(RequestContext context) {
		return null;
	}
}