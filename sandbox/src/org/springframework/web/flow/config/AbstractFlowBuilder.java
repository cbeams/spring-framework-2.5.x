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
 *
 *   public static final String PERSON_DETAILS = "personDetails";
 * 
 *   protected String flowId() {
 *       return PERSON_DETAILS;
 *   }
 *   
 *   public void buildStates() {
 *       addGetState(PERSON_DETAILS);
 *       addViewState(PERSON_DETAILS);
 *       addBindAndValidateState(PERSON_DETAILS);
 *       addDefaultEndState();
 *   }
 * </pre>
 * 
 * What this java-based FlowBuilder implementation does is add four states to a
 * flow identified as "personDetails". These include a "get"
 * <code>ActionState</code> (the start state), a <code>ViewState</code>
 * state, a "bind and validate" <code>ActionState</code>, and an end marker
 * state (<code>EndState</code>).
 * 
 * The first state, an action state, will be assigned the indentifier
 * 'personDetails.get'. This action state will automatically be configured with
 * the following defaults:
 * <ol>
 * <li>The action bean identifier 'personDetails.get'; this is the name of the
 * <code>Action</code> implementation that will execute when this state is
 * entered. In this example, that <code>Action</code> will go out to the DB,
 * load the Person, and put it in the Flow's data model.
 * <li>A "success" transition to a default view state, called
 * 'personDetails.view'. This means when the get <code>Action</code> returns a
 * "success" result event (aka outcome), the 'personDetails.view' state will be
 * entered.
 * <li>It will act as the start state for this flow (by default, the first
 * state added to a flow during the build process is treated as the start
 * state.)
 * </ol>
 * 
 * The second state, a view state, will be identified as 'personDetails.view'.
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called 'personDetails.view' - this is the logical name of a
 * view resource. This logical view name gets mapped to a physical view resource
 * (jsp, etc.) by the calling front controller (via a spring view resolver, or a
 * struts action forward, for example.)
 * <li>A "submit" transition to a bind and validate action state, indentified
 * by the default ID 'personDetails.bindAndValidate'. This means when a 'submit'
 * event is signaled by the view (for example, on a submit button click), the
 * bindAndValidate action state will be entered and the '
 * <code>personDetails.bindAndValidate</code>'<code>Action</code>
 * implementation will be executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as
 * 'personDetails.bindAndValidate'. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.bindAndValidate' - this is the name
 * of the <code>Action</code> implementation exported in the application
 * context that will execute when this state is entered. In this example, the
 * <code>Action</code> will bind form input in the HTTP request to a backing
 * Person form object, validate it, and update the DB.
 * <li>A "success" transition to a default end state, called 'finish'. This
 * means if the <code>Action</code> returns a "success" result, the 'finish'
 * end state will be transitioned to and the flow will terminate.
 * <li>A "error" transition back to the form view. This means if the
 * <code>Action</code> returns a "error" event, the 'personDetails.view' view
 * state will be transitioned back to.
 * </ol>
 * 
 * The fourth and last state, an end state, will be indentified with the default
 * end state ID 'finish'. This end state is a marker that signals the end of the
 * flow. When entered, the flow session terminates, and if this flow is acting
 * as a root flow in the current flow execution, any flow-allocated resources
 * will be cleaned up. An end state can optionally be configured with a logical
 * view name to forward to when entered. It will also trigger a state transition
 * in a resuming parent flow if this flow was participating as a spawned 'sub
 * flow' within a suspended parent flow.
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
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. By default, the subflow state ID will also be treated as the id of
	 * the subflow definition to spawn, to be used for retrieval by the
	 * FlowServiceLocator.
	 * @param id the state id, must be unique among all states of the flow built
	 *        by this builder. This id also acts as the id of the subFlow.
	 * @param subFlowId the id of the Flow definition to retieve (could also be
	 *        the ID of a FlowFactoryBean that produces the Flow)
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a subflow state to the flow built by this builder with the specified
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
	 * Add a <code>ViewState</code> marker to the flow built by this builder.
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
	 * @return The view marker state
	 * @throws IllegalStateException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, (String)null);
	}

	/**
	 * Add a <code>ViewState</code> marker to the flow built by this builder.
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g. personDetails.view)
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
	 * Add a <code>ViewState</code> marker to the flow built by this builder.
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g. personDetails.view)
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
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
	 * @return The view state
	 * @throws IllegalArgumentException the stateId was not unique after
	 *         qualificaion
	 */
	protected ViewState addViewState(String stateIdPrefix) throws IllegalArgumentException {
		return addViewState(stateIdPrefix, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
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
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
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
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
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
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
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
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
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
	 * Add a <code>ViewState</code> to the flow built by this builder. A view
	 * state triggers the rendering of a view template when entered.
	 * 
	 * @param stateIdPrefix The <code>ViewState</code> id prefix; note: the
	 *        {@link VIEW}action constant will be appended to this prefix to
	 *        build a qualified state id (e.g personDetails.view)
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
	 * builder will fail-fast if the lookup fails.
	 * <p>
	 * By default, the Action <code>id</code> to use for lookup will be the
	 * same as the specified <code>stateId</code>. It is expected that a
	 * valid <code>Action</code> implementation be exported in the backing
	 * service locator registry under that ID, or a NoSuchActionException will
	 * be thrown.
	 * <p>
	 * As this method adds a action state intended to execute creational logic,
	 * it also establishes several naming conventions and relavent defaults:
	 * <p>
	 * For example, the usage:
	 * 
	 * <pre>
	 *   ActionState createState = addCreateState("person");
	 * </pre>
	 * 
	 * ... builds an action state with the following properties:
	 * <ul>
	 * <li>id: <code>person.create</code> (note how the {@link CREATE}action
	 * qualifier is appended in a hierarchical fashion to the 'person' prefix)
	 * <li>action: set to the <code>Action</code> implementation in the
	 * registry exported with the id '<code>person.create</code>'
	 * </ul>
	 * <p>
	 * In addition, the create action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event 'success', transition to the '${stateIdPrefix}.view' state
	 * (e.g <code>person.view</code>) This assumes, after successfully
	 * executing some object creational logic you will wish to view the results
	 * of that creation.
	 * </ul>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        {@link CREATE}action constant will be appended to this prefix to
	 *        build the qualified state id (e.g person.create). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix) {
		return addCreateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * Adds a <i>create </i> <code>ActionState</code> to the flow built by
	 * this builder. The <i>create </i> stereotype is a simple qualifier that
	 * indicates this action state, when entered, executes an action that
	 * invokes object creational logic.
	 * <p>
	 * As this method adds a action state intended to execute creational logic,
	 * it also establishes several naming conventions and relavent defaults:
	 * <p>
	 * For example, the usage:
	 * 
	 * <pre>
	 *   ActionState createState = addCreateState("person", myCreateAction);
	 * </pre>
	 * 
	 * ... builds an action state with the following properties:
	 * <ul>
	 * <li>id: <code>person.create</code> (note how the {@link CREATE}action
	 * qualifier is appended in a hierarchical fashion to the 'person' prefix)
	 * <li>action: set to the 'myCreateAction' <code>Action</code> implementation
	 * </ul>
	 * <p>
	 * In addition, the create action state will be configured with the
	 * following default state transitions:
	 * <ul>
	 * <li>on event 'success', transition to the '${stateIdPrefix}.view' state
	 * (e.g <code>person.view</code>) This assumes, after successfully
	 * executing some object creational logic you will wish to view the results
	 * of that creation.
	 * </ul>
	 * If these defaults do not fit your needs, use one of the more generic
	 * action state builder methods. This method is provided as a convenience to
	 * help reduce repetive configuration code for common situations.
	 * 
	 * @param stateIdPrefix The <code>ActionState</code> id prefix; note: the
	 *        {@link CREATE}action constant will be appended to this prefix to
	 *        build the qualified state id (e.g person.create). Note: the
	 *        qualified state ID will also be used as the actionId, to lookup in
	 *        the locator's registry.
	 * @param action The action that will execute the creational logic.
	 * @return The action state
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action action) {
		return addCreateState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Transition transition) {
		return addCreateState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action action, Transition transition) {
		return addCreateState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(create(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(create(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix) {
		return addGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action action) {
		return addGetState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Transition transition) {
		return addGetState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action action, Transition transition) {
		return addGetState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(get(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(get(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix) {
		return addSetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action action) {
		return addSetState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Transition transition) {
		return addSetState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action action, Transition transition) {
		return addSetState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(set(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(set(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix) {
		return addGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action action) {
		return addLoadState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Transition transition) {
		return addLoadState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action action, Transition transition) {
		return addLoadState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(load(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(load(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix) {
		return addSearchState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action action) {
		return addSearchState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Transition transition) {
		return addSearchState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action action, Transition transition) {
		return addSearchState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(search(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(search(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix) {
		return addSetupState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix, Action action) {
		return addSetupState(stateIdPrefix, action, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix, Transition transition) {
		return addSetupState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix, Action action, Transition transition) {
		return addSetupState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(setup(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSetupState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(setup(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix) {
		return addBindAndValidateState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action action) {
		return addBindAndValidateState(stateIdPrefix, action, new Transition[] { onSuccessEnd(),
				onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(bindAndValidate(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(bindAndValidate(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix) {
		return addSaveState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action) {
		return addSaveState(stateIdPrefix, action, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param successStateId
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, String successStateId) {
		return addSaveState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param successStateId
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action, String successStateId) {
		return addSaveState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action, Transition transition) {
		return addSaveState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix) {
		return addDeleteState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action) {
		return addDeleteState(stateIdPrefix, action, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param successAndErrorStateId
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, String successAndErrorStateId) {
		return addDeleteState(stateIdPrefix, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param successAndErrorStateId
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action, String successAndErrorStateId) {
		return addDeleteState(stateIdPrefix, action, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action, Transition transition) {
		return addDeleteState(stateIdPrefix, action, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), action, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addValidateState(String stateIdPrefix) {
		return addValidateState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
	 */
	protected ActionState addValidateState(String stateIdPrefix, Action action) {
		return addValidateState(stateIdPrefix, action, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addValidateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(validate(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addValidateState(String stateIdPrefix, Action action, Transition[] transitions) {
		return addActionState(validate(stateIdPrefix), action, transitions);
	}

	/**
	 * @param endStateId
	 * @param viewName
	 * @return
	 */
	protected EndState addEndState(String endStateId, String viewName) {
		return new EndState(getFlow(), endStateId, viewName);
	}

	/**
	 * @param endStateId
	 * @return
	 */
	protected EndState addEndState(String endStateId) {
		return new EndState(getFlow(), endStateId);
	}

	/**
	 * @return
	 */
	protected EndState addFinishEndState() {
		return addEndState(getDefaultFinishEndStateId());
	}

	/**
	 * @param viewName
	 * @return
	 */
	protected EndState addFinishEndState(String viewName) {
		return addEndState(getDefaultFinishEndStateId(), viewName);
	}

	/**
	 * @return
	 */
	protected EndState addBackEndState() {
		return addEndState(getDefaultBackEndStateId());
	}

	/**
	 * @param backViewName
	 * @return
	 */
	protected EndState addBackEndState(String viewName) {
		return addEndState(getDefaultBackEndStateId(), viewName);
	}

	/**
	 * @return
	 */
	protected EndState addCancelEndState() {
		return addEndState(getDefaultCancelEndStateId());
	}

	/**
	 * @param cancelViewName
	 * @return
	 */
	protected EndState addCancelEndState(String viewName) {
		return addEndState(getDefaultCancelEndStateId(), viewName);
	}

	/**
	 *  
	 */
	protected void addDefaultEndStates() {
		addCancelEndState();
		addBackEndState();
		addFinishEndState();
	}

	/**
	 * @param viewName
	 */
	protected void addDefaultEndStates(String viewName) {
		addCancelEndState(viewName);
		addBackEndState(viewName);
		addFinishEndState(viewName);
	}

	/**
	 * @param eventId
	 * @param newState
	 * @return
	 */
	protected Transition onEvent(String eventId, String stateId) {
		return new Transition(eventId, stateId);
	}

	/**
	 * @param actionName
	 * @param eventId
	 * @param stateId
	 * @return
	 */
	protected Transition onEvent(String actionName, String eventId, String stateId) {
		return new Transition(join(actionName, eventId), stateId);
	}

	/**
	 * @param eventIdCriteria
	 * @param newStateId
	 * @return
	 */
	protected Transition onEvent(Constraint eventIdCriteria, String stateId) {
		return new Transition(eventIdCriteria, stateId);
	}

	/**
	 * @param newStateId
	 * @return
	 */
	protected Transition onAnyEvent(String stateId) {
		return new Transition(Transition.WILDCARD_EVENT_CRITERIA, stateId);
	}

	/**
	 * @param eventId
	 * @param newState
	 * @return
	 */
	protected Transition[] onEvents(String[] eventIds, String newState) {
		Transition[] transitions = new Transition[eventIds.length];
		for (int i = 0; i < eventIds.length; i++) {
			transitions[i] = new Transition(eventIds[i], newState);
		}
		return transitions;
	}

	/**
	 * @param successStateId
	 * @return
	 */
	protected Transition onSuccess(String successStateId) {
		return onEvent(getSuccessEventId(), successStateId);
	}

	/**
	 * @param actionName
	 * @param successStateId
	 * @return
	 */
	protected Transition onSuccess(String actionName, String successStateId) {
		return onEvent(actionName, getSuccessEventId(), successStateId);
	}

	/**
	 * @return
	 */
	protected String getSuccessEventId() {
		return FlowConstants.SUCCESS;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSuccessGet(String stateIdPrefix) {
		return onSuccess(get(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSuccessSetup(String stateIdPrefix) {
		return onSuccess(setup(stateIdPrefix));
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	protected Transition onSuccessView(String stateIdPrefix) {
		return onSuccess(view(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSuccessAdd(String stateIdPrefix) {
		return onSuccess(add(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSuccessSave(String stateIdPrefix) {
		return onSuccess(save(stateIdPrefix));
	}

	/**
	 * @return
	 */
	protected Transition onSuccessEnd() {
		return onSuccess(getDefaultFinishEndStateId());
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected Transition onSubmit(String stateId) {
		return onEvent(getSubmitEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getSubmitEventId() {
		return FlowConstants.SUBMIT;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSubmitBindAndValidate(String stateIdPrefix) {
		return onSubmit(bindAndValidate(stateIdPrefix));
	}

	/**
	 * @return
	 */
	protected Transition onSubmitEnd() {
		return onSubmit(getDefaultFinishEndStateId());
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected Transition onSearch(String stateId) {
		return onEvent(getSearchEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getSearchEventId() {
		return FlowConstants.SEARCH;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onSearchGet(String stateIdPrefix) {
		return onSearch(get(stateIdPrefix));
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected Transition onEdit(String stateId) {
		return onEvent(getEditEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getEditEventId() {
		return FlowConstants.EDIT;
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected Transition onBack(String stateId) {
		return onEvent(getBackEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getBackEventId() {
		return FlowConstants.BACK;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onBackSetup(String stateIdPrefix) {
		return onBack(setup(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onBackView(String stateIdPrefix) {
		return onBack(view(stateIdPrefix));
	}

	/**
	 * @return
	 */
	protected Transition onBackCancel() {
		return onBack(getDefaultCancelEndStateId());
	}

	/**
	 * @return
	 */
	protected String getDefaultCancelEndStateId() {
		return FlowConstants.CANCEL;
	}

	/**
	 * @return
	 */
	protected Transition onBackEnd() {
		return onBack(getDefaultBackEndStateId());
	}

	/**
	 * @return
	 */
	protected String getDefaultBackEndStateId() {
		return FlowConstants.BACK;
	}

	/**
	 * @param cancelStateId
	 * @return
	 */
	protected Transition onCancel(String stateId) {
		return onEvent(getCancelEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getCancelEventId() {
		return FlowConstants.CANCEL;
	}

	/**
	 * @return
	 */
	protected Transition onCancelEnd() {
		return onCancel(getDefaultCancelEndStateId());
	}

	/**
	 * @param finishStateId
	 * @return
	 */
	protected Transition onFinish(String stateId) {
		return onEvent(getFinishEventId(), stateId);
	}

	/**
	 * @return
	 */
	protected String getFinishEventId() {
		return FlowConstants.FINISH;
	}

	/**
	 * @return
	 */
	protected Transition onFinishEnd() {
		return onFinish(getDefaultFinishEndStateId());
	}

	/**
	 * @return
	 */
	protected String getDefaultFinishEndStateId() {
		return FlowConstants.FINISH;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onFinishGet(String stateIdPrefix) {
		return onFinish(get(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onFinishSetup(String stateIdPrefix) {
		return onFinish(setup(stateIdPrefix));
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected Transition[] onDefaultEndEvents(String stateId) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() }, stateId);
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	protected Transition[] onDefaultEndEventsView(String stateIdPrefix) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() }, view(stateIdPrefix));
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	protected Transition[] onDefaultEndEventsGet(String stateIdPrefix) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() }, get(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onFinishSave(String stateIdPrefix) {
		return onFinish(save(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onReset(String stateIdPrefix) {
		return onEvent(getResetEventId(), stateIdPrefix);
	}

	/**
	 * @return
	 */
	protected String getResetEventId() {
		return FlowConstants.RESET;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onResume(String stateIdPrefix) {
		return onEvent(getResumeEventId(), stateIdPrefix);
	}

	/**
	 * @return
	 */
	protected String getResumeEventId() {
		return FlowConstants.RESUME;
	}

	/**
	 * @param selectStateIdPrefix
	 * @return
	 */
	protected Transition onSelect(String stateIdPrefix) {
		return onEvent(getSelectEventId(), stateIdPrefix);
	}

	/**
	 * @return
	 */
	protected String getSelectEventId() {
		return FlowConstants.SELECT;
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	protected Transition onSelectGet(String selectStateIdPrefix) {
		return onSelect(get(selectStateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onError(String stateIdPrefix) {
		return onEvent(getErrorEventId(), stateIdPrefix);
	}

	protected Transition onError(String actionName, String stateIdPrefix) {
		return onEvent(actionName, getErrorEventId(), stateIdPrefix);
	}

	protected String getErrorEventId() {
		return FlowConstants.ERROR;
	}

	protected Transition onErrorView(String stateIdPrefix) {
		return onError(view(stateIdPrefix));
	}

	protected String create(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.CREATE);
	}

	protected String get(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.GET);
	}

	protected String load(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.LOAD);
	}

	protected String setup(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SETUP);
	}

	protected String view(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.VIEW);
	}

	protected String set(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SET);
	}

	protected String add(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.ADD);
	}

	protected String save(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SAVE);
	}

	protected String bindAndValidate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.BIND_AND_VALIDATE);
	}

	protected String bind(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.BIND);
	}

	protected String validate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.VALIDATE);
	}

	protected String delete(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.DELETE);
	}

	protected String edit(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.EDIT);
	}

	protected String search(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, FlowConstants.SEARCH);
	}

	protected String buildStateId(String stateIdPrefix, String stateIdSuffix) {
		if (stateIdPrefix.endsWith(stateIdSuffix)) {
			return stateIdPrefix;
		}
		else {
			return join(stateIdPrefix, stateIdSuffix);
		}
	}

	protected String modelMapper(String modelMapperIdPrefix) {
		Assert.notNull(modelMapperIdPrefix, "The model mapper id prefix is required");
		if (!modelMapperIdPrefix.endsWith(MODEL_MAPPER_ID_SUFFIX)) {
			return modelMapperIdPrefix + getQualifierDelimiter() + MODEL_MAPPER_ID_SUFFIX;
		}
		else {
			return modelMapperIdPrefix;
		}
	}

	protected String getDefaultFlowModelMapperId() {
		return modelMapper(getFlow().getId());
	}

	/**
	 * Qualify given suffix with the flow id.
	 */
	protected String qualify(String suffix) {
		return join(flowId(), suffix);
	}

	/**
	 * Join given prefix and suffix, separating them with a delimiter.
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