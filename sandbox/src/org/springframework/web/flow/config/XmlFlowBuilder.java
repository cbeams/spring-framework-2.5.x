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
package org.springframework.web.flow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.binding.convert.support.TextToClassConverter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.binding.format.support.LabeledEnumFormatter;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.ActionStateAction;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
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
 *    &lt;!DOCTYPE webflow PUBLIC &quot;-//SPRING//DTD WEBFLOW//EN&quot;
 * 		&quot;http://www.springframework.org/dtd/spring-webflow.dtd&quot;&gt;
 * </pre>
 * 
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
 * <td><i>null</i></td>
 * <td>Specifies the XML resource from which the flow definition is loaded.
 * This is a required property.</td>
 * </tr>
 * <tr>
 * <td>validating</td>
 * <td><i>true</i></td>
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
 * <td><i>empty</i></td>
 * <td>Set the default listeners that will be associated with each execution of
 * the flow created by this builder.</td>
 * </tr>
 * <tr>
 * <td>flowCreator</td>
 * <td><i>{@link BaseFlowBuilder.DefaultFlowCreator DefaultFlowCreator}</i></td>
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

	private static final String START_STATE_ATTRIBUTE = "start-state";

	private static final String ACTION_STATE_ELEMENT = "action-state";

	private static final String ACTION_ELEMENT = "action";

	private static final String BEAN_ATTRIBUTE = "bean";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String AUTOWIRE_ATTRIBUTE = "autowire";

	private static final String CLASSREF_ATTRIBUTE = "classref";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String VIEW_STATE_ELEMENT = "view-state";

	private static final String VIEW_ATTRIBUTE = "view";

	private static final String SUBFLOW_STATE_ELEMENT = "subflow-state";

	private static final String FLOW_ATTRIBUTE = "flow";

	private static final String ATTRIBUTE_MAPPER_ELEMENT = "attribute-mapper";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String EVENT_ATTRIBUTE = "on";

	private static final String TO_ATTRIBUTE = "to";
	
	private static final String PRECONDITION = "precondition";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String VALUE_ELEMENT = "value";

	private Resource resource;

	private boolean validating = true;

	private EntityResolver entityResolver = new FlowDtdResolver();

	/**
	 * The DOM document object for the XML loaded from the resource.
	 */
	protected Document doc;

	/**
	 * Create a new XML flow builder.
	 */
	public XmlFlowBuilder() {
	}

	/**
	 * Create a new XML flow builder.
	 * @param resource resource to read XML flow definitions from
	 */
	public XmlFlowBuilder(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Create a new XML flow builder.
	 * @param resource resource to read XML flow definitions from
	 * @param flowServiceLocator the flow service location strategy to use
	 */
	public XmlFlowBuilder(Resource resource, FlowServiceLocator flowServiceLocator) {
		this.resource = resource;
		setFlowServiceLocator(flowServiceLocator);
	}

	/**
	 * Returns the XML resource from which the flow definition is read.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Set the resource from which an XML flow definition will be read.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Returns whether or not the XML parser will validate the document.
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD. Defaults to true.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Returns the SAX entity resolver used by the XML parser.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
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
		} catch (IOException e) {
			throw new FlowBuilderException("Cannot load the XML flow definition resource '" + resource + "'", e);
		} catch (ParserConfigurationException e) {
			throw new FlowBuilderException("Cannot configure parser to parse the XML flow definition", e);
		} catch (SAXException e) {
			throw new FlowBuilderException("Cannot parse the flow definition XML document '" + resource + "'", e);
		}
		parseFlowDefinition();
		return getFlow();
	}

	public void buildStates() throws FlowBuilderException {
		parseStateDefinitions();
	}

	public void dispose() {
		setFlow(null);
		doc = null;
	}

	/**
	 * Load the flow definition from the configured resource.
	 * This helper method initializes the {@link #doc} member variable.
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

				public void warning(SAXParseException ex) {
					logger.warn("Ignored XML validation warning: " + ex.getMessage(), ex);
				}
			});
			docBuilder.setEntityResolver(this.entityResolver);
			is = resource.getInputStream();
			doc = docBuilder.parse(is);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}

	/**
	 * Parse the XML flow definitions and construct a Flow object.
	 * This helper method will set the "flow" property.
	 */
	protected void parseFlowDefinition() {
		Element root = doc.getDocumentElement();
		String id = root.getAttribute(ID_ATTRIBUTE);
		// set the flow under construction
		setFlow(createFlow(id));
	}

	/**
	 * Parse the state definitions in the XML file and add them to the flow
	 * object we're constructing.
	 */
	protected void parseStateDefinitions() {
		Element root = doc.getDocumentElement();
		String startStateId = root.getAttribute(START_STATE_ATTRIBUTE);
		// get the flow under construction
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
				else if (SUBFLOW_STATE_ELEMENT.equals(element.getNodeName())) {
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
		ActionStateAction[] actionStateActions = parseActionStateActions(element);
		Transition[] transitions = parseTransitions(element);
		new ActionState(flow, id, actionStateActions, transitions);
	}

	/**
	 * Parse given view state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddViewState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		Transition[] transitions = parseTransitions(element);
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			String viewName = element.getAttribute(VIEW_ATTRIBUTE);
			new ViewState(flow, id, viewName, transitions);
		}
		else {
			// a marker state
			new ViewState(flow, id, transitions);
		}
	}

	/**
	 * Parse given sub flow state definition and add a corresponding state to
	 * given flow.
	 */
	protected void parseAndAddSubFlowState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		String flowName = element.getAttribute(FLOW_ATTRIBUTE);
		Flow subFlow = getFlowServiceLocator().getFlow(flowName);
		FlowAttributeMapper mapper = parseAttributeMapper(element);
		Transition[] transitions = parseTransitions(element);
		new SubFlowState(flow, id, subFlow, mapper, transitions);
	}

	/**
	 * Parse given end state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddEndState(Flow flow, Element element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			String viewName = element.getAttribute(VIEW_ATTRIBUTE);
			new EndState(flow, id, viewName);
		}
		else {
			// a marker state
			new EndState(flow, id);
		}
	}

	/**
	 * Parse all given action state action definitions contained in given element.
	 */
	protected ActionStateAction[] parseActionStateActions(Element element) {
		List actionStateActions = new LinkedList();
		List actionElements = DomUtils.getChildElementsByTagName(element, ACTION_ELEMENT);
		for (int i = 0; i < actionElements.size(); i++) {
			actionStateActions.add(parseActionStateAction((Element)actionElements.get(i)));
		}
		return (ActionStateAction[])actionStateActions.toArray(new ActionStateAction[actionStateActions.size()]);
	}

	/**
	 * Parse an action state action definition and return the corresponding
	 * object.
	 */
	protected ActionStateAction parseActionStateAction(Element element) {
		Action targetAction = (Action)parseFlowService(element, Action.class);
		Properties properties = new Properties();
		if (element.hasAttribute(NAME_ATTRIBUTE)) {
			properties.put(ActionStateAction.NAME_PROPERTY, element.getAttribute(NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(METHOD_ATTRIBUTE)) {
			properties.put(ActionStateAction.METHOD_PROPERTY, element.getAttribute(METHOD_ATTRIBUTE));
		}
		List propertyElements = DomUtils.getChildElementsByTagName(element, PROPERTY_ELEMENT);
		for (int i = 0; i < propertyElements.size(); i++) {
			parseAndAddProperty((Element)propertyElements.get(i), properties);
		}
		return new ActionStateAction(targetAction, properties);
	}

	/**
	 * Parse a property definition from given element and add the property
	 * to given action.
	 */
	protected void parseAndAddProperty(Element element, Properties properties) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		if (element.hasAttribute(VALUE_ATTRIBUTE)) {
			properties.put(name, element.getAttribute(VALUE_ATTRIBUTE));
		}
		else {
			List valueElements = DomUtils.getChildElementsByTagName(element, VALUE_ELEMENT);
			Assert.state(valueElements.size() == 1, "A property value should be specified for property '" + name + "'");
			properties.put(name, DomUtils.getTextValue((Element)valueElements.get(0)));
		}
	}

	/**
	 * Find all transition definitions in given state definition and return a
	 * list of corresponding Transition objects.
	 */
	protected Transition[] parseTransitions(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		for (int i = 0; i < transitionElements.size(); i++) {
			transitions.add(parseTransition((Element)transitionElements.get(i)));
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
		String preconditionId = element.getAttribute(PRECONDITION);
		TransitionCriteria precondition = null;
		if (StringUtils.hasText(preconditionId)) {
			NodeList nodeList = element.getOwnerDocument().getElementsByTagName(PRECONDITION);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					Element ele = (Element)node;
					precondition = (TransitionCriteria)parseFlowService(ele, TransitionCriteria.class);
				}
			}
		}
		return new Transition(getTransitionCriteriaCreator().create(event), to, precondition);
	}

	/**
	 * Obtain an attribute mapper reference from given sub flow definition
	 * element and return the identified mapper, or null if no mapper is referenced.
	 */
	protected FlowAttributeMapper parseAttributeMapper(Element element) {
		List attributeMapperElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_MAPPER_ELEMENT);
		if (attributeMapperElements.isEmpty()) {
			return null;
		}
		else {
			Element attributeMapperElement = (Element)attributeMapperElements.get(0);
			return (FlowAttributeMapper)parseFlowService(attributeMapperElement, FlowAttributeMapper.class);
		}
	}

	/**
	 * Parse a flow service definition contained in given element.
	 * @param element the definition element
	 * @param serviceType type of the flow service to parse (Action, FlowAttributeMapper)
	 * @return the flow service
	 * @throws FlowBuilderException when the service definition cannot be parsed
	 */
	protected Object parseFlowService(Element element, Class serviceType) throws FlowBuilderException {
		String serviceId = element.getAttribute(BEAN_ATTRIBUTE);
		if (StringUtils.hasText(serviceId)) {
			// an explicit bean reference was specified
			if (Action.class.equals(serviceType)) {
				return getFlowServiceLocator().getAction(serviceId);
			}
			else if (FlowAttributeMapper.class.equals(serviceType)) {
				return getFlowServiceLocator().getFlowAttributeMapper(serviceId);
			} else if (TransitionCriteria.class.equals(serviceType)) {
				return getFlowServiceLocator().getTransitionCriteria(serviceId);
			}
		}
		else {
			String serviceClassName = element.getAttribute(CLASS_ATTRIBUTE);
			if (StringUtils.hasText(serviceClassName)) {
				// instantiate the service ourselves and wire it using specified
				// autowire mode
				String autowireLabel = element.getAttribute(AUTOWIRE_ATTRIBUTE);
				try {
					AutowireMode autowireMode = (AutowireMode)new LabeledEnumFormatter(AutowireMode.class)
							.parseValue(autowireLabel);
					Class serviceClass = (Class)new TextToClassConverter().convert(serviceClassName);
					if (Action.class.equals(serviceType)) {
						return getFlowServiceLocator().createAction(serviceClass, autowireMode);
					}
					else if (FlowAttributeMapper.class.equals(serviceType)) {
						return getFlowServiceLocator().createFlowAttributeMapper(serviceClass, autowireMode);
					}
				} catch (InvalidFormatException e) {
					throw new FlowBuilderException("Unsupported autowire mode '" + autowireLabel + "'", e);
				}
			}
			else {
				// try service lookup by type
				serviceClassName = element.getAttribute(CLASSREF_ATTRIBUTE);
				Assert.hasText(serviceClassName, "Exactly one of the bean, class, or classref attributes "
						+ "are required for a '" + ClassUtils.getShortName(serviceType) + "' service definition");
				Class serviceClass = (Class)new TextToClassConverter().convert(serviceClassName);
				if (Action.class.equals(serviceType)) {
					return getFlowServiceLocator().getAction(serviceClass);
				}
				else {
					return getFlowServiceLocator().getFlowAttributeMapper(serviceClass);
				}
			}
		}
		throw new FlowBuilderException("Illegal service definition for service of type '"
				+ ClassUtils.getShortName(serviceType) + "'");
	}
}