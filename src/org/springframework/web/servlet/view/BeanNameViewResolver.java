package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Simple implementation of ViewResolver that interprets a view name
 * as bean name in the current application context, i.e. in the XML
 * file of the executing DispatcherServlet.
 *
 * <p>This resolver can be handy for small applications, keeping all
 * definitions ranging from controllers to views are in the same place.
 * For normal applications, XmlViewResolver will be the better choice,
 * as it separates the XML bean definitions in a dedicated views file.
 * View beans should virtually never have references to any other
 * application beans - such a separation will make this clear.
 *
 * <p>This ViewResolver does not support internationalization.
 * Consider ResourceBundleViewResolver if you need to apply
 * different view resources per locale.
 *
 * @author Juergen Hoeller
 * @since 18.06.2003
 * @see XmlViewResolver
 * @see ResourceBundleViewResolver
 */
public class BeanNameViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	public View resolveViewName(String viewName, Locale locale) throws BeansException {
		return (View) getApplicationContext().getBean(viewName, View.class);
	}

}
