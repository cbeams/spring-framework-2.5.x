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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.NoSuchFlowDefinitionException;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.ViewState;
import org.springframework.web.flow.action.MultiAction;

/**
 * Base class for flow builders that programmatically build flows in Java
 * configuration code.
 * <p>
 * To give you an example of what a simple Java-based web flow builder
 * definition might look like, the following example defines the 'dynamic' web
 * flow roughly equivalent to the work flow statically implemented in Spring
 * MVC's simple form controller:
 * 
 * <pre>
 * 
 *  public class CustomerDetailFlowBuilder extends AbstractFlowBuilder {
 *  
 *  	protected String flowId() {
 *  		return &quot;customer.Detail&quot;;
 *  	}
 *  
 *   public void buildStates() {
 *      // get customer information
 *    	addActionState(&quot;getDetails&quot;,
 *                      action(GetCustomerAction.class, AutowireMode.BY_TYPE),
 *                      on(success(), &quot;viewDetails&quot;));
 *      // view customer information               
 *    	addViewState(&quot;viewDetails&quot;, &quot;customer.Detail.View&quot;,
 *                    on(submit(), &quot;bindAndValidate&quot;);
 *      // bind and validate customer information updates 
 *    	addActionState(&quot;bindAndValidate&quot;,
 *                      action(&quot;customer.Detail.bindAndValidate&quot;),
 *                      new Transition[] {
 *                          on(error(), &quot;viewDetails&quot;),
 *                          on(success(), &quot;finish&quot;)
 *                      }
 *      // finish
 *    	addEndState(&quot;finish&quot;);
 *   }
 *  
 *  
 * </pre>
 * 
 * What this Java-based FlowBuilder implementation does is add four states to a
 * flow identified as "customerDetails". These include a "get"
 * <code>ActionState</code> (the start state), a <code>ViewState</code>
 * state, a "bind and validate" <code>ActionState</code>, and an end marker
 * state (<code>EndState</code>).
 * 
 * The first state, an action state, will be assigned the indentifier
 * <code>getDetails</code>. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>An auto-wired action instance of GetCustomerDetails.class. This is he
 * <code>Action</code> implementation that will execute when this state is
 * entered. In this example, that <code>Action</code> will go out to the DB,
 * load the Customer, and put it in the Flow's request context.
 * <li>A <code>success</code> transition to a default view state, called
 * <ocde>viewDetails</code> This means when the get <code>Action </code>
 * returns a <code>success</code> result event (aka outcome), the <code>viewDetails</code>
 * state will be entered.
 * <li>It will act as the start state for this flow (by default, the first
 * state added to a flow during the build process is treated as the start
 * state.)
 * </ol>
 * 
 * The second state, a view state, will be identified as <code> viewDetails</code>
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called <code>customer.Detail.view</code>-- this is the
 * logical name of a view resource. This logical view name gets mapped to a
 * physical view resource (jsp, etc.) by the calling front controller (via a
 * spring view resolver, or a struts action forward, for example.)
 * <li>A <code>submit</code> transition to a bind and validate action state,
 * indentified by the default ID <code>"bindAndValidate"</code>. This means
 * when a <code>submit</code> event is signaled by the view (for example, on a
 * submit button click), the bindAndValidate action state will be entered and
 * the <code>customerDetails.bindAndValidate</code> <code>Action </code>
 * implementation will be executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as <code>
 * bindAndValidate</code>. This action state will automatically be configured
 * with the following defaults:
 * <ol>
 * <li>An action bean named <code>bindAndValidate</code>- this is the name
 * of the <code>Action</code> implementation exported in the application
 * context that will execute when this state is entered. In this example, the
 * <code>Action</code> will bind form input in the HTTP request to a backing
 * Customer form object, validate it, and update the DB.
 * <li>A <code>success</code> transition to a default end state, called
 * <code>finish</code>. This means if the <code>Action</code> returns a
 * <code>success</code> result, the <code>finish</code> end state will be
 * transitioned to and the flow will terminate.
 * <li>A <code>error</code> transition back to the form view. This means if
 * the <code>Action</code> returns a <code>error</code> event, the <code>
 * viewDetails</code> view state will be transitioned back to.
 * </ol>
 * 
 * The fourth and last state, an end state, will be indentified with the default
 * end state ID <code>finish</code>. This end state is a marker that signals
 * the end of the flow. When entered, the flow session terminates, and if this
 * flow is acting as a root flow in the current flow execution, any
 * flow-allocated resources will be cleaned up. An end state can optionally be
 * configured with a logical view name to forward to when entered. It will also
 * trigger a state transition in a resuming parent flow if this flow was
 * participating as a spawned 'sub flow' within a suspended parent flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowBuilder extends BaseFlowBuilder {

	/**
	 * The default attribute mapper ID suffix ("attributeMapper").
	 */
	public static final String ATTRIBUTE_MAPPER_ID_SUFFIX = "attributeMapper";

	/**
	 * Create an instance of a abstract flow builder; default constructor.
	 */
	protected AbstractFlowBuilder() {
		super();
	}

	/**
	 * Create an instance of an abstract flow builder, using the specified
	 * service locator to obtain needed flow services during configuation.
	 * @param flowServiceLocator The service locator.
	 */
	protected AbstractFlowBuilder(FlowServiceLocator flowServiceLocator) {
		super(flowServiceLocator);
	}

	/**
	 * Create an instance of an abstract flow builder, using the specified
	 * service locator and flow creator strategy.
	 * @param flowServiceLocator The service locator
	 * @param flowCreator The flow creation strategy
	 */
	protected AbstractFlowBuilder(FlowServiceLocator flowServiceLocator, FlowCreator flowCreator) {
		super(flowServiceLocator, flowCreator);
	}

	public final Flow init() throws FlowBuilderException {
		setFlow(createFlow(flowId()));
		return getFlow();
	}

	/**
	 * Returns the id (name) of the flow built by this builder. Subclasses
	 * should override to return the unique flowId.
	 * @return The unique flow id.
	 */
	protected abstract String flowId();

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateId The <code>ViewState</code> id - must be locally unique
	 *        to the flow built by this builder.
	 * @param viewName The name of the logical view name to render. This name
	 *        will be mapped to a physical resource template such as a JSP when
	 *        the ViewState is entered and control returns to the front
	 *        controller.
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateId, String viewName, Transition transition)
			throws IllegalArgumentException {
		return new ViewState(getFlow(), stateId, viewName, transition);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateId The <code>ViewState</code> id; must be unique in the
	 *        context of the flow built by this builder
	 * @param viewName The name of the logical view name to render; this name
	 *        will be mapped to a physical resource template such as a JSP when
	 *        the ViewState is entered and control returns to the front
	 *        controller.
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateId, String viewName, Transition[] transitions)
			throws IllegalArgumentException {
		return new ViewState(getFlow(), stateId, viewName, transitions);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A marker has a <code>null</code> <code>viewName</code> and assumes
	 * the HTTP response has already been written when entered. The marker notes
	 * that control should be returned to the HTTP client.
	 * @param stateId The <code>ViewState</code> id; must be unique in the
	 *        context of the flow built by this builder
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateId, Transition transition) throws IllegalArgumentException {
		return addViewState(stateId, (String)null, transition);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A view marker has a <code>null</code> <code>viewName</code> and
	 * assumes the HTTP response has already been written when entered. The
	 * marker notes that control should be returned to the HTTP client.
	 * <p>
	 * @param stateId The <code>ViewState</code> id; must be unique in the
	 *        context of the flow built by this builder
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateId, Transition[] transitions) throws IllegalArgumentException {
		return addViewState(stateId, (String)null, transitions);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param action the action implementation
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action action, Transition transition)
			throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, action, transition);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param action the action implementation
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action action, Transition[] transitions)
			throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, action, transitions);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param actionName a logical name to associate with this action, used to
	 *        qualify action results (e.g "myAction.success"), so one action can
	 *        be reused in different flows with other actions that return the
	 *        same logical result
	 * @param action the action implementation
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, String actionName, Action action, Transition transition)
			throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, actionName, action, transition);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param actionName a logical name to associate with this action, used to
	 *        qualify action results (e.g "myAction.success"), so one action can
	 *        be reused in different flows with other actions that return the
	 *        same logical result
	 * @param action the action implementation
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, String actionName, Action action, Transition[] transitions)
			throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, actionName, action, transitions);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes one or more <code>Action</code> implementations
	 * when entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param actions the action implementations, to be executed in order until
	 *        a valid transitional result is returned (Chain of Responsibility)
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, Action[] actions, Transition[] transitions)
			throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, actions, transitions);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes one or more <code>Action</code> implementations
	 * when entered.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param actionNames the logical names to associate with each action, used
	 *        to qualify action results (e.g "myAction.success"), so one action
	 *        can be reused in different flows with other actions that return
	 *        the same logical result
	 * @param actions the action implementations, to be executed in order until
	 *        a valid transitional result is returned (Chain of Responsibility)
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 */
	protected ActionState addActionState(String stateId, String[] actionNames, Action[] actions,
			Transition[] transitions) throws IllegalArgumentException {
		return new ActionState(getFlow(), stateId, actionNames, actions, transitions);
	}

	/**
	 * Request that the action with the specified id be executed when the action
	 * state being built is entered. Simply looks the action up by name and
	 * returns it.
	 * @param actionId The action id
	 * @return The action
	 * @throws NoSuchActionException the action could not be resolved.
	 */
	protected Action action(String actionId) throws NoSuchActionException {
		return getFlowServiceLocator().getAction(actionId);
	}

	/**
	 * Request that the multi-action with the specified id be executed when the
	 * action state being built is entered. Simply looks the multi-action up by
	 * name and returns it.
	 * @param actionId The multi action id
	 * @return The multi action
	 * @throws NoSuchActionException the action could not be resolved.
	 */
	protected MultiAction multiAction(String actionId) throws NoSuchActionException {
		Action action = getFlowServiceLocator().getAction(actionId);
		Assert.isInstanceOf(MultiAction.class, action, "Multi-action lookup with id '" + actionId + "' failed:");
		return (MultiAction)action;
	}

	/**
	 * Request that the actions with the specified ids be executed in the order
	 * specified when the action state being built is entered. Simply looks the
	 * actions up by id and returns them.
	 * @param actionIds The action ids
	 * @return The actions
	 * @throws NoSuchActionException at least one action could not be resolved.
	 */
	protected Action[] actions(String[] actionIds) throws NoSuchActionException {
		Action[] actions = new Action[actionIds.length];
		for (int i = 0; i < actionIds.length; i++) {
			actions[i] = getFlowServiceLocator().getAction(actionIds[i]);
		}
		return actions;
	}

	/**
	 * Request that the actions with the specified implementation be executed
	 * when the action state being built is entered. Looks the action up by
	 * implementation class and returns it.
	 * @param actionImplementationClass The action implementation--there must be
	 *        only one action impl of this type defined in the registry
	 * @return The actions The action
	 * @throws NoSuchActionException The action could not be resolved.
	 */
	protected Action actionRef(Class actionImplementationClass) throws NoSuchActionException {
		return getFlowServiceLocator().getAction(actionImplementationClass);
	}

	/**
	 * Request that the actions with the specified implementations be executed
	 * in the order specified when the action state being built is entered.
	 * Looks the actions up by implementation class and returns it.
	 * @param actionImplementationClasses The action implementations--there must
	 *        be only one action impl per type defined in the registry
	 * @return The actions The actions
	 * @throws NoSuchActionException One or more of the actions could not be
	 *         resolved
	 */
	protected Action[] actionRefs(Class[] actionImplementationClasses) throws NoSuchActionException {
		Action[] actions = new Action[actionImplementationClasses.length];
		for (int i = 0; i < actionImplementationClasses.length; i++) {
			actions[i] = getFlowServiceLocator().getAction(actionImplementationClasses[i]);
		}
		return actions;
	}

	/**
	 * Request that the action with the specified implementation be instantiated
	 * and executed when the action state being built is entered. Creates the
	 * action instance.
	 * @param actionImplementationClass The action implementation to instantiate
	 * @return The action
	 */
	protected Action action(Class actionImplementationClass) {
		return getFlowServiceLocator().createAction(actionImplementationClass, AutowireMode.DEFAULT);
	}

	/**
	 * Request that the actions with the specified implementations be
	 * instantiated and executed when the action state being built is entered.
	 * Creates the action instances.
	 * @param actionImplementationClasses The action implementations to
	 *        instantiate
	 * @return The actions
	 */
	protected Action[] actions(Class[] actionImplementationClasses) {
		Action[] actions = new Action[actionImplementationClasses.length];
		for (int i = 0; i < actionImplementationClasses.length; i++) {
			actions[i] = getFlowServiceLocator().createAction(actionImplementationClasses[i], AutowireMode.DEFAULT);
		}
		return actions;
	}

	/**
	 * Request that the action with the specified implementation be instantiated
	 * and executed when the action state being built is entered. Creates the
	 * action instance.
	 * @param actionImplementationClass The action implementation to instantiate
	 * @param autowireMode the instance autowiring strategy
	 * @return The action
	 */
	protected Action action(Class actionImplementationClass, AutowireMode autowireMode) {
		return getFlowServiceLocator().createAction(actionImplementationClass, autowireMode);
	}

	/**
	 * Request that the actions with the specified implementations be
	 * instantiated and executed when the action state being built is entered.
	 * Creates the action instances.
	 * @param actionImplementationClasses The action implementations to
	 *        instantiate
	 * @param autowireMode the instance autowiring strategy
	 * @return The actions
	 */
	protected Action[] actions(Class[] actionImplementationClasses, AutowireMode autowireMode) {
		Action[] actions = new Action[actionImplementationClasses.length];
		for (int i = 0; i < actionImplementationClasses.length; i++) {
			actions[i] = getFlowServiceLocator().createAction(actionImplementationClasses[i], autowireMode);
		}
		return actions;
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder
	 * @param subFlow the flow to be used as a subflow
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, Flow subFlow, Transition[] transitions) {
		new SubFlowState(getFlow(), id, subFlow, transitions);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id
	 * @param subFlow The flow definition to be used as the subflow
	 * @param attributeMapper The attribute mapper to map attributes between the
	 *        flow built by this builder and the subflow
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, Flow subFlow, FlowAttributeMapper attributeMapper,
			Transition[] transitions) {
		new SubFlowState(getFlow(), id, subFlow, attributeMapper, transitions);
	}

	/**
	 * Request that the attribute mapper with the specified name prefix be used
	 * to map attributes between a parent flow and a spawning subflow when the
	 * subflow state being constructed is entered.
	 * @param attributeMapperId The id prefix of the attribute mapper that will
	 *        map attributes between the the flow built by this builder and the
	 *        subflow
	 * @return The attribute mapper
	 * @throws NoSuchFlowAttributeMapperException no FlowAttributeMapper
	 *         implementation was exported with the specified id.
	 */
	protected FlowAttributeMapper attributeMapper(String attributeMapperId) throws NoSuchFlowAttributeMapperException {
		if (!StringUtils.hasText(attributeMapperId)) {
			return null;
		}
		return getFlowServiceLocator().getFlowAttributeMapper(attributeMapperId);
	}

	/**
	 * Request that the mapper of the specified implementation be used to map
	 * attributes between a parent flow and a spawning subflow when the subflow
	 * state being built is entered.
	 * @param flowAttributeMapperImplementationClass The attribute mapper
	 *        implementation, there must be only one instance in the registry.
	 * @return The attribute mapper
	 * @throws NoSuchFlowAttributeMapperException no FlowAttributeMapper
	 *         implementation was exported with the specified implementation, or
	 *         more than one existed.
	 */
	protected FlowAttributeMapper attributeMapperRef(Class flowAttributeMapperImplementationClass)
			throws NoSuchFlowAttributeMapperException {
		return getFlowServiceLocator().getFlowAttributeMapper(flowAttributeMapperImplementationClass);
	}

	/**
	 * Request that the flow attribute mapper with the specified implementation
	 * be instantiated, to be used to map attributs when a subflow is spawned in
	 * a subflow state. Creates the mapper instance.
	 * @param attributeMapperImplementationClass The attribute mapper
	 *        implementation to instantiate
	 * @return The attribute mapper
	 */
	protected FlowAttributeMapper attributeMapper(Class attributeMapperImplementationClass) {
		return getFlowServiceLocator().createFlowAttributeMapper(attributeMapperImplementationClass,
				AutowireMode.DEFAULT);
	}

	/**
	 * Request that the flow attribute mapper with the specified implementation
	 * be instantiated, to be used to map attributs when a subflow is spawned in
	 * a subflow state. Creates the mapper instance.
	 * @param attributeMapperImplementationClass The action implementation to
	 *        instantiate
	 * @param autowireMode the instance autowiring strategy
	 * @return The action The action
	 */
	protected FlowAttributeMapper attributeMapper(Class attributeMapperImplementationClass, AutowireMode autowireMode) {
		return getFlowServiceLocator().createFlowAttributeMapper(attributeMapperImplementationClass, autowireMode);
	}

	/**
	 * Appends the identifying 'attributeMapper' suffix to the specified prefix
	 * qualifier, returning a fully-qualified attribute mapper service
	 * identifier. For example: <code>attributeMapper("customerId")</code>
	 * results in <code>customerId.attributeMapper</code>.
	 * @param attributeMapperIdPrefix The attribute mapper ID qualifier
	 * @return The qualified attribute mapper id.
	 */
	protected String attributeMapperId(String attributeMapperIdPrefix) {
		Assert.notNull(attributeMapperIdPrefix, "The attribute mapper id prefix is required");
		if (!attributeMapperIdPrefix.endsWith(ATTRIBUTE_MAPPER_ID_SUFFIX)) {
			return attributeMapperIdPrefix + getQualifierDelimiter() + ATTRIBUTE_MAPPER_ID_SUFFIX;
		}
		else {
			return attributeMapperIdPrefix;
		}
	}

	/**
	 * Request that the <code>Flow</code> with the specified flowId be spawned
	 * as a subflow when the subflow state being built is entered. Simply
	 * resolves the subflow definition by id and returns it; throwing a
	 * fail-fast exception if it does not exist.
	 * @param flowId The flow definition id
	 * @return The flow to be used as a subflow, this should be passed to a
	 *         addSubFlowState call
	 */
	protected Flow flow(String flowId) throws NoSuchFlowDefinitionException {
		return getFlowServiceLocator().getFlow(flowId);
	}

	/**
	 * Request that the flow with the specified flowId and built by the
	 * specified flow builder implementation be spawned as a subflow when the
	 * subflow state being built is entered. Simply resolves the subflow
	 * definition by id, verifies it is built by the specified builder, and
	 * returns it; throwing a fail-fast exception if it does not exist or is
	 * build by the wrong builder.
	 * @param flowId The flow definition id
	 * @param flowBuilderImplementationClass Ther required FlowBuilder
	 *        implementation that must build the subflow.
	 * @return The flow to be used as a subflow, this should be passed to a
	 *         addSubFlowState call
	 */
	protected Flow flow(String flowId, Class flowBuilderImplementationClass) throws NoSuchFlowDefinitionException {
		return getFlowServiceLocator().getFlow(flowId, flowBuilderImplementationClass);
	}

	/**
	 * Adds an end state with the specified id that will display the specified
	 * view when entered as part of a terminating flow execution.
	 * @param endStateId The end state id
	 * @param viewName The view name
	 * @return The end state
	 */
	protected EndState addEndState(String endStateId, String viewName) throws IllegalArgumentException {
		return new EndState(getFlow(), endStateId, viewName);
	}

	/**
	 * Adds an end state with the specified id;.
	 * @param endStateId The end state id
	 * @return The end state
	 */
	protected EndState addEndState(String endStateId) throws IllegalArgumentException {
		return new EndState(getFlow(), endStateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of an event that matches the criteria defined by
	 * ${criteria}, transition to state ${stateId}.
	 * </ul>
	 * @param criteria The transition criteria
	 * @param stateId the state Id
	 * @return the transition (event matching criteria->stateId)
	 */
	protected Transition on(TransitionCriteria criteria, String stateId) {
		return new Transition(criteria, stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of event ${eventId}, transition to state
	 * ${stateId}.
	 * </ul>
	 * @param eventId The event id
	 * @param stateId the state Id
	 * @return the transition (eventId->stateId)
	 */
	protected Transition on(String eventId, String stateId) {
		return new Transition(eventId, stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of event ${actionName}.${eventId}, transition to
	 * state ${stateId}.
	 * </ul>
	 * @param actionName the action name qualifier
	 * @param eventId The event id
	 * @param stateId the state Id
	 * @return the transition
	 */
	protected Transition on(String actionName, String eventId, String stateId) {
		return new Transition(join(actionName, eventId), stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of any event (*), transition to state ${stateId}.
	 * </ul>
	 * @param stateId the state Id
	 * @return the transition (*->stateId)
	 */
	protected Transition onAnyEvent(String stateId) {
		return new Transition(Transition.WILDCARD_TRANSITION_CRITERIA, stateId);
	}

	/**
	 * Creates the <code>success</code> event id. "Success" indicates that an
	 * action completed successfuly.
	 * @return the event id.
	 */
	protected String success() {
		return eventId("success");
	}

	/**
	 * Creates the <code>error</code> event id. "Error" indicates that an
	 * action completed with an error status.
	 * @return the event id.
	 */
	protected String error() {
		return eventId("error");
	}

	/**
	 * Creates the <code>submit</code> event id. "Submit" indicates the user
	 * wants submitted a request (form) for processing.
	 * @return the event id.
	 */
	protected String submit() {
		return eventId("submit");
	}

	/**
	 * Creates the <code>back</code> event id. "Back" indicates the user wants
	 * to go to the previous step in the flow.
	 * @return the event id.
	 */
	protected String back() {
		return eventId("back");
	}

	/**
	 * Creates the <code>cancel</code> event id. "Cancel" indicates the flow
	 * was aborted because the user changed their mind.
	 * @return the event id.
	 */
	protected String cancel() {
		return eventId("cancel");
	}

	/**
	 * Creates the <code>finish</code> event id. "Finish" indicates an object
	 * was selected for processing or display.
	 * @return the event id.
	 */
	protected String finish() {
		return eventId("finish");
	}

	/**
	 * Creates the <code>select</code> event id. "Select" indicates an object
	 * was selected for processing or display.
	 * @return the event id.
	 */
	protected String select() {
		return eventId("select");
	}

	/**
	 * Creates the <code>edit</code> event id. "Edit" indicates an object was
	 * selected for creation or updating.
	 * @return the event id.
	 */
	protected String edit() {
		return eventId("edit");
	}

	/**
	 * Creates the <code>delete</code> event id. "Add" indicates a child
	 * object is being added to a parent collection.
	 * @return the event id.
	 */
	protected String add() {
		return eventId("add");
	}

	/**
	 * Creates the <code>delete</code> event id. "Delete" indicates a object
	 * is being removed.
	 * @return the event id.
	 */
	protected String delete() {
		return eventId("delete");
	}

	/**
	 * Factory method for producing a event id given a string key identifier.
	 * Default implementation does nothing, returning the key as the
	 * <code>eventId</code>. Subclasses may override.
	 * @param key The event id key
	 * @return the event id
	 */
	protected String eventId(String key) {
		return key;
	}

	/**
	 * Join given prefix and suffix into a single string separated by a
	 * delimiter.
	 * @param prefix the prefix
	 * @param suffix the suffix
	 * @return the qualified string
	 */
	protected String join(String prefix, String suffix) {
		return prefix + getQualifierDelimiter() + suffix;
	}

	/**
	 * Returns the delimitor used to seperate identifier parts. E.g. flow id and
	 * state id ("customerDetails.get"). Detaults to a dot (".").
	 */
	protected String getQualifierDelimiter() {
		return SEPARATOR;
	}
}