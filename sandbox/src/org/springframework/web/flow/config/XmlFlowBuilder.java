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
 * Flow builder that builds a flow based on the definitions found in an XML
 * file.
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilder extends BaseFlowBuilder {

	private static final String FLOW_ATTRIBUTE = "flow";
	private static final String ACTION_STATE_NAME = "action-state";
	private static final String VIEW_STATE_NAME = "view-state";
	private static final String SUB_FLOW_STATE_NAME = "sub-flow-state";
	private static final String END_STATE_NAME = "end-state";
	private static final String ACTION_NAME = "action";
	private static final String TRANSITION_NAME = "transition";
	
	private static final String ID_ATTRIBUTE = "id";
	private static final String START_STATE_ATTRIBUTE = "start-state";
	private static final String ATTRIBUTES_MAPPER_ATTRIBUTE = "attributes-mapper";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String VIEW_ATTRIBUTE = "view";
	private static final String MARKER_ATTRIBUTE = "marker";
	private static final String EVENT_ATTRIBUTE = "event";
	private static final String TO_ATTRIBUTE = "to";

	private Resource resource;

	private Document doc;

	private boolean validating = true;

	private EntityResolver entityResolver = new FlowDtdResolver();

	public XmlFlowBuilder() {
	}

	public XmlFlowBuilder(Resource resource) {
		this.resource = resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD.
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

	public void init() throws FlowBuilderException {
		try {
			doc = loadFlowDefinition();
		}
		catch (IOException e) {
			throw new FlowBuilderException("Cannot load the XML flow definition resource", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException("Cannot configure parser to the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException("Cannot parse the flow definition XML document", e);
		}
	}

	public void buildStates() throws FlowBuilderException {
		setFlow(parseFlowDefinition(doc));
	}

	protected Document loadFlowDefinition() throws IOException, ParserConfigurationException, SAXException {
		InputStream is = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(this.validating);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
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
			is = resource.getInputStream();
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
		Element root = doc.getDocumentElement();
		String id = root.getAttribute(ID_ATTRIBUTE);
		String startStateId = root.getAttribute(START_STATE_ATTRIBUTE);

		Flow flow = createFlow(id);

		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element element = (Element)node;
				if (ACTION_STATE_NAME.equals(element.getNodeName())) {
					parseAndAddActionState(flow, element);
				}
				else if (VIEW_STATE_NAME.equals(element.getNodeName())) {
					parseAndAddViewState(flow, element);
				}
				else if (SUB_FLOW_STATE_NAME.equals(element.getNodeName())) {
					parseAndAddSubFlowState(flow, element);
				}
				else if (END_STATE_NAME.equals(element.getNodeName())) {
					parseAndAddEndState(flow, element);
				}
			}
		}

		flow.setStartState((TransitionableState)flow.getState(startStateId));

		return flow;
	}

	protected void parseAndAddActionState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		Action[] actions = parseActions(element);
		Transition[] transitions = parseTransitions(element);
		new ActionState(flow, id, actions, transitions);
	}

	protected void parseAndAddViewState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String viewName = parseViewName(element);
		Transition[] transitions = parseTransitions(element);
		new ViewState(flow, id, viewName, transitions);
	}

	protected void parseAndAddSubFlowState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String flowName = element.getAttribute(FLOW_ATTRIBUTE);
		Flow subFlow = getFlowServiceLocator().getFlow(flowName);
		FlowAttributesMapper mapper = null;
		if (element.hasAttribute(ATTRIBUTES_MAPPER_ATTRIBUTE)) {
			mapper = getFlowServiceLocator().getFlowAttributesMapper(element.getAttribute(ATTRIBUTES_MAPPER_ATTRIBUTE));
		}
		Transition[] transitions = parseTransitions(element);
		new SubFlowState(flow, id, subFlow, mapper, transitions);
	}

	protected void parseAndAddEndState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String viewName = parseViewName(element);
		new EndState(flow, id, viewName);
	}

	protected Action[] parseActions(Element element) {
		List actions = new LinkedList();

		NodeList childNodeList = element.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element)childNode;
				if (ACTION_NAME.equals(childElement.getNodeName())) {
					actions.add(parseAction(childElement));
				}
			}
		}
		return (Action[])actions.toArray(new Action[actions.size()]);
	}

	protected Action parseAction(Element element) {
		String name = element.getAttribute(NAME_ATTRIBUTE);

		return getFlowServiceLocator().getAction(name);
	}

	protected Transition[] parseTransitions(Element element) {
		List transitions = new LinkedList();

		NodeList childNodeList = element.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element)childNode;
				if (TRANSITION_NAME.equals(childElement.getNodeName())) {
					transitions.add(parseTransition(childElement));
				}
			}
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	protected Transition parseTransition(Element element) {
		String event = element.getAttribute(EVENT_ATTRIBUTE);
		String to = element.getAttribute(TO_ATTRIBUTE);
		return new Transition(event, to);
	}

	protected String parseViewName(Element element) {
		String viewName = element.getAttribute(ID_ATTRIBUTE);
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			viewName = element.getAttribute(VIEW_ATTRIBUTE);
		}
		boolean marker = new Boolean(element.getAttribute(MARKER_ATTRIBUTE)).booleanValue();
		if (marker) {
			viewName = null;
		}
		return viewName;
	}
}