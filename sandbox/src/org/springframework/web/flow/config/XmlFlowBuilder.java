/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.web.flow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowModelMapper;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
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
 * file. The XML files read by this class should use the following doctype:
 * 
 * <pre>
 * 
 *  &lt;!DOCTYPE web-flow PUBLIC &quot;-//SPRING//DTD WEB FLOW//EN&quot;
 *  		&quot;http://www.springframework.org/dtd/web-flow.dtd&quot;&gt;
 *  
 * </pre>
 * 
 * <p>
 * Consult the web flow DTD for more information on the XML definition format.
 * An object of this class is normally configured in the Spring application
 * context.
 * 
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>resource</td>
 * <td><i>null </i></td>
 * <td>Specifies the resource from which the flow definition is loaded. This is
 * a required property.</td>
 * </tr>
 * <tr>
 * <td>validating</td>
 * <td><i>true </i></td>
 * <td>Set if the XML parser should validate the document and thus enforce a
 * DTD.</td>
 * </tr>
 * <tr>
 * <td>entityResolver</td>
 * <td><i>{@link FlowDtdResolver}</i></td>
 * <td>Set a SAX entity resolver to be used for parsing.</td>
 * </tr>
 * <tr>
 * <td>flowServiceLocator</td>
 * <td><i>{@link BeanFactoryFlowServiceLocator}</i></td>
 * <td>Set the flow service location strategy to use.</td>
 * </tr>
 * <tr>
 * <td>flowExecutionListeners</td>
 * <td><i>empty </i></td>
 * <td>Set the default listeners that will be associated with each execution of
 * the flow created by this builder.</td>
 * </tr>
 * <tr>
 * <td>flowCreator</td>
 * <td><i>{@link BaseFlowBuilder.DefaultFlowCreator}</i></td>
 * <td>Set the flow creation strategy to use.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.web.flow.config.FlowFactoryBean
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilder extends BaseFlowBuilder {

	private static final String ID_ATTRIBUTE = "id";

	private static final String START_STATE_ELEMENT_ATTRIBUTE = "start-state";

	private static final String ACTION_STATE_ELEMENT = "action-state";

	private static final String ACTION_ELEMENT = "action";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String BEAN_ATTRIBUTE = "bean";

	private static final String VIEW_STATE_ELEMENT = "view-state";

	private static final String VIEW_ATTRIBUTE = "view";

	private static final String MARKER_ATTRIBUTE = "marker";

	private static final String SUB_FLOW_STATE_ELEMENT = "sub-flow-state";

	private static final String FLOW_ATTRIBUTE = "flow";

	private static final String MODEL_MAPPER_ATTRIBUTE = "model-mapper";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String EVENT_ATTRIBUTE = "event";

	private static final String TO_ATTRIBUTE = "to";

	private Resource resource;

	private Document doc;

	private boolean validating = true;

	private EntityResolver entityResolver = new FlowDtdResolver();

	/**
	 * Create a new XML flow builder.
	 */
	public XmlFlowBuilder() {
	}

	/**
	 * Create a new XML flow builder.
	 * @param resource Resource to read XML flow definitions from
	 */
	public XmlFlowBuilder(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Create a new XML flow builder.
	 * @param resource Resource to read XML flow definitions from
	 * @param flowServiceLocator The flow service location strategy to use
	 */
	public XmlFlowBuilder(Resource resource, FlowServiceLocator flowServiceLocator) {
		this.resource = resource;
		setFlowServiceLocator(flowServiceLocator);
	}

	/**
	 * Set the resource from which XML flow definitions will be read.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD. Defaults to true.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * FlowDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * 
	 * @see org.springframework.web.flow.config.FlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public Flow init() throws FlowBuilderException {
		Assert.notNull(resource, "resource is a required property");
		Assert.notNull(getFlowServiceLocator(), "flowServiceLocator is a required property");
		Assert.notNull(getFlowCreator(), "flowCreator is a required property");

		try {
			loadFlowDefinition();
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

		parseFlowDefinition();

		return getFlow();
	}

	public void buildStates() throws FlowBuilderException {
		parseStateDefinitions();
	}

	/**
	 * Load the flow definition from the configured resource.
	 */
	protected void loadFlowDefinition() throws IOException, ParserConfigurationException, SAXException {
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
			doc = docBuilder.parse(is);
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

	/**
	 * Parse the XML flow definitions and construct a Flow object.
	 */
	protected void parseFlowDefinition() {
		Element root = doc.getDocumentElement();
		String id = root.getAttribute(ID_ATTRIBUTE);
		//set the flow under construction
		setFlow(createFlow(id));
	}

	/**
	 * Parse the state definitions in the XML file and add them to the flow
	 * object we're constructing.
	 */
	protected void parseStateDefinitions() {
		Element root = doc.getDocumentElement();
		String startStateId = root.getAttribute(START_STATE_ELEMENT_ATTRIBUTE);

		//get the flow under construction
		Flow flow = getFlow();

		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element element = (Element)node;
				if (ACTION_STATE_ELEMENT.equals(element.getNodeName())) {
					parseAndAddActionState(flow, element);
				}
				else if (VIEW_STATE_ELEMENT.equals(element.getNodeName())) {
					parseAndAddViewState(flow, element);
				}
				else if (SUB_FLOW_STATE_ELEMENT.equals(element.getNodeName())) {
					parseAndAddSubFlowState(flow, element);
				}
				else if (END_STATE_ELEMENT.equals(element.getNodeName())) {
					parseAndAddEndState(flow, element);
				}
			}
		}
		flow.setStartState(startStateId);
	}

	/**
	 * Parse given action state definition and add a corresponding state to
	 * given flow.
	 */
	protected void parseAndAddActionState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String[] actionNames = parseActionNames(element);
		Action[] actions = parseActions(element);
		Transition[] transitions = parseTransitions(element);
		new ActionState(flow, id, actionNames, actions, transitions);
	}

	/**
	 * Parse given view state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddViewState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String viewName = parseViewName(element);
		Transition[] transitions = parseTransitions(element);
		new ViewState(flow, id, viewName, transitions);
	}

	/**
	 * Parse given sub flow state definition and add a corresponding state to
	 * given flow.
	 */
	protected void parseAndAddSubFlowState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String flowName = element.getAttribute(FLOW_ATTRIBUTE);
		Flow subFlow = getFlowServiceLocator().getFlow(flowName);
		FlowModelMapper mapper = null;
		if (element.hasAttribute(MODEL_MAPPER_ATTRIBUTE)) {
			mapper = getFlowServiceLocator().getFlowModelMapper(element.getAttribute(MODEL_MAPPER_ATTRIBUTE));
		}
		Transition[] transitions = parseTransitions(element);
		new SubFlowState(flow, id, subFlow, mapper, transitions);
	}

	/**
	 * Parse given end state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddEndState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String viewName = parseViewName(element);
		new EndState(flow, id, viewName);
	}

	/**
	 * Find all action names (for named actions) in given action state
	 * definition. Unnamed actions have a [null] action name. The returned array
	 * should contain a name (or [null]) for each Action returned by the
	 * <code>parseActions()</code> method.
	 */
	protected String[] parseActionNames(Element element) {
		List actionNames = new LinkedList();

		NodeList childNodeList = element.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element)childNode;
				if (ACTION_ELEMENT.equals(childElement.getNodeName())) {
					if (childElement.hasAttribute(NAME_ATTRIBUTE)) {
						actionNames.add(childElement.getAttribute(NAME_ATTRIBUTE));
					}
					else {
						actionNames.add(null);
					}
				}
			}
		}
		return (String[])actionNames.toArray(new String[actionNames.size()]);
	}

	/**
	 * Find all action definitions in given action state definition and obtain
	 * corresponding Action objects.
	 */
	protected Action[] parseActions(Element element) {
		List actions = new LinkedList();

		NodeList childNodeList = element.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element)childNode;
				if (ACTION_ELEMENT.equals(childElement.getNodeName())) {
					actions.add(parseAction(childElement));
				}
			}
		}
		return (Action[])actions.toArray(new Action[actions.size()]);
	}

	/**
	 * Parse given action definition and return a corresponding Action object.
	 */
	protected Action parseAction(Element element) {
		String bean = element.getAttribute(BEAN_ATTRIBUTE);
		return getFlowServiceLocator().getAction(bean);
	}

	/**
	 * Find all transition definitions in given state definition and return a
	 * list of corresponding Transition objects.
	 */
	protected Transition[] parseTransitions(Element element) {
		List transitions = new LinkedList();

		NodeList childNodeList = element.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element)childNode;
				if (TRANSITION_ELEMENT.equals(childElement.getNodeName())) {
					transitions.add(parseTransition(childElement));
				}
			}
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Parse a transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition parseTransition(Element element) {
		String event = element.getAttribute(EVENT_ATTRIBUTE);
		String to = element.getAttribute(TO_ATTRIBUTE);
		return new Transition(event, to);
	}

	/**
	 * Obtain a logical view name from given state definition. Could return null
	 * if there is no logical view name specified (that is, the state definition
	 * defines a <i>marker </i> state).
	 */
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