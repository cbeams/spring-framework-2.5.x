package org.springframework.web.servlet.handler;

import java.util.List;
import java.util.ArrayList;

import org.springframework.util.StringUtils;
import org.springframework.context.ApplicationContextException;

/**
 * Implementation of the HandlerMapping interface to map from URLs
 * to beans. This is the default implementation used by the
 * DispatcherServlet, but is somewhat naive. A SimpleUrlHandlerMapping
 * or a custom handler mapping should be used by preference.
 *
 * <p>The mapping is from URL to bean name. Thus an incoming URL
 * "/foo" would map to a handler named "/foo", or to "/foo /foo2"
 * in case of multiple mappings to a single handler.
 * Note: In XML definitions, you'll need to use an alias "name=/foo"
 * in the bean definition, as the XML id may not contain slashes.
 *
 * <p>Supports direct matches (given "/test" -> registered "/test")
 * and "*" matches (given "/test" -> registered "/t*").
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleUrlHandlerMapping
 */
public class BeanNameUrlHandlerMapping extends AbstractUrlHandlerMapping {
	
	public void initApplicationContext() throws ApplicationContextException {
		logger.debug("Looking for URL mappings...");
		String[] urlMaps = getApplicationContext().getBeanDefinitionNames();

		// take anything beginning with a slash in the bean name
		for (int i = 0; i < urlMaps.length; i++) {
			String[] urls = checkForUrl(urlMaps[i]);
			if (urls != null) {
				logger.debug("Found URL mapping [" + urlMaps[i] + "]");
				Object handler = initHandler(getApplicationContext().getBean(urlMaps[i]),
				                             StringUtils.arrayToCommaDelimitedString(urls));

				// create a mapping to each part of the path
				for (int j = 0; j < urls.length; j++) {
					registerHandler(urls[j], handler);
				}
			}
			else {
				logger.debug("Rejected bean name '" + urlMaps[i] + "'");
			}
		}
	}

	/**
	 * Check name and aliases of the given bean for URLs,
	 * detected by starting with "/".
	 */
	private String[] checkForUrl(String beanName) {
		List urls = new ArrayList();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		String[] aliases = getApplicationContext().getAliases(beanName);
		for (int j = 0; j < aliases.length; j++) {
			if (aliases[j].startsWith("/")) {
				urls.add(aliases[j]);
			}
		}
		return (String[]) urls.toArray(new String[urls.size()]);
	}

}
