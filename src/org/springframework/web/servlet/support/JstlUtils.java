package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;

/**
 * Helper class for preparing JSTL views.
 * @author Juergen Hoeller
 * @since 20.08.2003
 */
public abstract class JstlUtils {

	public static final String REQUEST_SCOPE_SUFFIX = ".request";

	/**
	 * Exposes JSTL-specific request attributes specifying locale
	 * and resource bundle for JSTL's formatting and message tags,
	 * using Spring's locale and message source.
	 * @param request current HTTP request
	 * @param messageSource the MessageSource to expose,
	 * typically the current application context
	 * @throws ServletException
	 */
	public static void exposeLocalizationContext(HttpServletRequest request, MessageSource messageSource)
	    throws ServletException {

		// add JSTL locale and LocalizationContext request attributes
		Locale jstlLocale = RequestContextUtils.getLocale(request);
		ResourceBundle bundle = new MessageSourceResourceBundle(messageSource, jstlLocale);
		LocalizationContext jstlContext = new LocalizationContext(bundle, jstlLocale);

		// for JSTL implementations that stick to the config names (e.g. Resin's)
		request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
		request.setAttribute(Config.FMT_LOCALE, jstlLocale);

		// for JSTL implementations that append the scope to the config names (e.g. Jakarta's)
		request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + REQUEST_SCOPE_SUFFIX, jstlContext);
		request.setAttribute(Config.FMT_LOCALE + REQUEST_SCOPE_SUFFIX, jstlLocale);
	}

}
