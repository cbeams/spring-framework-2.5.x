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
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.ElementProcessor;
import org.springframework.core.io.Resource;

import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.StringWriter;

/**
 * A useful class for implementation inheritence which provides helper methods for creating
 * new Spring configuration items and for tranforming DOM nodes.
 *
 * @author James Strachan
 * @version $Revision: 1.2 $
 */
public class ElementProcessorSupport {


    /**
     * Recursively process all the child elements with the given processor
     */
    protected void processChildren(ElementProcessor processor, Element element, BeanDefinitionReader beanDefinitionReader, Resource resource) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0, size = nodeList.getLength(); i < size; i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element child = (Element) node;
                processor.processElement(child, beanDefinitionReader, resource);
                processChildren(processor, child, beanDefinitionReader, resource);
            }
        }
    }
    
    // Helper methods to add new Spring XML elements

    /**
     * Creates and adds a new bean node on the given owner element
     */
    protected Element addBeanElement(Element owner, String className) {
        Element property = owner.getOwnerDocument().createElement("bean");
        property.setAttribute("class", className);
        owner.appendChild(property);
        return property;
    }


    /**
     * Creates and adds a new property node on the given bean element
     */
    protected Element addPropertyElement(Element bean, String propertyName) {
        Element property = bean.getOwnerDocument().createElement("property");
        property.setAttribute("name", propertyName);
        bean.appendChild(property);
        return property;
    }


    /**
     * Adds a new property element to the given bean element with the name and value
     */
    protected Element addPropertyElement(Element bean, String propertyName, String value) {
        Element property = addPropertyElement(bean, propertyName);
        property.setAttribute("value", value);
        return property;
    }

    /**
     * Adds a new constructor argument element to the given bean
     */
    protected void addConstructorValueNode(Element bean, String value) {
        Element constructorArg = bean.getOwnerDocument().createElement("constructor-arg");
        constructorArg.setAttribute("value", value);
        bean.appendChild(constructorArg);
    }


    // DOM utilities

    /**
     * Moves the content of the given element to the given element
     */
    protected void moveContent(Element from, Element to) {
        // lets copy across all the remainingattributes
        NamedNodeMap attributes = from.getAttributes();
        for (int i = 0, size = attributes.getLength(); i < size; i++ ) {
            Attr node = (Attr) attributes.item(i);
            to.setAttributeNS(node.getNamespaceURI(), node.getName(), node.getValue());
        }

        // lets move the child nodes across
        NodeList childNodes = from.getChildNodes();
        while (childNodes.getLength() > 0) {
            Node node = childNodes.item(0);
            from.removeChild(node);
            to.appendChild(node);
        }
    }

    protected void logXmlGenerated(Log log, String message, Node node) {
        if (log.isDebugEnabled()) {
            try {
                String xml = asXML(node);
                log.debug(message + ": " + xml);
            }
            catch (TransformerException e) {
                log.warn("Could not transform generated XML into text: " + e, e);
            }
        }
        /*
        try {
            System.out.println("Adding new components: " + asXML(node));
        }
        catch (TransformerException e) {
            // ignore
        }
        */
    }

    /**
     * A helper method useful for debugging and logging which will convert the given DOM node into XML text
     */
    protected String asXML(Node node) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(buffer));
        return buffer.toString();
    }
}
