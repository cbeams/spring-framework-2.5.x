package org.springframework.ui.freemarker;

import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Utility class for working with FreeMarker.
 * Provides convenience methods to process a FreeMarker template with a model.
 * @author Juergen Hoeller
 * @since 14.03.2004
 */
public abstract class FreemarkerTemplateUtils {

	/**
	 * Process the specified FreeMarker template with the given model and write
	 * the result to the given Writer.
	 * @param model the model object, typically a Map that contains model names
	 * as keys and model objects as values
	 * @return the result as String
	 * @throws IOException if the template wasn't found or couldn't be read
	 * @throws freemarker.template.TemplateException if rendering failed
	 */
	public static String processTemplateIntoString(Template template, Object model)
			throws IOException, TemplateException {
		StringWriter result = new StringWriter();
		template.process(model, result);
		return result.toString();
	}

}
