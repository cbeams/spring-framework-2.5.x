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

import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.ViewState;

/**
 * Base class for flow builders that programmatically build flows.
 * 
 * <p>
 * To give you an example of what a web flow definition might look like, the
 * following piece of java code defines a web flow equivalent to the work flow
 * implemented by Spring MVC's simple form controller:
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
 *       addGetState(PERSON_DETAILS));
 *       addViewState(PERSON_DETAILS));
 *       addBindAndValidateState(PERSON_DETAILS));
 *       addDefaultEndState());
 *   }
 * </pre>
 * 
 * What this java-based FlowBuilder implementation does is add 4 states to the
 * "personDetails" flow -- a "get action" state (the start state), a "view"
 * state, a "bind and validate" action state, and a end marker state.
 * 
 * The first state, an action state, will be assigned the indentifier as
 * 'personDetails.get'. This action state will automatically be configured with
 * the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.get' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will go out to the
 * DB, load the Person, and put it in the Flow's data model.
 * <li>An "success" transition to a default view state, called
 * 'personDetails.view'. This means when <code>ActionBean</code> returns a
 * "success" result event (aka outcome), the 'viewPersonDetails' state will be
 * transitioned to.
 * <li>It will act as the start state for this flow.
 * </ol>
 * 
 * The second state, a view state, will be identified as 'personDetails.view'.
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called 'personDetails.view' - this is the logical name of a
 * view resource. This logical view name gets mapped to a physical view resource
 * (jsp, etc.) by the calling front controller.
 * <li>A "submit" transition to a bind and validate action state, indentified
 * by the default ID 'personDetails.bindAndValidate'. This means when a 'submit'
 * event is signaled by the view (for example, on a submit button click), the
 * bindAndValidate action state will be entered and the '
 * <code>personDetails.bindAndValidate</code>'<code>ActionBean</code> will
 * be executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as
 * 'personDetails.bindAndValidate'. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.bindAndValidate' - this is the name
 * of the <code>ActionBean</code> instance that will execute when this state
 * is entered. In this example, the <code>ActionBean</code> will bind form
 * input to a backing Person form object, validate it, and update the DB.
 * <li>A "success" transition to a default end state, called 'finish'. This
 * means if the <code>ActionBean</code> returns a "success" event, the
 * 'finish' end state will be transitioned to and the flow will terminate.
 * </ol>
 * 
 * The fourth and last state, a end state, will be indentified with the default
 * end state ID 'finish'. This end state is a marker that signals the end of the
 * flow. When entered, the flow session terminates, and if this flow is acting
 * as a root flow in the current flow execution, any flow-allocated resources
 * will be cleaned up. An end state can optionally be configured with a logical
 * view name to forward to when entered. It will also trigger a state transition
 * in a resuming parent flow, if this flow was participating as a spawned
 * 'subflow' within a suspended parent flow.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowBuilder extends BaseFlowBuilder {

	public final void init() throws FlowBuilderException {
		setFlow(createFlow(flowId()));
	}

	/**
	 * Returns the id (name) of the flow built by this builder. Subclasses
	 * should override to return the unique flowId.
	 * 
	 * @return The unique flow id.
	 */
	protected abstract String flowId();

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. The builder will attempt to resolve the Flow definition associated
	 * with the provided subFlowId by querying the FlowServiceLocator.
	 * @param id the state id
	 * @param subFlowId the id of the Flow definition to retieve (could also be
	 *        the ID of a FlowFactoryBean that produces the Flow)
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, String subFlowId, Transition[] transitions) {
		addSubFlowState(id, spawnFlow(subFlowId), transitions);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id
	 * @param subFlow the flow to be used as a subflow
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, Flow subFlow, Transition[] transitions) {
		new SubFlowState(getFlow(), id, subFlow, transitions);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. The sub flow state ID will also be treated as the id of the sub flow
	 * definition, to be used for retrieval by the FlowServiceLocator.
	 * 
	 * The retrieved subflow definition must also be built by the specified
	 * FlowBuilder implementation, or an exception is thrown. This allows for
	 * easy navigation to sub flow creation logic from within the parent flow
	 * definition, and validates that a particular build implementation does
	 * indeed produce the subflow.
	 * @param id the state id, as well as the subflow id
	 * @param flowBuilderImplementation The flow builder implementation
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, Class flowBuilderImplementation, Transition[] transitions) {
		new SubFlowState(getFlow(), id, spawnFlow(id, flowBuilderImplementation), transitions);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. This builder will attempt to resolve the Flow definition associated
	 * with the provided subFlowId by querying the configured
	 * FlowServiceLocator.
	 * @param id the state id
	 * @param subFlowId the id of the Flow definition to retieve and to be
	 *        spawned as a subflow (could also be the ID of a FlowFactoryBean
	 *        that produces the Flow)
	 * @param attributesMapperId The id of the attributes mapper, to map
	 *        attributes between the the flow built by this builder and the sub
	 *        flow
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        sub flow ends (this assumes you always transition to the same
	 *        state regardless of which EndState is reached in the subflow)
	 */
	protected void addSubFlowState(String id, String subFlowId, String attributesMapperId,
			String subFlowDefaultFinishStateId) {
		addSubFlowState(id, spawnFlow(subFlowId), useAttributesMapper(attributesMapperId), subFlowDefaultFinishStateId);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id
	 * @param subFlow the Flow definition to be spawned as a subflow
	 * @param attributesMapperId The id of the attributes mapper, to map
	 *        attributes between the the flow built by this builder and the sub
	 *        flow
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        sub flow ends (this assumes you always transition to the same
	 *        state regardless of which EndState is reached in the subflow)
	 */
	protected void addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			String subFlowDefaultFinishStateId) {
		addSubFlowState(id, subFlow, attributesMapper, new Transition[] { onAnyEvent(subFlowDefaultFinishStateId) });
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. The sub flow state ID will also be treated as the id of the sub flow
	 * definition to spawn, to be used for retrieval by the FlowServiceLocator.
	 * 
	 * The retrieved subflow definition must be built by the specified
	 * FlowBuilder implementation or an exception is thrown. This allows for
	 * easy navigation to sub flow creation logic from within the parent flow
	 * builder definition, and validates that a particular build implementation
	 * does indeed create the subflow.
	 * @param id the state id, as well as the subflow id
	 * @param flowBuilderImplementation The flow builder implementation
	 * @param transitions The eligible set of state transitions
	 * @param subFlowDefaultFinishStateId The state Id to transition to when the
	 *        sub flow ends (this assumes you always transition to the same
	 *        state regardless of which EndState is reached in the subflow)
	 */
	protected void addSubFlowState(String id, Class subFlowBuilderImplementation,
			FlowAttributesMapper attributesMapper, String subFlowDefaultFinishStateId) {
		addSubFlowState(id, spawnFlow(id, subFlowBuilderImplementation), attributesMapper,
				new Transition[] { onAnyEvent(subFlowDefaultFinishStateId) });
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. The builder will attempt to resolve the Flow definition associated
	 * with the provided subFlowId by querying the FlowServiceLocator.
	 * @param id the state id
	 * @param subFlowId the id of the Flow definition to retieve (could also be
	 *        the ID of a FlowFactoryBean that produces the Flow)
	 * @param attributesMapperId The id of the attributes mapper to map
	 *        attributes between the the flow built by this builder and the sub
	 *        flow
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
		addSubFlowState(id, spawnFlow(subFlowId), useAttributesMapper(attributesMapperId), transitions);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID.
	 * @param id the state id
	 * @param subFlow The flow definition to be used as the subflow
	 * @param attributesMapper The attributes mapper to map attributes between
	 *        the the flow built by this builder and the sub flow
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			Transition[] transitions) {
		new SubFlowState(getFlow(), id, subFlow, attributesMapper, transitions);
	}

	/**
	 * Add a subflow state to the flow built by this builder with the specified
	 * ID. The sub flow state ID will also be treated as the id of the sub flow
	 * definition to spawn, to be used for retrieval by the FlowServiceLocator.
	 * 
	 * The retrieved subflow definition must be built by the specified
	 * FlowBuilder implementation or an exception is thrown. This allows for
	 * easy navigation to sub flow creation logic from within the parent flow
	 * builder definition, and validates that a particular build implementation
	 * does indeed create the subflow.
	 * @param id the state id, as well as the subflow id
	 * @param flowBuilderImplementation The flow builder implementation
	 * @param attributesMapper The attributes mapper to map attributes between
	 *        the the flow built by this builder and the sub flow
	 * @param transitions The eligible set of state transitions
	 */
	protected void addSubFlowState(String id, Class subFlowBuilderImplementation,
			FlowAttributesMapper attributesMapper, Transition[] transitions) {
		new SubFlowState(getFlow(), id, spawnFlow(id, subFlowBuilderImplementation), attributesMapper, transitions);
	}

	/**
	 * Request that the flow with the specified flowId be spawned as a subflow
	 * when the sub flow state is entered. Simply resolves the subflow
	 * definition by id and returns it; throwing a fail-fast exception if it
	 * does not exist.
	 * @param flowId The flow definition id
	 * @return The flow to be used as a subflow, this should be passed to a
	 *         addSubFlowState call
	 */
	protected Flow spawnFlow(String flowId) {
		return getFlowServiceLocator().getFlow(flowId);
	}

	/**
	 * Request that the flow with the specified flowId and built by the
	 * specified flow builder implementation be spawned as a subflow when the
	 * sub flow state is entered. Simply resolves the subflow definition by id,
	 * verifies it is built by the specified builder, and returns it; throwing a
	 * fail-fast exception if it does not exist or is build by the wrong
	 * builder.
	 * @param flowId The flow definition id
	 * @param flowBuilderImplementationClass Ther required FlowBuilder
	 *        implementation that must build the sub flow.
	 * @return The flow to be used as a subflow, this should be passed to a
	 *         addSubFlowState call
	 */
	protected Flow spawnFlow(String flowId, Class flowBuilderImplementationClass) {
		return getFlowServiceLocator().getFlow(flowId, flowBuilderImplementationClass);
	}

	/**
	 * Request that the action with the specified name be executed when the
	 * action state being built is entered. Simply looks the action up by name
	 * and returns it.
	 * @param actionName The action name
	 * @return The action
	 * @throws NoSuchActionException
	 */
	protected Action executeAction(String actionName) throws NoSuchActionException {
		return getFlowServiceLocator().getAction(actionName);
	}

	/**
	 * Request that the actions with the specified name be executed in the order
	 * specified when the action state being built is entered. Simply looks the
	 * actions up by name and returns them.
	 * @param actionNames The action names
	 * @return The actions
	 * @throws NoSuchActionException
	 */
	protected Action[] executeActions(String[] actionNames) throws NoSuchActionException {
		Action[] actions = new Action[actionNames.length];
		for (int i = 0; i < actionNames.length; i++) {
			actions[i] = getFlowServiceLocator().getAction(actionNames[i]);
		}
		return actions;
	}

	/**
	 * Request that the actions with the specified implementation be executed
	 * when the action state being built is entered. Looks the action up by
	 * implementation class and returns it.
	 * @param actionImplementationClass The action implementation, must be
	 *        unique
	 * @return The actions The action
	 * @throws NoSuchActionException
	 */
	protected Action executeAction(Class actionImplementationClass) {
		return getFlowServiceLocator().getAction(actionImplementationClass);
	}

	/**
	 * Request that the actions with the specified implementations be executed
	 * in the order specified when the action state being built is entered.
	 * Looks the action up by implementation class and returns it.
	 * @param actionImplementationClasses The action implementations, must be
	 *        unique
	 * @return The actions The actions
	 * @throws NoSuchActionException
	 */
	protected Action[] executeActions(Class[] actionImplementationClasses) throws NoSuchActionException {
		Action[] actions = new Action[actionImplementationClasses.length];
		for (int i = 0; i < actionImplementationClasses.length; i++) {
			actions[i] = getFlowServiceLocator().getAction(actionImplementationClasses[i]);
		}
		return actions;
	}

	/**
	 * Request that the attribute mapper with the specified name prefix be used
	 * to map attributes between a parent flow and a spawning subflow when the
	 * subflow state being constructed is entered.
	 * @param attributesMapperBeanNamePrefix The attribute mapper prefix
	 * @return The attributes mapper
	 */
	protected FlowAttributesMapper useAttributesMapper(String attributesMapperBeanNamePrefix) {
		return getFlowServiceLocator().getFlowAttributesMapper(attributesMapper(attributesMapperBeanNamePrefix));
	}

	/**
	 * Request that the attribute mapper of the specified implementation be used
	 * to map attributes between a parent flow and a spawning subflow when the
	 * subflow state being constructed is entered.
	 * @param flowAttributesMapperImplementationClass
	 * @return The attributes mapper
	 */
	protected FlowAttributesMapper useAttributesMapper(Class flowAttributesMapperImplementationClass) {
		return getFlowServiceLocator().getFlowAttributesMapper(flowAttributesMapperImplementationClass);
	}

	/**
	 * Add a ViewState marker to the flow built by this builder; a marker has a
	 * null viewName and assumes the response has already been written. The
	 * marker notes that control should be returned to the
	 * @param stateIdPrefix
	 * @return
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix) {
		return addViewState(stateIdPrefix, (String)null);
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix, Transition transition) {
		return addViewState(stateIdPrefix, (String)null, transition);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ViewState addViewStateMarker(String stateIdPrefix, Transition[] transitions) {
		return addViewState(stateIdPrefix, (String)null, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix) {
		return addViewState(stateIdPrefix, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName) {
		return addViewState(stateIdPrefix, viewName, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, Transition[] transitions) {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transitions
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition[] transitions) {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, Transition transition) {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName(stateIdPrefix), transition);
	}

	protected String viewName(String stateIdPrefix) {
		return view(stateIdPrefix);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transition
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition transition) {
		return new ViewState(getFlow(), view(stateIdPrefix), viewName, transition);
	}

	/**
	 * @param stateId
	 * @return
	 */
	protected String actionId(String stateId) {
		return stateId;
	}

	/**
	 * @param actionStateId
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, Transition transition) {
		return new ActionState(getFlow(), stateId, executeAction(actionId(stateId)), transition);
	}

	/**
	 * @param stateId
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action action, Transition transition) {
		return new ActionState(getFlow(), stateId, action, transition);
	}

	/**
	 * @param stateId
	 * @param actionName
	 * @param action
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionName, Action action, Transition transition) {
		return new ActionState(getFlow(), stateId, actionName, action, transition);
	}

	/**
	 * @param stateId
	 * @param actionId
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionId, Transition transition) {
		return new ActionState(getFlow(), stateId, executeAction(actionId), transition);
	}

	/**
	 * @param stateId
	 * @param actionName
	 * @param actionId
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionName, String actionId, Transition transition) {
		return new ActionState(getFlow(), stateId, actionName, executeAction(actionId), transition);
	}

	/**
	 * @param stateId
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, executeAction(actionId(stateId)), transitions);
	}

	/**
	 * @param stateId
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action action, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, action, transitions);
	}

	/**
	 * @param stateId
	 * @param actionName
	 * @param action
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionName, Action action, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, actionName, action, transitions);
	}

	/**
	 * @param stateId
	 * @param actionId
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionId, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, executeAction(actionId), transitions);
	}

	/**
	 * @param stateId
	 * @param actionName
	 * @param actionId
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionName, String actionId, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, actionName, executeAction(actionId), transitions);
	}

	/**
	 * @param stateId
	 * @param actions
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action[] actions, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, actions, transitions);
	}

	/**
	 * @param stateId
	 * @param actionNames
	 * @param actions
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String[] actionNames, Action[] actions,
			Transition[] transitions) {
		return new ActionState(getFlow(), stateId, actionNames, actions, transitions);
	}

	/**
	 * @param stateId
	 * @param actionIds
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String[] actionIds, Transition[] transitions) {
		return new ActionState(getFlow(), stateId, executeActions(actionIds), transitions);
	}

	/**
	 * @param stateId
	 * @param actionNames
	 * @param actionIds
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String[] actionNames, String[] actionIds,
			Transition[] transitions) {
		return new ActionState(getFlow(), stateId, actionNames, executeActions(actionIds), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix) {
		return addCreateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param action
	 * @return
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
	 * @param saveActionName
	 * @param transitions
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, String saveActionName, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), saveActionName, transitions);
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
	 * @param deleteActionName
	 * @param transitions
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, String deleteActionName, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), deleteActionName, transitions);
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

	/**
	 * @param actionName
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onError(String actionName, String stateIdPrefix) {
		return onEvent(actionName, getErrorEventId(), stateIdPrefix);
	}

	/**
	 * @return
	 */
	protected String getErrorEventId() {
		return FlowConstants.ERROR;
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
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

	/**
	 * @param stateIdPrefix
	 * @param stateIdSuffix
	 * @return
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
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	protected String join(String prefix, String suffix) {
		return prefix + DOT_SEPARATOR + suffix;
	}

	/**
	 * @param attributesMapperBeanNamePrefix
	 * @return
	 */
	protected String attributesMapper(String attributesMapperBeanNamePrefix) {
		if (!attributesMapperBeanNamePrefix.endsWith(FlowConstants.ATTRIBUTES_MAPPER_ID_SUFFIX)) {
			return attributesMapperBeanNamePrefix + DOT_SEPARATOR + FlowConstants.ATTRIBUTES_MAPPER_ID_SUFFIX;
		}
		else {
			return attributesMapperBeanNamePrefix;
		}
	}

	/**
	 * @return
	 */
	protected String getDefaultFlowAttributesMapperId() {
		return attributesMapper(getFlow().getId());
	}
}