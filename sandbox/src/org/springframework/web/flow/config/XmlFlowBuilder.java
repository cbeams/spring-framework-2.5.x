package org.springframework.web.flow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.Resource;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionableState;
import org.springframework.web.flow.ViewState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Flow builder that builds a flow based on the definitions found in an XML file.
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilder extends BaseFlowBuilder {
	
	private Resource resource;
	private Document doc;

	private boolean validating = true;
	private EntityResolver entityResolver=new FlowDtdResolver();
	
	public XmlFlowBuilder() {
	}
	
	public XmlFlowBuilder(Resource resource) {
		this.resource=resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a DTD.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * FlowDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * 
	 * @see FlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}
	
	protected Document loadFlowDefinition() throws IOException, ParserConfigurationException, SAXException {
		InputStream is=null;
		try {
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			factory.setValidating(this.validating);
			DocumentBuilder docBuilder=factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException ex) throws SAXException {
					throw ex;
				}
				
				public void fatalError(SAXParseException ex) throws SAXException {
					throw ex;
				}
				
				public void warning(SAXParseException ex) throws SAXException {
					logger.warn("Ignored XML validation warning: " + ex.getMessage(), ex);
				}
			});
			docBuilder.setEntityResolver(this.entityResolver);
			is=resource.getInputStream();
			return docBuilder.parse(is);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}
	
	protected Flow parseFlowDefinition(Document doc) {
		Element root=doc.getDocumentElement();
		
		String name=root.getAttribute("name");
		String startStateId=root.getAttribute("start-state");
		
		Flow flow=createFlow(name);
		
		NodeList nodeList=root.getChildNodes();
		for (int i=0; i<nodeList.getLength(); i++) {
			Node node=nodeList.item(i);
			if (node instanceof Element) {
				Element element=(Element)node;
				if ("action-state".equals(element.getNodeName())) {
					parseAndAddActionState(flow, element);
				}
				else if ("view-state".equals(element.getNodeName())) {
					parseAndAddViewState(flow, element);
				}
				else if ("sub-flow-state".equals(element.getNodeName())) {
					parseAndAddSubFlowState(flow, element);
				}
				else if ("end-state".equals(element.getNodeName())) {
					parseAndAddEndState(flow, element);
				}
			}
		}
		
		flow.setStartState((TransitionableState)flow.getState(startStateId));
		
		return flow;
	}
	
	protected void parseAndAddActionState(Flow flow, Element element) {
		String id=element.getAttribute("id");

		Action[] actions=parseActions(element);
		Transition[] transitions=parseTransitions(element);
		
		new ActionState(flow, id, actions, transitions);
	}
	
	protected void parseAndAddViewState(Flow flow, Element element) {
		String id=element.getAttribute("id");
		String viewName=parseViewName(element);

		Transition[] transitions=parseTransitions(element);

		new ViewState(flow, id, viewName, transitions);
	}
	
	protected void parseAndAddSubFlowState(Flow flow, Element element) {
		String id=element.getAttribute("id");
		String flowName=element.getAttribute("flow");
		
		Flow subFlow=getFlowServiceLocator().getFlow(flowName);
		
		FlowAttributesMapper mapper=null;
		if (element.hasAttribute("attributes-mapper")) {
			mapper=getFlowServiceLocator().getFlowAttributesMapper(element.getAttribute("attributes-mapper"));
		}

		Transition[] transitions=parseTransitions(element);

		new SubFlowState(flow, id, subFlow, mapper, transitions);
	}
	
	protected void parseAndAddEndState(Flow flow, Element element) {
		String id=element.getAttribute("id");
		String viewName=parseViewName(element);
		
		new EndState(flow, id, viewName);
	}
	
	protected Action[] parseActions(Element element) {
		List actions=new LinkedList();

		NodeList childNodeList=element.getChildNodes();
		for (int i=0; i<childNodeList.getLength(); i++) {
			Node childNode=childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement=(Element)childNode;
				if ("action".equals(childElement.getNodeName())) {
					actions.add(parseAction(childElement));
				}
			}
		}

		return (Action[])actions.toArray(new Action[actions.size()]);
	}

	protected Action parseAction(Element element) {
		String name=element.getAttribute("name");
		
		return getFlowServiceLocator().getAction(name);
	}
	
	protected Transition[] parseTransitions(Element element) {
		List transitions=new LinkedList();
		
		NodeList childNodeList=element.getChildNodes();
		for (int i=0; i<childNodeList.getLength(); i++) {
			Node childNode=childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement=(Element)childNode;
				if ("transition".equals(childElement.getNodeName())) {
					transitions.add(parseTransition(childElement));
				}
			}
		}
		
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}
	
	protected Transition parseTransition(Element element) {
		String name=element.getAttribute("name");
		String to=element.getAttribute("to");
		
		return new Transition(name, to);
	}
	
	protected String parseViewName(Element element) {
		String viewName=element.getAttribute("id");
		if (element.hasAttribute("view")) {
			viewName=element.getAttribute("view");
		}
		boolean marker=new Boolean(element.getAttribute("marker")).booleanValue();
		if (marker) {
			viewName=null;
		}
		return viewName;
	}

	public void init() throws FlowBuilderException {
		try {
			doc=loadFlowDefinition();
		}
		catch (IOException e) {
			throw new FlowBuilderException("cannot load flow definition", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException("cannot configure parser to load flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException("cannot parse flow definition", e);
		}
	}

	public void buildStates() throws FlowBuilderException {
		setFlow(parseFlowDefinition(doc));
	}

}
