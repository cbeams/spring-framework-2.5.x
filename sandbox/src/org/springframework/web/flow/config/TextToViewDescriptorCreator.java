package org.springframework.web.flow.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.convert.support.TextToClass;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.SimpleViewDescriptorCreator;
import org.springframework.web.flow.ViewDescriptorCreator;

public class TextToViewDescriptorCreator extends AbstractConverter {
	
	/**
	 * Prefix used when the encoded view name wants to identify the
	 * class of a ViewDescriptorCreator.
	 */
	public static final String CLASS_PREFIX = "class:";

	/**
	 * Prefix used when the encoded view name wants to identify the
	 * class of a ViewDescriptorCreator.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	protected Object doConvert(Object source, Class targetClass) throws ConversionException {
		return createDefaultViewDescriptorCreator((String)source);
	}

	/**
	 * Create the default view descriptor producer (factory)
	 * @param encodedView the encoded view name
	 * @return the newly created producer
	 * @throws ConversionException when there are problems decoding the view name
	 * @throws BeansException when there are problems instantiating the producer
	 */
	protected ViewDescriptorCreator createDefaultViewDescriptorCreator(String encodedView)
			throws ConversionException, BeansException {
		if (StringUtils.hasText(encodedView)) {
			if (encodedView.startsWith(CLASS_PREFIX)) {
				String className = encodedView.substring(CLASS_PREFIX.length());
				Class clazz = (Class)new TextToClass().convert(className);
				return (ViewDescriptorCreator)BeanUtils.instantiateClass(clazz);
			} else if (encodedView.startsWith(REDIRECT_PREFIX)) {
				String viewInfo = encodedView.substring(REDIRECT_PREFIX.length());
				return new RedirectViewDescriptorCreator(viewInfo);
			}
		}
		return new SimpleViewDescriptorCreator(encodedView);
	}
	
	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewDescriptorCreator.class } ;
	}
}