package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;

/**
 * Bean definition reader for Spring's default XML bean definition format.
 * Typically applied to a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the given bean factory superclass,
 * and relies on the latter's implementation of the BeanDefinitionRegistry interface.
 * It supports singletons, prototypes, and references to either of these kinds of bean.

 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see #setParserClass
 * @version $Id: XmlBeanDefinitionReader.java,v 1.3 2003-12-30 00:15:37 jhoeller Exp $
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean validating = true;

	private EntityResolver entityResolver;

	private Class parserClass = DefaultXmlBeanDefinitionParser.class;


	/**
	 * Create new XmlBeanDefinitionReader for the given bean factory.
	 */
	public XmlBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a DTD.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default, BeansDtdResolver
	 * will be used. Can be overridden for custom entity resolution, e.g. relative
	 * to some specific base path.
	 * @see org.springframework.beans.factory.xml.BeansDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Set the XmlBeanDefinitionParser implementation to use.
	 * Default is DefaultXmlBeanDefinitionParser.
	 * @see XmlBeanDefinitionParser
	 * @see DefaultXmlBeanDefinitionParser
	 */
	public void setParserClass(Class parserClass) {
		if (this.parserClass == null || !XmlBeanDefinitionParser.class.isAssignableFrom(parserClass)) {
			throw new IllegalArgumentException("parserClass must be a XmlBeanDefinitionParser");
		}
		this.parserClass = parserClass;
	}

	/**
	 * Load bean definitions from the given resource.
	 * @param resource XML resource to load bean definitions from
	 * @throws BeansException in case of loading or parsing errors
	 */
	public void loadBeanDefinitions(Resource resource) throws BeansException {
		if (resource == null) {
			throw new BeanDefinitionStoreException("Resource cannot be null: expected an XML file");
		}
		InputStream is = null;
		try {
			logger.info("Loading XmlBeanFactory from resource [" + resource + "]");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			logger.debug("Using JAXP implementation [" + factory + "]");
			factory.setValidating(this.validating);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new BeansErrorHandler());
			docBuilder.setEntityResolver(this.entityResolver != null ? this.entityResolver : new BeansDtdResolver());
			is = resource.getInputStream();
			Document doc = docBuilder.parse(is);
			loadBeanDefinitions(doc, resource);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException("Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (SAXParseException ex) {
			throw new BeanDefinitionStoreException("Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new BeanDefinitionStoreException("XML document from " + resource + " is invalid", ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document from " + resource, ex);
		}
		finally {
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex) {
				throw new FatalBeanException("IOException closing stream for XML document from " + resource, ex);
			}
		}
	}

	/**
	 * Load bean definitions from the given DOM document.
	 * All calls go through this.
	 * @param doc the DOM document
	 */
	public void loadBeanDefinitions(Document doc, Resource resource) throws BeansException {
		XmlBeanDefinitionParser parser = (XmlBeanDefinitionParser) BeanUtils.instantiateClass(this.parserClass);
		parser.loadBeanDefinitions(getBeanFactory(), getBeanClassLoader(), doc, resource);
	}


	/**
	 * Private implementation of SAX ErrorHandler used when validating XML.
	 */
	private static class BeansErrorHandler implements ErrorHandler {

		/**
		 * We can't use the enclosing class' logger as it's protected and inherited.
		 */
		private final static Log logger = LogFactory.getLog(XmlBeanFactory.class);

		public void error(SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void warning(SAXParseException ex) throws SAXException {
			logger.warn("Ignored XML validation warning: " + ex);
		}
	}

}
