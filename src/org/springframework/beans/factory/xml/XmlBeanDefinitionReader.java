package org.springframework.beans.factory.xml;

import java.io.FileInputStream;
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
	 * Load definitions from the given file.
	 * @param fileName name of the file containing the XML document
	 */
	public void loadBeanDefinitions(String fileName) throws BeansException {
		try {
			logger.info("Loading XmlBeanFactory from file [" + fileName + "]");
			loadBeanDefinitions(new FileInputStream(fileName));
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Can't open file [" + fileName + "]", ex);
		}
	}

	/**
	 * Load definitions from the given input stream and close it.
	 * @param is InputStream containing XML
	 */
	public void loadBeanDefinitions(InputStream is) throws BeansException {
		if (is == null)
			throw new BeanDefinitionStoreException("InputStream cannot be null: expected an XML file", null);

		try {
			logger.info("Loading XmlBeanFactory from InputStream [" + is + "]");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			logger.debug("Using JAXP implementation [" + factory + "]");
			factory.setValidating(this.validating);
			DocumentBuilder db = factory.newDocumentBuilder();
			db.setErrorHandler(new BeansErrorHandler());
			db.setEntityResolver(this.entityResolver != null ? this.entityResolver : new BeansDtdResolver());
			Document doc = db.parse(is);
			loadBeanDefinitions(doc);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException("ParserConfiguration exception parsing XML", ex);
		}
		catch (SAXParseException ex) {
			throw new BeanDefinitionStoreException("Line " + ex.getLineNumber() + " in XML document is invalid", ex);
		}
		catch (SAXException ex) {
			throw new BeanDefinitionStoreException("XML document is invalid", ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document", ex);
		}
		finally {
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex) {
				throw new FatalBeanException("IOException closing stream for XML document", ex);
			}
		}
	}

	/**
	 * Load bean definitions from the given DOM document.
	 * All calls go through this.
	 * @param doc the DOM document
	 */
	public void loadBeanDefinitions(Document doc) throws BeansException {
		XmlBeanDefinitionParser parser = (XmlBeanDefinitionParser) BeanUtils.instantiateClass(this.parserClass);
		parser.loadBeanDefinitions(getBeanFactory(), getBeanClassLoader(), doc);
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
