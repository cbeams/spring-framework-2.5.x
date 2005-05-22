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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.binding.format.support.LabeledEnumFormatter;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.binding.support.Mapping;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.AnnotatedAction;
import org.springframework.web.flow.DecisionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.State;
import org.springframework.web.flow.SubflowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.TransitionCriteriaFactory;
import org.springframework.web.flow.ViewDescriptorCreator;
import org.springframework.web.flow.ViewState;
import org.springframework.web.flow.action.ActionTransitionCriteria;
import org.springframework.web.flow.action.MultiAction;
import org.springframework.web.flow.support.ParameterizableFlowAttributeMapper;
import org.springframework.web.flow.support.TransitionCriteriaChain;
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
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td><i>null</i></td>
 * <td>Specifies the XML file location from which the flow definition is loaded.
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
 * </table>
 * 
 * @see org.springframework.web.flow.config.FlowFactoryBean
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends BaseFlowBuilder {
	
	// recognized XML elements and attributes

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

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VIEW_STATE_ELEMENT = "view-state";

	private static final String VIEW_ATTRIBUTE = "view";

	private static final String DECISION_STATE_ELEMENT = "decision-state";

	private static final String IF_ELEMENT = "if";

	private static final String TEST_ATTRIBUTE = "test";

	private static final String THEN_ATTRIBUTE = "then";

	private static final String ELSE_ATTRIBUTE = "else";

	private static final String SUBFLOW_STATE_ELEMENT = "subflow-state";

	private static final String FLOW_ATTRIBUTE = "flow";

	private static final String ATTRIBUTE_MAPPER_ELEMENT = "attribute-mapper";

	private static final String INPUT_ELEMENT = "input";

	private static final String OUTPUT_ELEMENT = "output";

	private static final String AS_ATTRIBUTE = "as";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String ON_ATTRIBUTE = "on";

	private static final String TO_ATTRIBUTE = "to";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String VALUE_ELEMENT = "value";
	
	private static final String SETUP_ELEMENT = "setup";
	
	private static final String ON_ERROR_ATTRIBUTE = "on-error";
	
	
	/**
	 * Internal helper class capturing flow artifact definition info.
	 * 
	 * @author Erwin Vervaet
	 */
	private static class FlowArtifact {
		
		public String bean;
		public Class classRef;
		public Class clazz;
		public AutowireMode autowire;

		public boolean isBeanRef() {
			return StringUtils.hasText(bean);
		}
		
		public boolean isClassRef() {
			return classRef != null;
		}
		
		public boolean shouldCreate() {
			return clazz != null;
		}
	}
	
	private Resource location;

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
	 * @param location resource to read XML flow definitions from
	 */
	public XmlFlowBuilder(Resource location) {
		this.location = location;
	}

	/**
	 * Create a new XML flow builder.
	 * @param location resource to read XML flow definitions from
	 * @param flowServiceLocator the flow service location strategy to use
	 */
	public XmlFlowBuilder(Resource location, FlowServiceLocator flowServiceLocator) {
		this.location = location;
		setFlowServiceLocator(flowServiceLocator);
	}

	/**
	 * Returns the XML resource from which the flow definition is read.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Set the resource from which an XML flow definition will be read.
	 */
	public void setLocation(Resource location) {
		this.location = location;
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
		Assert.notNull(location, "location is a required property");
		Assert.notNull(getFlowServiceLocator(), "flowServiceLocator is a required property");
		try {
			loadFlowDefinition();
		}
		catch (IOException e) {
			throw new FlowBuilderException("Cannot load the XML flow definition resource '" + location + "'", e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException("Cannot configure parser to parse the XML flow definition", e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException("Cannot parse the flow definition XML document '" + location + "'", e);
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
			factory.setValidating(isValidating());
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
			docBuilder.setEntityResolver(getEntityResolver());
			is = location.getInputStream();
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
	 * Parse a class reference from named attribute of given element.
	 */
	protected Class parseClass(Element element, String attributeName) {
		return (Class)converterFor(Class.class).execute(element.getAttribute(attributeName));
	}
	
	/**
	 * Parse and return the autowire mode specified in given element.
	 */
	protected AutowireMode parseAutowireMode(Element element) {
		if (element.hasAttribute(AUTOWIRE_ATTRIBUTE)) {
			try {
				return (AutowireMode)
					new LabeledEnumFormatter(AutowireMode.class).parseValue(element.getAttribute(AUTOWIRE_ATTRIBUTE));
			}
			catch (InvalidFormatException e) {
				throw new FlowBuilderException("Unsupported autowire mode '" + element.getAttribute(AUTOWIRE_ATTRIBUTE) + "'", e);
			}
		}
		else {
			return AutowireMode.DEFAULT;
		}
	}
	
	/**
	 * Parse a flow artifact definition contained in given XML element.
	 */
	protected FlowArtifact parseFlowArtifactDefinition(Element element) {
		FlowArtifact res = new FlowArtifact();
		res.bean = element.getAttribute(BEAN_ATTRIBUTE);
		if (element.hasAttribute(CLASSREF_ATTRIBUTE)) {
			res.classRef = parseClass(element, CLASSREF_ATTRIBUTE);
		}
		if (element.hasAttribute(CLASS_ATTRIBUTE)) {
			res.clazz = parseClass(element, CLASS_ATTRIBUTE);
		}
		res.autowire = parseAutowireMode(element);
		return res;
	}

	/**
	 * Parse the XML flow definitions and construct a Flow object.
	 * This helper method will set the "flow" property.
	 */
	protected void parseFlowDefinition() {
		Element element = doc.getDocumentElement();
		FlowArtifact flowDef = parseFlowArtifactDefinition(element);
		Flow flow;
		if (flowDef.isBeanRef()) {
			flow = getFlowServiceLocator().getFlow(flowDef.bean);
		}
		else if (flowDef.isClassRef()) {
			flow = getFlowServiceLocator().getFlow(flowDef.classRef);
		}
		else if (flowDef.shouldCreate()) {
			flow = getFlowServiceLocator().createFlow(flowDef.clazz, flowDef.autowire);
		}
		else {
			flow = getFlowServiceLocator().createFlow(flowDef.autowire);
		}
		flow.setId(element.getAttribute(ID_ATTRIBUTE));
		flow.setProperties(parseProperties(element));
		// set the flow under construction
		setFlow(flow);
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
				else if (DECISION_STATE_ELEMENT.equals(element.getNodeName())) {
					parseAndAddDecisionState(flow, element);
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
	 * Parse a state definition from given element, returning given fallback state
	 * in case no other state object is identified.
	 */
	protected State parseStateDefinition(Element element, Class requiredStateType, State fallbackInstance) {
		FlowArtifact stateDef = parseFlowArtifactDefinition(element);
		State state;
		if (stateDef.isBeanRef()) {
			state = getFlowServiceLocator().getState(stateDef.bean);
		}
		else if (stateDef.isClassRef()) {
			state = getFlowServiceLocator().getState(stateDef.classRef);
		}
		else if (stateDef.shouldCreate()) {
			state = getFlowServiceLocator().createState(stateDef.clazz, stateDef.autowire);
		}
		else {
			state = fallbackInstance;
		}
		Assert.isInstanceOf(requiredStateType, state,
				"The state object for the '" + element.getAttribute(ID_ATTRIBUTE)
				+ "' state definition should subclass '" + ClassUtils.getShortName(requiredStateType) + "'");
		return state;
	}

	/**
	 * Parse given action state definition and add a corresponding state to
	 * given flow.
	 */
	protected void parseAndAddActionState(Flow flow, Element element) {
		ActionState actionState = (ActionState)parseStateDefinition(element, ActionState.class, new ActionState());
		actionState.setId(element.getAttribute(ID_ATTRIBUTE));
		actionState.setFlow(flow);
		actionState.addActions(parseAnnotatedActions(element));
		actionState.addAll(parseTransitions(element));
		actionState.setProperties(parseProperties(element));
	}

	/**
	 * Parse given view state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddViewState(Flow flow, Element element) {
		ViewState viewState = (ViewState)parseStateDefinition(element, ViewState.class, new ViewState());
		viewState.setId(element.getAttribute(ID_ATTRIBUTE));
		viewState.setFlow(flow);
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			ViewDescriptorCreator creator = (ViewDescriptorCreator)
				converterFor(ViewDescriptorCreator.class).execute(element.getAttribute(VIEW_ATTRIBUTE));
			viewState.setViewDescriptorCreator(creator);
		}
		viewState.addAll(parseTransitions(element));
		viewState.setProperties(parseProperties(element));
		// setup action support
		List setupElements = DomUtils.getChildElementsByTagName(element, SETUP_ELEMENT);
		if (!setupElements.isEmpty()) {
			Element setupElement = (Element)setupElements.get(0);
			viewState.setSetupCriteria(new ActionTransitionCriteria(parseAnnotatedAction(setupElement)));
			if (setupElement.hasAttribute(ON_ERROR_ATTRIBUTE)) {
				viewState.setSetupErrorStateId(setupElement.getAttribute(ON_ERROR_ATTRIBUTE));
			}
		}
	}

	/**
	 * Parse given decision state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddDecisionState(Flow flow, Element element) {
		DecisionState decisionState = (DecisionState)parseStateDefinition(element, DecisionState.class, new DecisionState());
		decisionState.setId(element.getAttribute(ID_ATTRIBUTE));
		decisionState.setFlow(flow);
		decisionState.addAll(parseIfs(element));
		decisionState.setProperties(parseProperties(element));
	}

	/**
	 * Parse given sub flow state definition and add a corresponding state to
	 * given flow.
	 */
	protected void parseAndAddSubFlowState(Flow flow, Element element) {
		SubflowState subflowState = (SubflowState)parseStateDefinition(element, SubflowState.class, new SubflowState());
		subflowState.setId(element.getAttribute(ID_ATTRIBUTE));
		subflowState.setFlow(flow);
		subflowState.setSubflow(getFlowServiceLocator().getFlow(element.getAttribute(FLOW_ATTRIBUTE)));
		subflowState.setAttributeMapper(parseAttributeMapper(element));
		subflowState.addAll(parseTransitions(element));
		subflowState.setProperties(parseProperties(element));
	}

	/**
	 * Parse given end state definition and add a corresponding state to given
	 * flow.
	 */
	protected void parseAndAddEndState(Flow flow, Element element) {
		EndState endState = (EndState)parseStateDefinition(element, EndState.class, new EndState());
		endState.setId(element.getAttribute(ID_ATTRIBUTE));
		endState.setFlow(flow);
		if (element.hasAttribute(VIEW_ATTRIBUTE)) {
			ViewDescriptorCreator creator = (ViewDescriptorCreator)
				converterFor(ViewDescriptorCreator.class).execute(element.getAttribute(VIEW_ATTRIBUTE));
			endState.setViewDescriptorCreator(creator);
		}
		endState.setProperties(parseProperties(element));
	}

	/**
	 * Parse all annotated action definitions contained in given element.
	 */
	protected AnnotatedAction[] parseAnnotatedActions(Element element) {
		List actions = new LinkedList();
		List actionElements = DomUtils.getChildElementsByTagName(element, ACTION_ELEMENT);
		Iterator it = actionElements.iterator();
		while (it.hasNext()) {
			actions.add(parseAnnotatedAction((Element)it.next()));
		}
		return (AnnotatedAction[])actions.toArray(new AnnotatedAction[actions.size()]);
	}

	/**
	 * Parse an annotated action definition and return the corresponding object.
	 */
	protected AnnotatedAction parseAnnotatedAction(Element element) {
		AnnotatedAction action = new AnnotatedAction((Action)parseAction(element));
		if (element.hasAttribute(NAME_ATTRIBUTE)) {
			action.setName(element.getAttribute(NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(METHOD_ATTRIBUTE)) {
			// direct support for multi-actions
			action.setProperty(MultiAction.DefaultActionExecuteMethodNameResolver.METHOD_PROPERTY,
					element.getAttribute(METHOD_ATTRIBUTE));
		}
		parseAndAddProperties(element, action);
		return action;
	}

	/**
	 * Parse an action definition and return the corresponding object.
	 */
	protected Action parseAction(Element element) throws FlowBuilderException {
		FlowArtifact actionDef = parseFlowArtifactDefinition(element);
		if (actionDef.isBeanRef()) {
			return getFlowServiceLocator().getAction(actionDef.bean);
		}
		else if (actionDef.isClassRef()) {
			return getFlowServiceLocator().getAction(actionDef.classRef);
		}
		else if (actionDef.shouldCreate()) {
			return getFlowServiceLocator().createAction(actionDef.clazz, actionDef.autowire);
		}
		throw new FlowBuilderException(this, "Illegal action definition: '" + element + "'");
	}
	
	/**
	 * Parse all properties defined as nested elements of given element.
	 */
	protected Map parseProperties(Element element) {
		MapAttributeSource properties = new MapAttributeSource();
		parseAndAddProperties(element, properties);
		return properties.getAttributeMap();
	}

	/**
	 * Parse all properties defined as nested elements of given element
	 * and add them to given set of properties.
	 */
	protected void parseAndAddProperties(Element element, MutableAttributeSource properties) {
		List propertyElements = DomUtils.getChildElementsByTagName(element, PROPERTY_ELEMENT);
		for (int i = 0; i < propertyElements.size(); i++) {
			parseAndAddProperty((Element)propertyElements.get(i), properties);
		}
	}

	/**
	 * Parse a property definition from given element and add the property
	 * to given set.
	 */
	protected void parseAndAddProperty(Element element, MutableAttributeSource properties) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		Object value = null;
		if (element.hasAttribute(VALUE_ATTRIBUTE)) {
			value = element.getAttribute(VALUE_ATTRIBUTE);
		}
		else {
			List valueElements = DomUtils.getChildElementsByTagName(element, VALUE_ELEMENT);
			Assert.state(valueElements.size() == 1, "A property value should be specified for property '" + name + "'");
			value = DomUtils.getTextValue((Element)valueElements.get(0));
		}
		properties.setAttribute(name, convertPropertyValue(element, value));
	}
	
	/**
	 * Do type conversion for given property value.
	 */
	protected Object convertPropertyValue(Element element, Object value) {
		if (element.hasAttribute(TYPE_ATTRIBUTE)) {
			// do value type conversion
			Class targetClass = getConversionService().withAlias(element.getAttribute(TYPE_ATTRIBUTE));
			return converterFor(targetClass).execute(value);
		}
		else {
			return value;
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
		FlowArtifact transitionDef = parseFlowArtifactDefinition(element);
		Transition transition;
		if (transitionDef.isBeanRef()) {
			transition = getFlowServiceLocator().getTransition(transitionDef.bean);
		}
		else if (transitionDef.isClassRef()) {
			transition = getFlowServiceLocator().getTransition(transitionDef.classRef);
		}
		else if (transitionDef.shouldCreate()) {
			transition = getFlowServiceLocator().createTransition(transitionDef.clazz, transitionDef.autowire);
		}
		else {
			transition = new Transition();
		}
		TransitionCriteria matchingCriteria = (TransitionCriteria)
			converterFor(TransitionCriteria.class).execute(element.getAttribute(ON_ATTRIBUTE));
		transition.setMatchingCriteria(matchingCriteria);
		transition.setTargetStateId(element.getAttribute(TO_ATTRIBUTE));
		TransitionCriteriaChain executionCriteria = new TransitionCriteriaChain();
		AnnotatedAction[] actions = parseAnnotatedActions(element);
		for (int i = 0; i < actions.length; i++) {
			executionCriteria.add(new ActionTransitionCriteria(actions[i]));
		}
		transition.setExecutionCriteria(executionCriteria);
		transition.setProperties(parseProperties(element));
		return transition;
	}

	/**
	 * Find all if definitions in given state definition and return a
	 * list of corresponding Transition objects.
	 */
	protected Transition[] parseIfs(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, IF_ELEMENT);
		Iterator it = transitionElements.iterator();
		while (it.hasNext()) {
			transitions.addAll(Arrays.asList(parseIf((Element)it.next())));
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Parse an "if" transition definition and return a corresponding Transition
	 * object.
	 */
	protected Transition[] parseIf(Element element) {
		FlowArtifact ifDef = parseFlowArtifactDefinition(element);
		TransitionCriteria criteria = (TransitionCriteria)
			converterFor(TransitionCriteria.class).execute(element.getAttribute(TEST_ATTRIBUTE));
		String trueStateId = element.getAttribute(THEN_ATTRIBUTE);
		Transition thenTransition = new Transition(criteria, trueStateId);
		String falseStateId = element.getAttribute(ELSE_ATTRIBUTE);
		if (StringUtils.hasText(falseStateId)) {
			Transition elseTransition = new Transition(TransitionCriteriaFactory.any(), falseStateId);
			return new Transition[] { thenTransition, elseTransition };
		}
		else {
			return new Transition[] { thenTransition };
		}
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
			FlowArtifact mapperDef = parseFlowArtifactDefinition(attributeMapperElement);
			if (mapperDef.isBeanRef()) {
				return getFlowServiceLocator().getFlowAttributeMapper(mapperDef.bean);
			}
			else if (mapperDef.isClassRef()) {
				return getFlowServiceLocator().getFlowAttributeMapper(mapperDef.classRef);
			}
			else if (mapperDef.shouldCreate()) {
				return getFlowServiceLocator().createFlowAttributeMapper(mapperDef.clazz, mapperDef.autowire);
			}
			else {
				// inline definition of an mappings
				ParameterizableFlowAttributeMapper attributeMapper = new ParameterizableFlowAttributeMapper();
				List inputElements = DomUtils.getChildElementsByTagName(attributeMapperElement, INPUT_ELEMENT);
				if (inputElements.size() > 0) {
					List inputMappings = new ArrayList(inputElements.size());
					Iterator it = inputElements.iterator();
					while (it.hasNext()) {
						inputMappings.add(parseMapping((Element)it.next()));
					}
					attributeMapper.setInputMappings(inputMappings);
				}
				List outputElements = DomUtils.getChildElementsByTagName(attributeMapperElement, OUTPUT_ELEMENT);
				if (outputElements.size() > 0) {
					List outputMappings = new ArrayList(outputElements.size());
					Iterator it = outputElements.iterator();
					while (it.hasNext()) {
						outputMappings.add(parseMapping((Element)it.next()));
					}
					attributeMapper.setOutputMappings(outputMappings);
				}
				return attributeMapper;
			}
		}
	}

	/**
	 * Parse a single inline attribute mapping definition.
	 */
	protected Mapping parseMapping(Element element) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String value = element.getAttribute(VALUE_ATTRIBUTE);
		String as = element.getAttribute(AS_ATTRIBUTE);
		if (StringUtils.hasText(name)) {
			if (StringUtils.hasText(as)) {
				return new Mapping(name, as);
			}
			else {
				return new Mapping(name);
			}
		}
		else if (StringUtils.hasText(value)) {
			Assert.hasText(as, "The 'as' attribute is required with the 'value' attribute");
			return new Mapping(value, as);
		}
		else {
			throw new FlowBuilderException(this, "Name or value is required in a mapping definition");
		}
	}
}