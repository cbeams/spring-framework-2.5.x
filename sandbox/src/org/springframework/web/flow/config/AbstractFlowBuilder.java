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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowModelMapper;
import org.springframework.web.flow.NoSuchFlowDefinitionException;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.ViewState;

/**
 * Base class for flow builders that programmatically build flows in Java
 * configuration code.
 * <p>
 * To give you an example of what a simple java-based web flow builder
 * definition might look like, the following example defines the 'dynamic' web
 * flow equivalent to the work flow statically implemented in Spring MVC's
 * simple form controller:
 * 
 * <pre>
 * public class EditPersonDetailsFlowBuilder extends AbstractFlowBuilder {
 * 	public static final String PERSON_DETAILS = &quot;personDetails&quot;;
 * 
 * 	protected String flowId() {
 * 		return PERSON_DETAILS;
 * 	}
 * 
 * 	public void buildStates() {
 * 		addGetState(PERSON_DETAILS);
 * 		addViewState(PERSON_DETAILS);
 * 		addBindAndValidateState(PERSON_DETAILS);
 * 		addFinishEndState();
 * 	}
 * }
 * </pre>
 * 
 * What this java-based FlowBuilder implementation does is add four states to a
 * flow identified as "personDetails". These include a "get"
 * <code>ActionState</code> (the start state), a <code>ViewState</code>
 * state, a "bind and validate" <code>ActionState</code>, and an end marker
 * state (<code>EndState</code>).
 * 
 * The first state, an action state, will be assigned the indentifier
 * <code>personDetails.get</code>. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>The action bean identifier <code>personDetails.get</code>; this is
 * the name of the <code>Action</code> implementation that will execute when
 * this state is entered. In this example, that <code>Action</code> will go
 * out to the DB, load the Person, and put it in the Flow's data model.
 * <li>A <code>success</code> transition to a default view state, called
 * <ocde>personDetails.view'</code> This means when the get <code>Action
 * </code> returns a <code>success</code> result event (aka outcome), the
 * <code>personDetails.view</code> state will be entered.
 * <li>It will act as the start state for this flow (by default, the first
 * state added to a flow during the build process is treated as the start
 * state.)
 * </ol>
 * 
 * The second state, a view state, will be identified as <code>
 * personDetails.view</code> This view state will automatically be configured
 * with the following defaults:
 * <ol>
 * <li>A view name called <code>personDetails.view</code> --this is the
 * logical name of a view resource. This logical view name gets mapped to a
 * physical view resource (jsp, etc.) by the calling front controller (via a
 * spring view resolver, or a struts action forward, for example.)
 * <li>A <code>submit</code> transition to a bind and validate action state,
 * indentified by the default ID <code>personDetails.bindAndValidate</code>.
 * This means when a <code>submit</code> event is signaled by the view (for
 * example, on a submit button click), the bindAndValidate action state will be
 * entered and the <code>personDetails.bindAndValidate</code> <code>Action
 * </code> implementation will be executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as <code>
 * personDetails.bindAndValidate</code>. This action state will automatically
 * be configured with the following defaults:
 * <ol>
 * <li>A action bean named <code>personDetails.bindAndValidate</code>- this
 * is the name of the <code>Action</code> implementation exported in the
 * application context that will execute when this state is entered. In this
 * example, the <code>Action</code> will bind form input in the HTTP request
 * to a backing Person form object, validate it, and update the DB.
 * <li>A <code>success</code> transition to a default end state, called
 * <code>finish</code>. This means if the <code>Action</code> returns a
 * <code>success</code> result, the <code>finish</code> end state will be
 * transitioned to and the flow will terminate.
 * <li>A <code>error</code> transition back to the form view. This means if
 * the <code>Action</code> returns a <code>error</code> event, the <code>
 * personDetails.view</code> view state will be transitioned back to.
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
	 * The default model mapper ID suffix ("modelMapper").
	 */
	public static final String MODEL_MAPPER_ID_SUFFIX = "modelMapper";

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
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID. By default, the subflow state ID will also be treated as the id of
	 * the subflow definition to spawn, to be used for retrieval by the
	 * FlowServiceLocator.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. This id also acts as the id of the subFlow.
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, Transition[] transitions) {
		addSubFlowState(id, spawnFlow(subFlowId(id)), transitions);
	}

	/**
	 * Factory method that returns an sub flow identifier given a stateId as
	 * input. This default implementation simply returns the stateId; because as
	 * a default, the value of the subflow state id is also treated as the
	 * <code>id</code> of the <code>Flow</code> definition to lookup using
	 * the <code>FlowServiceLocator</code>.
	 * @param stateId The stateId
	 * @return the subflowId
	 */
	protected String subFlowId(String stateId) {
		return stateId;
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
	 * ID. The subflow state ID will also be treated as the id of the subflow
	 * definition, to be used for retrieval by the FlowServiceLocator.
	 * <p>
	 * The retrieved subflow definition must also be built by the specified
	 * FlowBuilder implementation, or an exception is thrown. This allows for
	 * easy navigation to subflow creation logic from within the parent flow
	 * definition, and validates that a particular build implementation does
	 * indeed produce the subflow.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. This id also acts as the id of the subFlow.
	 * @param flowBuilderImplementation The flow builder implementation
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 * @throws NoSuchFlowDefinitionException the subflow could not be resolved,
	 *         or it was not built by the specified builder implementation.
	 */
	protected void addSubFlowState(String id, Class flowBuilderImplementation, Transition[] transitions)
			throws IllegalArgumentException, NoSuchFlowDefinitionException {
		new SubFlowState(getFlow(), id, spawnFlow(id, flowBuilderImplementation), transitions);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID. By default, the subflow state ID will also be treated as the id of
	 * the subflow definition to spawn, to be used for retrieval by the
	 * FlowServiceLocator.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. This id also acts as the id of the subFlow.
	 * @param modelMapper The model mapper to map attributes between the flow
	 *        built by this builder and the subflow
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        subflow ends (this assumes you always transition to the same state
	 *        regardless of which EndState is reached in the subflow)
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, FlowModelMapper modelMapper, String subFlowDefaultFinishStateId)
			throws IllegalArgumentException {
		addSubFlowState(id, spawnFlow(subFlowId(id)), modelMapper,
				new Transition[] { onAnyEvent(subFlowDefaultFinishStateId) });
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id, must be unique among all states
	 * @param subFlow the Flow definition to be spawned as a subflow
	 * @param modelMapper The model mapper to map attributes between the flow
	 *        built by this builder and the subflow
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        subflow ends (this assumes you always transition to the same state
	 *        regardless of which EndState is reached in the subflow)
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, Flow subFlow, FlowModelMapper modelMapper,
			String subFlowDefaultFinishStateId) throws IllegalArgumentException {
		addSubFlowState(id, subFlow, modelMapper, new Transition[] { onAnyEvent(subFlowDefaultFinishStateId) });
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID. The subflow state ID will also be treated as the id of the subflow
	 * definition to spawn, to be used for retrieval by the FlowServiceLocator.
	 * <p>
	 * The retrieved subflow definition must be built by the specified
	 * FlowBuilder implementation or an exception is thrown. This allows for
	 * easy navigation to subflow creation logic from within the parent flow
	 * builder definition, and validates that a particular build implementation
	 * does indeed create the subflow.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. The state id will also act as the subflow id.
	 * @param subFlowBuilderImplementation The flow builder implementation
	 * @param modelMapper The model mapper to map attributes between the the
	 *        flow built by this builder and the subflow
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        subflow ends (this assumes you always transition to the same state
	 *        regardless of which EndState is reached in the subflow)
	 * @throws IllegalArgumentException the state id is not unique
	 * @throws NoSuchFlowDefinitionException the subflow could not be resolved,
	 *         or it was not built by the specified builder implementation.
	 */
	protected void addSubFlowState(String id, Class subFlowBuilderImplementation, FlowModelMapper modelMapper,
			String subFlowDefaultFinishStateId) {
		addSubFlowState(id, spawnFlow(id, subFlowBuilderImplementation), modelMapper,
				new Transition[] { onAnyEvent(subFlowDefaultFinishStateId) });
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id
	 * @param subFlow The flow definition to be used as the subflow
	 * @param modelMapper The model mapper to map attributes between the flow
	 *        built by this builder and the subflow
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 */
	protected void addSubFlowState(String id, Flow subFlow, FlowModelMapper modelMapper, Transition[] transitions) {
		new SubFlowState(getFlow(), id, subFlow, modelMapper, transitions);
	}

	/**
	 * Adds a subflow state to the flow built by this builder with the specified
	 * ID. The subflow state ID will also be treated as the id of the subflow
	 * definition to spawn, to be used for retrieval by the FlowServiceLocator.
	 * <p>
	 * The retrieved subflow definition must be built by the specified
	 * FlowBuilder implementation or an exception is thrown. This allows for
	 * easy navigation to subflow creation logic from within the parent flow
	 * builder definition, and validates that a particular build implementation
	 * does indeed create the subflow.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. The state id will also act as the subflow id.
	 * @param subFlowBuilderImplementation The flow builder implementation
	 * @param modelMapper The model mapper to map attributes between the the
	 *        flow built by this builder and the subflow
	 * @param transitions The eligible set of state transitions
	 * @throws IllegalArgumentException the state id is not unique
	 * @throws NoSuchFlowDefinitionException the subflow could not be resolved,
	 *         or was not produced by the appropriate builder implementation.
	 */
	protected void addSubFlowState(String id, Class subFlowBuilderImplementation, FlowModelMapper modelMapper,
			Transition[] transitions) {
		new SubFlowState(getFlow(), id, spawnFlow(id, subFlowBuilderImplementation), modelMapper, transitions);
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
	protected Flow spawnFlow(String flowId) throws NoSuchFlowDefinitionException {
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
	protected Flow spawnFlow(String flowId, Class flowBuilderImplementationClass) throws NoSuchFlowDefinitionException {
		return getFlowServiceLocator().getFlow(flowId, flowBuilderImplementationClass);
	}

	/**
	 * Request that the action with the specified id be executed when the action
	 * state being built is entered. Simply looks the action up by name and
	 * returns it.
	 * @param actionId The action id
	 * @return The action
	 * @throws NoSuchActionException the action could not be resolved.
	 */
	protected Action executeAction(String actionId) throws NoSuchActionException {
		return getFlowServiceLocator().getAction(actionId);
	}

	/**
	 * Request that the actions with the specified ids be executed in the order
	 * specified when the action state being built is entered. Simply looks the
	 * actions up by id and returns them.
	 * @param actionIds The action ids
	 * @return The actions
	 * @throws NoSuchActionException at least one action could not be resolved.
	 */
	protected Action[] executeActions(String[] actionIds) throws NoSuchActionException {
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
	protected Action executeAction(Class actionImplementationClass) {
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
	protected Action[] executeActions(Class[] actionImplementationClasses) throws NoSuchActionException {
		Action[] actions = new Action[actionImplementationClasses.length];
		for (int i = 0; i < actionImplementationClasses.length; i++) {
			actions[i] = getFlowServiceLocator().getAction(actionImplementationClasses[i]);
		}
		return actions;
	}

	/**
	 * Request that the model mapper with the specified name prefix be used to
	 * map attributes between a parent flow and a spawning subflow when the
	 * subflow state being constructed is entered.
	 * @param modelMapperIdPrefix The id prefix of the model mapper that will
	 *        map attributes between the the flow built by this builder and the
	 *        subflow; note, the id prefix will have the model mapper suffix
	 *        appended to produce the qualified service identifier (e.g
	 *        userId.modelMapper) See: {@link String#modelMapper(String)}
	 * @return The model mapper
	 * @throws NoSuchFlowModelMapperException no FlowModelMapper implementation
	 *         was exported with the specified id.
	 */
	protected FlowModelMapper useModelMapper(String modelMapperIdPrefix) throws NoSuchFlowModelMapperException {
		if (!StringUtils.hasText(modelMapperIdPrefix)) {
			return null;
		}
		return getFlowServiceLocator().getFlowModelMapper(modelMapper(modelMapperIdPrefix));
	}

	/**
	 * Request that the mapper of the specified implementation be used to map
	 * attributes between a parent flow and a spawning subflow when the subflow
	 * state being built is entered.
	 * @param flowModelMapperImplementationClass The model mapper
	 *        implementation, there must be only one instance in the registry.
	 * @return The model mapper
	 * @throws NoSuchFlowModelMapperException no FlowModelMapper implementation
	 *         was exported with the specified implementation, or more than one
	 *         existed.
	 */
	protected FlowModelMapper useModelMapper(Class flowModelMapperImplementationClass)
			throws NoSuchFlowModelMapperException {
		return getFlowServiceLocator().getFlowModelMapper(flowModelMapperImplementationClass);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A view marker has a <code>null</code> <code>viewName</code> and
	 * assumes the HTTP response has already been written when entered. The
	 * marker notes that control should be returned to the HTTP client.
	 * <p>
	 * The view state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event 'cancel', transition to the 'cancel' state
	 * <li>on event 'back', transition to the 'back' state
	 * <li>on event 'submit', transition to the
	 * '${stateIdPrefix}.bindAndValidate' state
	 * </ul>
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, (String)null);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A marker has a <code>null</code> <code>viewName</code> and assumes
	 * the HTTP response has already been written when entered. The marker notes
	 * that control should be returned to the HTTP client.
	 * <p>
	 * The view state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event 'cancel', transition to the 'cancel' state
	 * <li>on event 'back', transition to the 'back' state
	 * <li>on event 'submit', transition to the
	 * '${stateIdPrefix}.bindAndValidate' state
	 * </ul>
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g.
	 *        personDetails.view)
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix, Transition transition) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, (String)null, transition);
	}

	/**
	 * Adds a <code>ViewState</code> marker to the flow built by this builder.
	 * <p>
	 * A view marker has a <code>null</code> <code>viewName</code> and
	 * assumes the HTTP response has already been written when entered. The
	 * marker notes that control should be returned to the HTTP client.
	 * <p>
	 * The view state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event 'cancel', transition to the 'cancel' state
	 * <li>on event 'back', transition to the 'back' state
	 * <li>on event 'submit', transition to the
	 * '${stateIdPrefix}.bindAndValidate' state
	 * </ul>
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g.
	 *        personDetails.view)
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix, Transition[] transitions)
			throws IllegalArgumentException {
		return addViewState(stateIdPrefix, (String)null, transitions);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * <p>
	 * By default, the view state will be configured with a logical
	 * <code>viewName</code> equal to its qualified state ID. The qualified
	 * state ID is built by appending the VIEW action constant to the provided
	 * stateIdPrefix argument. This means, for example, a provided
	 * <code>stateIdPrefix</code> of "personDetails" would result in a
	 * qualified stateId of "personDetails.view" and a <code>viewName</code>
	 * also of "personDetails.view". This view name will be mapped to a physical
	 * view resource to render a response when the view state is entered during
	 * a flow execution.
	 * <p>
	 * The view state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event 'cancel', transition to the 'cancel' state
	 * <li>on event 'back', transition to the 'back' state
	 * <li>on event 'submit', transition to the
	 * '${stateIdPrefix}.bindAndValidate' state
	 * </ul>
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateIdPrefix) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * <p>
	 * By default, the view state will be configured with a logical
	 * <code>viewName</code> equal to its qualified state ID. The qualified
	 * state ID is built by appending the VIEW action constant to the provided
	 * stateIdPrefix argument. This means, for example, a provided
	 * <code>stateIdPrefix</code> of "personDetails" would result in a
	 * qualified stateId of "personDetails.view" and a <code>viewName</code>
	 * also of "personDetails.view". This view name will be mapped to a physical
	 * view resource to render a response when the view state is entered during
	 * a flow execution.
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateIdPrefix, Transition transition) throws IllegalArgumentException {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName(stateIdPrefix), transition);
	}

	/**
	 * Factory method that generates a viewName from a stateIdPrefix. This
	 * implementation appends the "view" action constant to the prefix in the
	 * form ${stateIdPrefix}.view. Subclasses may override.
	 * @param stateIdPrefix The stateIdPrefix
	 * @return The view name.
	 */
	protected String viewName(String stateIdPrefix) {
		return view(stateIdPrefix);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * <p>
	 * By default, the view state will be configured with a logical
	 * <code>viewName</code> equal to its qualified state ID. The qualified
	 * state ID is built by appending the VIEW action constant to the provided
	 * stateIdPrefix argument. This means, for example, a provided
	 * <code>stateIdPrefix</code> of "personDetails" would result in a
	 * qualified stateId of "personDetails.view" and a <code>viewName</code>
	 * also of "personDetails.view". This view name will be mapped to a physical
	 * view resource to render a response when the view state is entered during
	 * a flow execution.
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateIdPrefix, Transition[] transitions) throws IllegalArgumentException {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * <p>
	 * The view state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event 'cancel', transition to the 'cancel' state
	 * <li>on event 'back', transition to the 'back' state
	 * <li>on event 'submit', transition to the
	 * '${stateIdPrefix}.bindAndValidate' state
	 * </ul>
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
	 * @param viewName The name of the logical view name to render; this name
	 *        will be mapped to a physical resource template such as a JSP when
	 *        the ViewState is entered and control returns to the front
	 *        controller.
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, viewName, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
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
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition transition)
			throws IllegalArgumentException {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName, transition);
	}

	/**
	 * Adds a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link FlowConstants#VIEW}action constant will be appended to
	 *        this prefix to build a qualified state id (e.g personDetails.view)
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
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition[] transitions)
			throws IllegalArgumentException {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName, transitions);
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * <p>
	 * This method will attempt to locate the correct <code>Action</code>
	 * implementation by ID by contacting the <code>FlowServiceLocator</code>.
	 * The flow builder will fail-fast if Action lookup fails.
	 * <p>
	 * By default, the <code>id</code> of the Action to use for lookup using
	 * the <code>FlowServiceLocator</code> will be the same as the provided
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that name, or a NoSuchActionException will be thrown.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param transition A single supported transition for this state, mapping a
	 *        path from this state to another state (triggered by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 * @throws NoSuchActionException no action could be found with an
	 *         <code>id</code> equal to the <code>stateId</code> value.
	 */
	protected ActionState addActionState(String stateId, Transition transition) throws IllegalArgumentException,
			NoSuchActionException {
		return new ActionState(getFlow(), stateId, executeAction(actionId(stateId)), transition);
	}

	/**
	 * Factory method that returns an action bean identifier given a stateId as
	 * input. This default implementation simply returns the stateId; because as
	 * a default, the value of the action state id is also treated as the
	 * <code>id</code> of the action bean to lookup using the
	 * <code>FlowServiceLocator</code>.
	 * @param stateId The stateId
	 * @return the actionId
	 */
	protected String actionId(String stateId) {
		return stateId;
	}

	/**
	 * Adds an <code>ActionState</code> to the flow built by this builder. An
	 * action state executes an <code>Action</code> implementation when
	 * entered.
	 * <p>
	 * This method will attempt to locate the correct <code>Action</code>
	 * implementation by ID by contacting the <code>FlowServiceLocator</code>.
	 * The flow builder will fail-fast if Action lookup fails.
	 * <p>
	 * By default, the <code>id</code> of the Action to use for lookup using
	 * the <code>FlowServiceLocator</code> will be the same as the provided
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that name, or a NoSuchActionException will be thrown.
	 * @param stateId The qualified stateId for the state; must be unique in the
	 *        context of the flow built by this builder
	 * @param transitions The supported transitions for this state, where each
	 *        transition maps a path from this state to another state (triggered
	 *        by an event).
	 * @return The action state
	 * @throws IllegalArgumentException the stateId was not unique
	 * @throws NoSuchActionException no action could be found with an
	 *         <code>id</code> equal to the <code>stateId</code> value.
	 */
	protected ActionState addActionState(String stateId, Transition[] transitions) throws IllegalArgumentException,
			NoSuchActionException {
		return new ActionState(getFlow(), stateId, executeAction(actionId(stateId)), transitions);
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
	 * Adds a <i>create </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>create </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object creational logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute creational logic, they also establishes several naming
	 * conventions and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState createState = addCreateState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.create</td>
	 * <td>The create action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.create</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the create action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g. <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing some object creational logic
	 * you will wish to view the results of that creation.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>create</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.create). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix) {
		return addCreateState(stateIdPrefix, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>create </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>create </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object creational logic.
	 * <p>
	 * By default, the create action state will be configured with the following
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This default assumes, after successfully executing some object creational
	 * logic you will wish to view the results of that creation.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        <code>create</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.create). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param action The action that will execute the creational logic.
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action action) {
		return addCreateState(stateIdPrefix, action, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>create </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>create </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object creational logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>create</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.create). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(create(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>create </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>create </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object creational logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>create</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.create).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(create(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>get </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>get </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute retrieval logic, they also establishes several naming conventions
	 * and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState getState = addGetState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.get</td>
	 * <td>The get action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.get</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the get action state will be configured with the following
	 * default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g. <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing some object retrieval logic
	 * you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>get</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g person.get). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @return The action state
	 */
	protected ActionState addGetState(String stateIdPrefix) {
		return addGetState(stateIdPrefix, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>get </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>get </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * By default, the get action state will be configured with the following
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This default assumes, after successfully executing some object retrieval
	 * logic you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        <code>get</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g person.get). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @param action The action that will execute the retrieval logic.
	 * @return The action state
	 */
	protected ActionState addGetState(String stateIdPrefix, Action action) {
		return addGetState(stateIdPrefix, action, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>get </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>get </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>get</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g person.get). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addGetState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(get(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>get </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>get </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>get</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g person.get).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addGetState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(get(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>set </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>set </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that puts input data
	 * (typically in the request) into the flow model.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute attribute setting logic, they also establishes several naming
	 * conventions and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState setState = addSetState("personId", transition);</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>personId.set</td>
	 * <td>The set action qualifier is appended in a hierarchical fashion to
	 * the 'personId' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>personId.set</code>'</td>
	 * </table>
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>set</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g personId.set). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSetState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(set(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>set </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>set </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that sets attributes
	 * in the flow model.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>set</code> action constant will be appended to this prefix
	 *        to build the qualified state id (e.g personId.set).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSetState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(set(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>load </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>load </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute retrieval logic, they also establishes several naming conventions
	 * and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState loadState = addLoadState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.load</td>
	 * <td>The load action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.load</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the load action state will be configured with the following
	 * default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g. <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing some object retrieval logic
	 * you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>load</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.load). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addLoadState(String stateIdPrefix) {
		return addLoadState(stateIdPrefix, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>load </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>load </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * By default, the load action state will be configured with the following
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This default assumes, after successfully executing some object retrieval
	 * logic you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        <code>load</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.load). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param action The action that will execute the retrieval logic.
	 * @return The action state
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action action) {
		return addLoadState(stateIdPrefix, action, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>load </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>load </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>load</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.load). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addLoadState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(load(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>load </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>load </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that invokes object
	 * retrieval logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>load</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.load).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(load(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>search </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>search </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object query (finder) logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute retrieval logic, they also establishes several naming conventions
	 * and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState searchState = addSearchState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.search</td>
	 * <td>The search action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.search</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the search action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g. <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing some object retrieval logic
	 * you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>search</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.search). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addSearchState(String stateIdPrefix) {
		return addSearchState(stateIdPrefix, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>search </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>search </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object query (finder) logic.
	 * <p>
	 * By default, the search action state will be configured with the following
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This default assumes, after successfully executing some object retrieval
	 * logic you will wish to view the results of that retrieval.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        <code>search</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.search). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param action The action that will execute the retrieval logic.
	 * @return The action state
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action action) {
		return addSearchState(stateIdPrefix, action, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>search </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>search </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object query (finder) logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>search</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.search). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSearchState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(search(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>search </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>search </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object query (finder) logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>search</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.search).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(search(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>setup </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>setup </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * prepares a view (often a form) for display.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute retrieval logic, they also establishes several naming conventions
	 * and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState setupState = addSetupState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.setup</td>
	 * <td>The setup action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.setup</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the setup action state will be configured with the following
	 * default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g. <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing view setup logic, you wish to
	 * display the view.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>setup</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.setup). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addSetupState(String stateIdPrefix) {
		return addSetupState(stateIdPrefix, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>setup </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>setup </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * prepares a view (often a form) for display.
	 * <p>
	 * By default, the setup action state will be configured with the following
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>${stateIdPrefix}.view</code> state (e.g <code>person.view</code>)
	 * </ul>
	 * <p>
	 * This assumes, after successfully executing view setup logic, you wish to
	 * display the view.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        <code>setup</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.setup). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param action The action that will execute the retrieval logic.
	 * @return The action state
	 */
	protected ActionState addSetupState(String stateIdPrefix, Action action) {
		return addSetupState(stateIdPrefix, action, new Transition[] { onSuccessView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>setup </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>setup </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * prepares a view (often a form) for display.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>setup</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.setup). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSetupState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(setup(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>setup </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>setup </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object retrieval logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>setup</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.setup).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSetupState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(setup(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>bindAndValidate </i> <code>ActionState</code> to the flow
	 * built by this builder. The <i>bindAndValidate </i> stereotype is a simple
	 * qualifier that indicates this action state, when entered, executes an
	 * action that binds form input to a backing object and validates it.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute binding/validation logic, they also establishes several naming
	 * conventions and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState bindAndValidateState = addBindAndValidateState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.bindAndValidate</td>
	 * <td>The bindAndValidate action qualifier is appended in a hierarchical
	 * fashion to the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.bindAndValidate</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the bindAndValidate action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * This example assumes after successfully executing bind and validate logic
	 * the flow will end. It also assumes, if the bind and validate action
	 * returns 'error', the form view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>bindAndValidate</code> action constant will be appended to
	 *        this prefix to build the qualified state id (e.g
	 *        person.bindAndValidate). Note: the qualified state ID will also be
	 *        used as the actionId, to lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix) {
		return addBindAndValidateState(stateIdPrefix,
				new Transition[] { onSuccessFinish(), onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>bindAndValidate </i> <code>ActionState</code> to the flow
	 * built by this builder. The <i>bindAndValidate </i> stereotype is a simple
	 * qualifier that indicates this action state, when entered, executes an
	 * action that binds form input to a backing object and validates it.
	 * <p>
	 * The bindAndValidate action state will be configured with the following
	 * default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * This example assumes after successfully executing bind and validate logic
	 * the flow will end. It also assumes, if the bind and validate action
	 * returns 'error', the form view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>bindAndValidate</code> action constant will be appended to
	 *        this prefix to build the qualified state id (e.g
	 *        person.bindAndValidate).
	 * @param action The action that will execute when this state is entered
	 * @return The action state
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action action) {
		return addBindAndValidateState(stateIdPrefix, action, new Transition[] { onSuccessFinish(),
				onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>bindAndValidate </i> <code>ActionState</code> to the flow
	 * built by this builder. The <i>bindAndValidate </i> stereotype is a simple
	 * qualifier that indicates this action state, when entered, executes an
	 * action that binds form input to a backing object and validates it.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>bindAndValidate</code> action constant will be appended to
	 *        this prefix to build the qualified state id (e.g
	 *        person.bindAndValidate). Note: the qualified state ID will also be
	 *        used as the actionId, to lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(bindAndValidate(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>bindAndValidate </i> <code>ActionState</code> to the flow
	 * built by this builder. The <i>bindAndValidate </i> stereotype is a simple
	 * qualifier that indicates this action state, when entered, executes an
	 * action that binds form input to a backing object and validates it.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>bindAndValidate</code> action constant will be appended to
	 *        this prefix to build the qualified state id (e.g
	 *        person.bindAndValidate).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(bindAndValidate(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>save </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>save </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that saves data out
	 * to a persistent store.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute persistence logic, they also establishes several naming
	 * conventions and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState saveState = addSaveState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.save</td>
	 * <td>The save action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.save</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the save action state will be configured with the following
	 * default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * These defaults assume after successfully executing save logic the flow
	 * should end. It also assumes, if the save action returns 'error', the form
	 * view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>save</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.save). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addSaveState(String stateIdPrefix) {
		return addSaveState(stateIdPrefix, new Transition[] { onSuccessFinish(), onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>save </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>save </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that saves data out
	 * to a persistent store.
	 * <p>
	 * The save action state will be configured with the following default state
	 * transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * These defaults assume after successfully executing save logic the flow
	 * should end. It also assumes, if the save action returns 'error', the form
	 * view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>save</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.save).
	 * @return The action state
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action) {
		return addSaveState(stateIdPrefix, action, new Transition[] { onSuccessFinish(), onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>save </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>save </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that saves data out
	 * to a persistent store.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>save</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.save). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSaveState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>save </i> <code>ActionState</code> to the flow built by this
	 * builder. The <i>save </i> stereotype is a simple qualifier that indicates
	 * this action state, when entered, executes an action that saves data out
	 * to a persistent store.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>save</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.save).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>delete </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>delete </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that binds
	 * form input to a backing object and validates it.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * <p>
	 * As the various flavors of this method add an action state intended to
	 * execute binding/validation logic, they also establishes several naming
	 * conventions and relavent defaults. For example, the usage:
	 * <p>
	 * <code>ActionState deleteState = addDeleteState("person");</code>
	 * <p>
	 * ... builds an action state with the following properties: <table
	 * border="1">
	 * <tr>
	 * <th>Property</th>
	 * <th>Value</th>
	 * <th>Notes</th>
	 * <tr>
	 * <td>id</td>
	 * <td>person.delete</td>
	 * <td>The delete action qualifier is appended in a hierarchical fashion to
	 * the 'person' prefix</td>
	 * <tr>
	 * <td>action</td>
	 * <td colspan="2">The <code>Action</code> implementation in the registry
	 * exported with the id '<code>person.delete</code>'</td>
	 * </table>
	 * <p>
	 * In addition, the delete action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * This example assumes after successfully executing deletion logic the flow
	 * will end. It also assumes, if the delete action returns 'error', the form
	 * view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>delete</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.delete). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @return The action state
	 */
	protected ActionState addDeleteState(String stateIdPrefix) {
		return addDeleteState(stateIdPrefix, new Transition[] { onSuccessFinish(), onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>delete </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>delete </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object deletion logic.
	 * <p>
	 * The delete action state will be configured with the following default
	 * state transitions:
	 * <ul>
	 * <li>on event <code>success</code>, transition to the
	 * <code>finish</code> end state, ending the flow.
	 * <li>on event <code>error</code>, transition back to the
	 * <code>${stateIdPrefix}.view</code> view state (typically the form view,
	 * so input may be revised and subsequently resubmitted.)
	 * </ul>
	 * <p>
	 * This example assumes after successfully executing deletion logic the flow
	 * will end. It also assumes, if the delete action returns 'error', the form
	 * view should be displayed.
	 * <p>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetitive configuration code for common situations.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>delete</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.delete).
	 * @param action The action that will execute when this state is entered
	 * @return The action state
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action) {
		return addDeleteState(stateIdPrefix, action, new Transition[] { onSuccessFinish(), onErrorView(stateIdPrefix) });
	}

	/**
	 * Adds a <i>delete </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>delete </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object deletion logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>delete</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.delete). Note:
	 *        the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>delete </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>delete </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object deletion logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>delete</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.delete).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds a <i>validate </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>validate </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * performs data validation logic.
	 * <p>
	 * The <code>Action</code> implementation to use will be looked up by ID
	 * by messaging the configured <code>FlowServiceLocator</code>. This flow
	 * builder will fail-fast if the lookup fails. By default, the Action
	 * <code>id</code> to use for lookup will be the same as the specified
	 * <code>stateId</code>. It is expected that a valid <code>Action</code>
	 * implementation be exported in the backing service locator registry under
	 * that id, or a <code>NoSuchActionException</code> will be thrown.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>validate</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.validate).
	 *        Note: the qualified state ID will also be used as the actionId, to
	 *        lookup in the locator's registry.
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addValidateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(validate(stateIdPrefix), transitions);
	}

	/**
	 * Adds a <i>validate </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>validate </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * performs data validation logic.
	 * @param stateIdPrefix The <code>ActionState</code> id prefix. Note: the
	 *        <code>validate</code> action constant will be appended to this
	 *        prefix to build the qualified state id (e.g person.validate).
	 * @param action The action that will execute when this state is entered
	 * @param transitions The supported transitions for this state, where each
	 *        maps a path from this state to another state (triggered by an
	 *        event).
	 * @return The action state
	 */
	protected ActionState addValidateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(validate(stateIdPrefix), action, transitions);
	}

	/**
	 * Adds an end state with the specified id that will display the specified
	 * view when entered as part of a terminating flow execution.
	 * @param endStateId The end state id
	 * @param viewName The view name
	 * @return The end state
	 */
	protected EndState addEndState(String endStateId, String viewName) {
		return new EndState(getFlow(), endStateId, viewName);
	}

	/**
	 * Adds an end state with the specified id;.
	 * @param endStateId The end state id
	 * @return The end state
	 */
	protected EndState addEndState(String endStateId) {
		return new EndState(getFlow(), endStateId);
	}

	/**
	 * Adds an end state with id <code>finish</code>.
	 * @return The end state
	 */
	protected EndState addFinishEndState() {
		return addEndState(getDefaultFinishEndStateId());
	}

	/**
	 * Adds an end state with id <code>finish</code> that will display the
	 * specifid view when entered as part of a terminating flow execution.
	 * @param viewName the view
	 * @return The end state the end state
	 */
	protected EndState addFinishEndState(String viewName) {
		return addEndState(getDefaultFinishEndStateId(), viewName);
	}

	/**
	 * Adds an end state with id <code>back</code>.
	 * @return The end state
	 */
	protected EndState addBackEndState() {
		return addEndState(getDefaultBackEndStateId());
	}

	/**
	 * Adds an end state with id <code>back</code> that will display the
	 * specifid view when entered as part of a terminating flow execution.
	 * @param viewName the view
	 * @return The end state
	 */
	protected EndState addBackEndState(String viewName) {
		return addEndState(getDefaultBackEndStateId(), viewName);
	}

	/**
	 * Adds an end state with id <code>cancel</code>.
	 * @return The end state
	 */
	protected EndState addCancelEndState() {
		return addEndState(getDefaultCancelEndStateId());
	}

	/**
	 * Adds an end state with id <code>cancel</code> that will display the
	 * specified view when entered as part of a terminating flow execution.
	 * @param viewName the view
	 * @return The end state
	 */
	protected EndState addCancelEndState(String viewName) {
		return addEndState(getDefaultCancelEndStateId(), viewName);
	}

	/**
	 * Adds 'finish', 'back', and 'cancel' end states as a convenience.
	 */
	protected void addDefaultEndStates() {
		addCancelEndState();
		addBackEndState();
		addFinishEndState();
	}

	/**
	 * Adds 'finish', 'back', and 'cancel' end states as a convenience; each
	 * state will request rendering of the specified view when entered.
	 * @param viewName the view
	 */
	protected void addDefaultEndStates(String viewName) {
		addCancelEndState(viewName);
		addBackEndState(viewName);
		addFinishEndState(viewName);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of event ${eventId}, transition to state
	 * ${stateId}.
	 * </ul>
	 * @param eventId The event id
	 * @param stateId the state Id
	 * @return the transition
	 */
	protected Transition onEvent(String eventId, String stateId) {
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
	protected Transition onEvent(String actionName, String eventId, String stateId) {
		return new Transition(join(actionName, eventId), stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of event that matches the criteria defined by
	 * ${eventIdCriteria}, transition to state ${stateId}.
	 * </ul>
	 * @param eventIdCriteria The event id criteria
	 * @param stateId the state Id
	 * @return the transition
	 */
	protected Transition onEvent(Constraint eventIdCriteria, String stateId) {
		return new Transition(eventIdCriteria, stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>On the occurence of any event (*), transition to state ${stateId}.
	 * </ul>
	 * @param stateId the state Id
	 * @return the transition
	 */
	protected Transition onAnyEvent(String stateId) {
		return new Transition(Transition.WILDCARD_EVENT_CRITERIA, stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition (success->${stateId})
	 */
	protected Transition onSuccess(String stateId) {
		return onEvent(getSuccessEventId(), stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>${actionName}.success</code> event,
	 * transition to the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param actionName the actionName
	 * @param stateId The state id
	 * @return The transition
	 */
	protected Transition onSuccess(String actionName, String stateId) {
		return onEvent(actionName, getSuccessEventId(), stateId);
	}

	/**
	 * Returns the 'success' event id. Subclasses may override
	 * @return Ther event id
	 */
	protected String getSuccessEventId() {
		return FlowConstants.SUCCESS;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.get</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g success->person.get)
	 */
	protected Transition onSuccessGet(String stateIdPrefix) {
		return onSuccess(get(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.setup</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g personForm)
	 * @return The transition (e.g success->personForm.setup)
	 */
	protected Transition onSuccessSetup(String stateIdPrefix) {
		return onSuccess(setup(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the view state with the id <code>${stateIdPrefix}.view</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition
	 */
	protected Transition onSuccessView(String stateIdPrefix) {
		return onSuccess(view(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.save</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g success->person.save)
	 */
	protected Transition onSuccessSave(String stateIdPrefix) {
		return onSuccess(save(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>success</code>' event, transition
	 * to the default end state with the id '<code>finish</code>'.
	 * </ul>
	 * @return The transition (e.g success->finish)
	 */
	protected Transition onSuccessFinish() {
		return onSuccess(getDefaultFinishEndStateId());
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>submit</code>' event, transition
	 * to the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition (submit->${stateId})
	 */
	protected Transition onSubmit(String stateId) {
		return onEvent(getSubmitEventId(), stateId);
	}

	/**
	 * Returns the 'submit' event id. Subclasses may override.
	 * @return The submit event id.
	 */
	protected String getSubmitEventId() {
		return FlowConstants.SUBMIT;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>submit</code>' event, transition
	 * to the action state with the id
	 * <code>${stateIdPrefix}.bindAndValidate</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g submit->person.bindAndValidate)
	 */
	protected Transition onSubmitBindAndValidate(String stateIdPrefix) {
		return onSubmit(bindAndValidate(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>back</code> event, transition to the
	 * state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition (back->${stateId})
	 */
	protected Transition onBack(String stateId) {
		return onEvent(getBackEventId(), stateId);
	}

	/**
	 * Returns the 'back' event id. Subclassses may override
	 * @return the back eventId.
	 */
	protected String getBackEventId() {
		return FlowConstants.BACK;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the action state with the id <code>${stateIdPrefix}.get</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g back->person.get)
	 */
	protected Transition onBackGet(String stateIdPrefix) {
		return onBack(get(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the action state with the id <code>${stateIdPrefix}.setup</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g personForm)
	 * @return The transition (e.g back->personForm.setup)
	 */
	protected Transition onBackSetup(String stateIdPrefix) {
		return onBack(setup(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the view state with the id <code>${stateIdPrefix}.view</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g personForm)
	 * @return The transition (e.g back->personForm.view)
	 */
	protected Transition onBackView(String stateIdPrefix) {
		return onBack(view(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the end state with the id '<code>cancel</code>'.
	 * </ul>
	 * @return The transition (e.g back->cancel)
	 */
	protected Transition onBackCancel() {
		return onBack(getDefaultCancelEndStateId());
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the end state with the id '<code>finish</code>'.
	 * </ul>
	 * @return The transition (e.g back->finish)
	 */
	protected Transition onBackFinish() {
		return onBack(getDefaultFinishEndStateId());
	}

	/**
	 * Returns the 'cancel' end state id.
	 * @return The cancel end state id.
	 */
	protected String getDefaultCancelEndStateId() {
		return FlowConstants.CANCEL;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>back</code>' event, transition to
	 * the end state with the id '<code>back</code>'.
	 * </ul>
	 * @return The transition (e.g back->back)
	 */
	protected Transition onBackEnd() {
		return onBack(getDefaultBackEndStateId());
	}

	/**
	 * Returns the 'back' end state id.
	 * @return the back end state id.
	 */
	protected String getDefaultBackEndStateId() {
		return FlowConstants.BACK;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>cancel</code> event, transition to
	 * the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition
	 */
	protected Transition onCancel(String stateId) {
		return onEvent(getCancelEventId(), stateId);
	}

	/**
	 * Returns the 'cancel' eventId
	 * @return the cancel eventId.
	 */
	protected String getCancelEventId() {
		return FlowConstants.CANCEL;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>cancel</code>' event, transition
	 * to the end state with the id '<code>cancel</code>'.
	 * </ul>
	 * @return The transition (e.g cancel->cancel)
	 */
	protected Transition onCancelEnd() {
		return onCancel(getDefaultCancelEndStateId());
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>cancel</code>' event, transition
	 * to the end state with the id '<code>finish</code>'.
	 * </ul>
	 * @return The transition (e.g cancel->finish)
	 */
	protected Transition onCancelFinish() {
		return onBack(getDefaultFinishEndStateId());
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>finish</code> event, transition to
	 * the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition (finish->${stateId})
	 */
	protected Transition onFinish(String stateId) {
		return onEvent(getFinishEventId(), stateId);
	}

	/**
	 * Returns the finish eventId.
	 * @return the finish eventId.
	 */
	protected String getFinishEventId() {
		return FlowConstants.FINISH;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>finish</code>' event, transition
	 * to the end state with the id '<code>finish</code>'.
	 * </ul>
	 * @return The transition (e.g finish->finish)
	 */
	protected Transition onFinishEnd() {
		return onFinish(getDefaultFinishEndStateId());
	}

	/**
	 * Returns the finish end stateId.
	 * @return the finish end stateId.
	 */
	protected String getDefaultFinishEndStateId() {
		return FlowConstants.FINISH;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>finish</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.get</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g finish->person.get)
	 */
	protected Transition onFinishGet(String stateIdPrefix) {
		return onFinish(get(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>finish</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.setup</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g personForm)
	 * @return The transition (e.g finish->personForm.setup)
	 */
	protected Transition onFinishSetup(String stateIdPrefix) {
		return onFinish(setup(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>finish</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.save</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g finish->person.save)
	 */
	protected Transition onFinishSave(String stateIdPrefix) {
		return onFinish(save(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>select</code> event, transition to
	 * the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition (select->${stateId})
	 */
	protected Transition onSelect(String stateId) {
		return onEvent(getSelectEventId(), stateId);
	}

	/**
	 * Returns the 'select' event id.
	 * @return The eventId.
	 */
	protected String getSelectEventId() {
		return FlowConstants.SELECT;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>select</code>' event, transition
	 * to the action state with the id <code>${stateIdPrefix}.get</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition (e.g select->person.get)
	 */
	protected Transition onSelectGet(String stateIdPrefix) {
		return onSelect(get(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>error</code>' event, transition to
	 * the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param stateId The state id
	 * @return The transition The transition
	 */
	protected Transition onError(String stateId) {
		return onEvent(getErrorEventId(), stateId);
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the <code>${actionName}.error</code> event,
	 * transition to the state with the id <code>${stateId}</code>
	 * </ul>
	 * @param actionName the action name qualifier
	 * @param stateId The state id
	 * @return The transition The transition
	 */
	protected Transition onError(String actionName, String stateId) {
		return onEvent(actionName, getErrorEventId(), stateId);
	}

	/**
	 * Returns the well-known 'error' event id. Subclasses may override.
	 * @return The error event id
	 */
	protected String getErrorEventId() {
		return FlowConstants.ERROR;
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>error</code>' event, transition to
	 * the view state with the id <code>${stateIdPrefix}.setup</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition
	 */
	protected Transition onErrorSetup(String stateIdPrefix) {
		return onError(view(stateIdPrefix));
	}

	/**
	 * Creates a transition stating:
	 * <ul>
	 * <li>on an occurence of the '<code>error</code>' event, transition to
	 * the view state with the id <code>${stateIdPrefix}.view</code>.
	 * </ul>
	 * @param stateIdPrefix The state id qualifier (e.g person)
	 * @return The transition
	 */
	protected Transition onErrorView(String stateIdPrefix) {
		return onError(view(stateIdPrefix));
	}

	/**
	 * Append the create action state stereotype to the provided stateId,
	 * building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String create(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.CREATE);
	}

	/**
	 * Append the get action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String get(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.GET);
	}

	/**
	 * Append the load action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String load(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.LOAD);
	}

	/**
	 * Append the setup action state stereotype to the provided stateId,
	 * building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String setup(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SETUP);
	}

	/**
	 * Append the view state stereotype to the provided stateId, building a
	 * qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String view(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.VIEW);
	}

	/**
	 * Append the set action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String set(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SET);
	}

	/**
	 * Append the add action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String add(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.ADD);
	}

	/**
	 * Append the save action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String save(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SAVE);
	}

	/**
	 * Append the bindAndValidate action state stereotype to the provided
	 * stateId, building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String bindAndValidate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.BIND_AND_VALIDATE);
	}

	/**
	 * Append the bind action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String bind(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.BIND);
	}

	/**
	 * Append the validate action state stereotype to the provided stateId,
	 * building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String validate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.VALIDATE);
	}

	/**
	 * Append the delete action state stereotype to the provided stateId,
	 * building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String delete(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.DELETE);
	}

	/**
	 * Append the edit action state stereotype to the provided stateId, building
	 * a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String edit(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.EDIT);
	}

	/**
	 * Append the search action state stereotype to the provided stateId,
	 * building a qualified stateId.
	 * @param stateIdPrefix the state id prefix
	 * @return the qualified state id
	 */
	protected String search(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SEARCH);
	}

	/**
	 * Builds a qualified state id by joining a <code>stateIdPrefix</code>
	 * qualifier with a descriptive suffix that communicates the action to be
	 * performed at that state. The returned result is the qualified state id
	 * that must be unique among all states in the flow built by this builder.
	 * @param stateIdPrefix (e.g. "person")
	 * @param stateIdSuffix (e.g. "get")
	 * @return The qualfied stateId (e.g. "person.get")
	 */
	protected String buildStateId(String stateIdPrefix, String stateIdSuffix) {
		if (stateIdPrefix.endsWith(stateIdSuffix)) {
			return stateIdPrefix;
		}
		else {
			return join(stateIdPrefix, stateIdSuffix);
		}
	}

	/**
	 * Appends the identifying 'modelMapper' suffix to the specified prefix
	 * qualifier, returning a fully-qualified model mapper service identifier.
	 * For example: <code>modelMapper("personId")</code> results in
	 * <code>personId.modelMapper</code>.
	 * @param modelMapperIdPrefix The model mapper ID qualifier
	 * @return The qualified model mapper id.
	 */
	protected String modelMapper(String modelMapperIdPrefix) {
		Assert.notNull(modelMapperIdPrefix, "The model mapper id prefix is required");
		if (!modelMapperIdPrefix.endsWith(MODEL_MAPPER_ID_SUFFIX)) {
			return modelMapperIdPrefix + getQualifierDelimiter() + MODEL_MAPPER_ID_SUFFIX;
		}
		else {
			return modelMapperIdPrefix;
		}
	}

	/**
	 * Returns the default model mapper id for the flow built by this builder.
	 * <p>
	 * By default, returns "${flowId}.modelMapper"
	 * @return the default model mapper id.
	 */
	protected String getDefaultFlowModelMapperId() {
		return modelMapper(flowId());
	}

	/**
	 * Qualify the specified id suffix with the id of the flow built by this
	 * builder.
	 * @param idSuffix the suffix to qualify with the flow id (e.g myAction)
	 * @return the qualified id (e.g MyFlow.myAction)
	 */
	protected String qualify(String idSuffix) {
		return join(flowId(), idSuffix);
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
	 * state id ("personDetails.get"). Detaults to a dot (".").
	 */
	protected String getQualifierDelimiter() {
		return DOT_SEPARATOR;
	}
}