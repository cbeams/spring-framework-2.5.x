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
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.support.AbstractConstraint;

/**
 * Singleton definition of a web flow.
 * 
 * At a high level, a Flow captures the definition of a logical page flow within
 * a web application. A logical page flow typically fulfills a business process
 * that takes place over a series of steps (modeled as states.)
 * 
 * Structurally, a Flow is composed of a set of states. A state is a point in
 * the flow where something happens; for instance, showing a view, executing an
 * action, or spawning a subflow.
 * 
 * Each state has one or more transitions that are used to move to another
 * state. A transition is triggered by an event.
 * 
 * Each Flow has exactly one start state. A start state is simply a marker for
 * the state the Flow should transition to when a start event is signaled.
 * 
 * When a start event is signaled by a requesting client, a new
 * <code>FlowSession</code> is created, which tracks a single client instance
 * of this flow. A HTTP-session-scoped <code>FlowSessionExecutionStack</code>
 * provides a call stack that tracks the current state of this flow session's
 * execution, including any subflows that have been spawned.
 * 
 * To give you an example of what a web flow definition might look like, the
 * following piece of java code defines a web flow equivalent to the work flow
 * implemented by a simple form controller:
 * <p>
 * 
 * <pre>
 * public class EditPersonDetailsFlow extends Flow {
 *
 *   public static final String PERSON_DETAILS = "personDetails";
 * 
 *   public EditPersonDetailsFlow() {
 *      super("editPersonDetails");
 *   }
 *
 *   protected void init() {
 *      add(createGetState(PERSON_DETAILS));
 *      add(createViewState(PERSON_DETAILS));
 *      add(createSubmitState(PERSON_DETAILS, getDefaultSubmitActionBeanName()));
 *      add(createDefaultEndState());
 *   }
 * </pre>
 * 
 * What this does is add 4 states to the "EditPersonDetailsFlow"--a "get action"
 * state (the start state), a "view" state, a "submit action" state, and a end
 * marker state.
 * 
 * The first state, an action state, will be indentified as 'getPersonDetails'.
 * This action state will automatically be configured with the following
 * defaults:
 * <ol>
 * <li>A action bean named 'getPersonDetailsAction' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will go out to the DB ,
 * load the Person, and put it in the Flow's data model.
 * <li>An "success" transition to a default view state, called
 * 'viewPersonDetails'. This means if the <code>ActionBean</code> returns a
 * "success" event, the 'viewPersonDetails' state will be transitioned to.
 * <li>It will act as the start state for this flow.
 * </ol>
 * 
 * The second state, a view state, will be identified as 'viewPersonDetails'.
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called 'viewPersonDetails' - this is the logical name of a
 * view resource. This logical view name gets mapped to a physical view resource
 * (jsp, etc.) by the calling front controller.
 * <li>A "submit" transition to a submit action state, indentified by the
 * default ID 'submitAction'. This means when a 'submit' event is signaled by
 * the view (for example, on a submit button click), the submit action state
 * will be entered and the <code>submitAction</code> <code>ActionBean</code>
 * will be executed. This example assumes 'submitAction' is a generic
 * <code>ActionBean</code> that does data binding, validation, and save/update
 * DAO invocation.
 * </ol>
 * 
 * The third state, an action state, will be indentified as
 * 'submitPersonDetails'. This action state will automatically be configured
 * with the following defaults:
 * <ol>
 * <li>A action bean named 'submitAction' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will bind form input
 * to a backing Person form object, validate it, and update the DB.
 * <li>A "success" transition to a default end state, called 'finish'. This
 * means if the <code>ActionBean</code> returns a "success" event, the
 * 'finish' end state will be transitioned to.
 * </ol>
 * 
 * The fourth and last state, a end state, will be indentified with the default
 * end state ID 'finish'. This end state is a marker that signals the end of the
 * flow. It can optionally be configured with a logical view name to forward to.
 * It will also trigger a state transition in a resuming parent flow, if this
 * flow was participating as a 'subflow' within a nested flow.
 * 
 * This class is directly instantitable as it is fully configurable for use -
 * either externally or via a specific subclass.
 * 
 * @author Keith Donald
 * @see FlowEventProcessor
 */
public class Flow implements FlowEventProcessor, Serializable {

	private static final long serialVersionUID = 3258695403305513015L;

	public static final String CREATE = "create";

	public static final String ADD = "add";

	public static final String LINK = "link";

	public static final String REMOVE = "remove";

	public static final String UNLINK = "unlink";

	public static final String DELETE = "delete";

	public static final String GET = "get";

	public static final String POPULATE = "populate";

	public static final String VIEW = "view";

	public static final String SUBMIT = "submit";

	public static final String BIND_AND_VALIDATE = "bindAndValidate";

	public static final String EDIT = "edit";

	public static final String VALIDATE = "validate";

	public static final String SEARCH = "search";

	public static final String SAVE = "save";

	public static final String SUCCESS = "success";

	public static final String ERROR = "error";

	public static final String BACK = "back";

	public static final String CANCEL = "cancel";

	public static final String FINISH = "finish";

	public static final String ATTRIBUTES_MAPPER_ID_SUFFIX = "AttributesMapper";

	protected final Log logger = LogFactory.getLog(getClass());

	private String id;

	private StartState startState;

	private StateGroups stateGroups = new StateGroups();

	private transient FlowDao flowDao;

	private transient FlowLifecycleListener flowLifecycleListener;

	/**
	 * @param id
	 */
	public Flow(String id) {
		this.id = id;
		initFlow();
	}

	/**
	 * @param id
	 * @param flowDao
	 */
	public Flow(String id, FlowDao flowDao) {
		this.id = id;
		setFlowDao(flowDao);
		initFlow();
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param states
	 */
	public Flow(String id, String startStateId, AbstractState[] states) {
		this.id = id;
		addAll(states);
		setStartState(startStateId);
		initFlow();
	}

	/**
	 * @param dao
	 */
	public void setFlowDao(FlowDao dao) {
		Assert.notNull(dao, "The flow data access object is required for loading subflows and action beans");
		this.flowDao = dao;
	}

	/**
	 * @param listener
	 */
	public void setFlowLifecycleListener(FlowLifecycleListener listener) {
		this.flowLifecycleListener = listener;
	}

	/**
	 *  
	 */
	protected void initFlow() {

	}

	/**
	 * @return
	 */
	protected FlowDao getFlowDao() {
		assertFlowDaoSet();
		return this.flowDao;
	}

	/**
	 *  
	 */
	private void assertFlowDaoSet() {
		Assert.notNull(flowDao,
				"The flow DAO reference is required to load subflows and action beans - programmer error?");
	}

	/**
	 * @return Returns the listener. A listener is not required.
	 */
	public FlowLifecycleListener getFlowLifecycleListener() {
		return flowLifecycleListener;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param state
	 * @return
	 */
	public boolean add(AbstractState state) {
		return addAll(getDefaultStateGroupId(), new AbstractState[] { state });
	}

	/**
	 * @return
	 */
	protected String getDefaultStateGroupId() {
		return StateGroups.DEFAULT_GROUP_ID;
	}

	/**
	 * @param states
	 * @return
	 */
	public boolean addAll(AbstractState[] states) {
		return addAll(getDefaultStateGroupId(), states);
	}

	/**
	 * @param groupId
	 * @param state
	 * @return
	 */
	public boolean add(String groupId, AbstractState state) {
		return addAll(groupId, new AbstractState[] { state });
	}

	/**
	 * @param groupId
	 * @param state
	 * @return
	 */
	public boolean addAll(String groupId, AbstractState state) {
		return addAll(groupId, new AbstractState[] { state });
	}

	/**
	 * @param groupId
	 * @param states
	 * @return
	 */
	public boolean addAll(String groupId, AbstractState[] states) {
		boolean firstAdd = false;
		if (this.stateGroups.isEmpty()) {
			firstAdd = true;
		}
		boolean changed = this.stateGroups.addAll(groupId, states);
		if (changed && firstAdd) {
			setStartState((TransitionableState)this.stateGroups.statesIterator().next());
		}
		return changed;
	}

	/**
	 * @param subFlowId
	 * @param transition
	 * @return
	 */
	public boolean addSubFlow(String subFlowId, Transition transition) {
		return add(new SubFlowState(subFlowId, transition));
	}

	/**
	 * @param subFlowIdSuffix
	 * @param subFlowAttributesMapperId
	 * @param subFlowDefaultFinishStateId
	 * @return
	 */
	public boolean addSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
			String subFlowDefaultFinishStateId) {
		return addSubFlow(subFlowIdSuffix, subFlowAttributesMapperId, new Transition[] {
				onBack(subFlowDefaultFinishStateId), onCancel(subFlowDefaultFinishStateId),
				onFinish(subFlowDefaultFinishStateId) });
	}

	/**
	 * @param subFlowId
	 * @param transitions
	 * @return
	 */
	public boolean addSubFlow(String subFlowId, Transition[] transitions) {
		return add(new SubFlowState(subFlowId, transitions));
	}

	/**
	 * @param subFlowId
	 * @param subFlowAttributesMapperId
	 * @param transitions
	 * @return
	 */
	public boolean addSubFlow(String subFlowId, String subFlowAttributesMapperId, Transition[] transitions) {
		return add(new SubFlowState(subFlowId, subFlowAttributesMapperId, transitions));
	}

	/**
	 * @param editSubFlowIdSuffix
	 * @param transition
	 * @return
	 */
	public boolean addEditSubFlow(String editSubFlowIdSuffix, Transition transition) {
		return addSubFlow(buildEditFlowId(editSubFlowIdSuffix), transition);
	}

	/**
	 * @param subFlowIdSuffix
	 * @param subFlowAttributesMapperId
	 * @param subFlowDefaultFinishStateId
	 * @return
	 */
	public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
			String subFlowDefaultFinishStateId) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), subFlowAttributesMapperId, subFlowDefaultFinishStateId);
	}

	/**
	 * @param subFlowIdSuffix
	 * @param transitions
	 * @return
	 */
	public boolean addEditSubFlow(String subFlowIdSuffix, Transition[] transitions) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), transitions);
	}

	/**
	 * @param subFlowIdSuffix
	 * @param subFlowAttributesMapperId
	 * @param transitions
	 * @return
	 */
	public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId, Transition[] transitions) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), subFlowAttributesMapperId, transitions);
	}

	/**
	 * @param suffix
	 * @return
	 */
	protected String buildEditFlowId(String suffix) {
		return EDIT + StringUtils.capitalize(suffix);
	}

	/**
	 * @param stateGroup
	 * @return
	 */
	public boolean add(StateGroup stateGroup) {
		return this.stateGroups.add(stateGroup);
	}

	/**
	 * @return
	 */
	public Iterator statesIterator() {
		return this.stateGroups.statesIterator();
	}

	/**
	 * @param state
	 * @throws NoSuchFlowStateException
	 */
	public void setStartState(TransitionableState state) throws NoSuchFlowStateException {
		assertValidState(state);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting start state for flow '" + getId() + "' as '" + state + "'");
		}
		this.startState = new StartState(state);
	}

	/**
	 * @param startStateId
	 * @throws NoSuchFlowStateException
	 */
	public void setStartState(String startStateId) throws NoSuchFlowStateException {
		this.startState = new StartState((ViewState)getRequiredState(startStateId));
	}

	/**
	 * @param state
	 * @throws NoSuchFlowStateException
	 */
	private void assertValidState(AbstractState state) throws NoSuchFlowStateException {
		getRequiredState(state.getId());
	}

	/**
	 * @param stateId
	 * @return
	 * @throws NoSuchFlowStateException
	 */
	public AbstractState getRequiredState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

	/**
	 * @param stateId
	 * @return
	 */
	public AbstractState getState(String stateId) {
		Iterator it = stateGroups.statesIterator();
		while (it.hasNext()) {
			AbstractState state = (AbstractState)it.next();
			if (state.getId().equals(stateId)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * @param stateId
	 * @return
	 * @throws NoSuchFlowStateException
	 */
	public TransitionableState getRequiredTransitionableState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getRequiredState(stateId);
		Assert.state(state.isTransitionable(), "This state '" + stateId + "' of flow '" + getId()
				+ "' must be transitionable");
		return (TransitionableState)state;
	}

	/**
	 * @return
	 * @throws IllegalStateException
	 */
	public StartState getStartState() throws IllegalStateException {
		Assert.state(startState != null, "No state has been marked as the start state for this flow '" + getId()
				+ "' -- programmer error?");
		return startState;
	}

	/**
	 * @return
	 */
	public int getViewStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isViewState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	/**
	 * @return
	 */
	public int getActionStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isActionState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	/**
	 * @return
	 */
	public int getSubFlowStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isSubFlowState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	/**
	 * @return
	 */
	public int getEndStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isEndState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	/*
	 * see #FlowEventProcessor.start
	 */
	public ViewDescriptor start(FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) throws IllegalStateException {
		if (logger.isDebugEnabled()) {
			logger.debug("A new session for flow '" + getId() + "' was requested; processing...");
		}
		return getStartState().enter(this, sessionExecutionStack, request, response, inputAttributes);
	}

	/*
	 * see #FlowEventProcessor.execute
	 */
	public ViewDescriptor execute(String eventId, String stateId, FlowSessionExecutionStack sessionExecutionStack,
			HttpServletRequest request, HttpServletResponse response) throws FlowNavigationException {
		Assert.isTrue(sessionExecutionStack.isActive(),
				"The currently executing flow stack is not active - this should not happen");
		Flow activeFlow = getActiveFlow(sessionExecutionStack);
		TransitionableState currentState = activeFlow.getRequiredTransitionableState(stateId);
		ViewDescriptor viewDescriptor = currentState.execute(eventId, activeFlow, sessionExecutionStack, request,
				response);
		return viewDescriptor;
	}

	/**
	 * @param sessionExecutionStack
	 * @return
	 */
	Flow getActiveFlow(FlowSessionExecutionStack sessionExecutionStack) {
		String activeFlowId = sessionExecutionStack.getActiveFlowId();
		if (getId().equals(activeFlowId)) {
			return this;
		}
		else {
			return getFlowDao().getFlow(activeFlowId);
		}
	}

	/**
	 * @return
	 */
	FlowLifecycleListener getLifecycleListener() {
		return flowLifecycleListener;
	}

	/**
	 * @return
	 */
	public boolean isLifecycleListenerSet() {
		return flowLifecycleListener != null;
	}

	/**
	 * @return
	 */
	protected FlowSession createSession() {
		return new FlowSession(getId(), getStartState().getState().getId());
	}

	/**
	 * @param input
	 * @return
	 */
	protected FlowSession createSession(Map input) {
		return new FlowSession(getId(), null, input);
	}

	// flow config factory methods

	/**
	 * @param stateId
	 * @param transition
	 * @return
	 */
	public ActionState createActionState(String stateId, Transition transition) {
		return new ActionState(stateId, transition);
	}

	/**
	 * @param stateId
	 * @param actionBeanName
	 * @param transition
	 * @return
	 */
	public ActionState createActionState(String stateId, String actionBeanName, Transition transition) {
		return new ActionState(stateId, actionBeanName, transition);
	}

	/**
	 * @param stateId
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String stateId, Transition[] transitions) {
		return new ActionState(stateId, transitions);
	}

	/**
	 * @param stateId
	 * @param actionBeanName
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String stateId, String actionBeanName, Transition[] transitions) {
		return new ActionState(stateId, actionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix) {
		return createCreateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix, Transition transition) {
		return createCreateState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(create(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix) {
		return createGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix, Transition transition) {
		return createGetState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(get(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix) {
		return createPopulateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix, Transition transition) {
		return createPopulateState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(populate(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix) {
		return createViewState(stateIdPrefix, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix, String viewName) {
		return createViewState(stateIdPrefix, viewName, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix, Transition transition) {
		return new ViewState(view(stateIdPrefix), transition);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transition
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix, String viewName, Transition transition) {
		return new ViewState(view(stateIdPrefix), viewName, transition);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix, Transition[] transitions) {
		return new ViewState(view(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transitions
	 * @return
	 */
	public ViewState createViewState(String stateIdPrefix, String viewName, Transition[] transitions) {
		return new ViewState(view(stateIdPrefix), viewName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createBindAndValidateState(String stateIdPrefix) {
		return createBindAndValidateState(stateIdPrefix,
				new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createBindAndValidateState(String stateIdPrefix, Transition transition) {
		return createBindAndValidateState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createBindAndValidateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(bindAndValidate(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createAddState(String stateIdPrefix) {
		return createAddState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createAddState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param successStateId
	 * @return
	 */
	public ActionState createAddState(String stateIdPrefix, String successStateId) {
		return createAddState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param addActionBeanName
	 * @param transitions
	 * @return
	 */
	public ActionState createAddState(String stateIdPrefix, String addActionBeanName, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), addActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix) {
		return createSaveState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param successStateId
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, String successStateId) {
		return createSaveState(stateIdPrefix,
				new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(save(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param saveActionBeanName
	 * @param transitions
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, String saveActionBeanName, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), saveActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix) {
		return createDeleteState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param successAndErrorStateId
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, String successAndErrorStateId) {
		return createDeleteState(stateIdPrefix, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(delete(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param deleteActionBeanName
	 * @param transitions
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, String deleteActionBeanName, Transition[] transitions) {
		return createActionState(delete(stateIdPrefix), deleteActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createValidateState(String stateIdPrefix) {
		return createValidateState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createValidateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(validate(stateIdPrefix), transitions);
	}

	/**
	 * @return
	 */
	public EndState createFinishEndState() {
		return new EndState(getDefaultSuccessEndStateId());
	}

	/**
	 * @param viewName
	 * @return
	 */
	public EndState createFinishEndState(String viewName) {
		return new EndState(getDefaultSuccessEndStateId(), viewName);
	}

	/**
	 * @return
	 */
	public AbstractState createBackEndState() {
		return new EndState(getDefaultBackEndStateId());
	}

	/**
	 * @param backViewName
	 * @return
	 */
	public AbstractState createBackEndState(String backViewName) {
		return new EndState(getDefaultBackEndStateId(), backViewName);
	}

	/**
	 * @return
	 */
	public EndState createCancelEndState() {
		return new EndState(getDefaultCancelEndStateId());
	}

	/**
	 * @param cancelViewName
	 * @return
	 */
	public EndState createCancelEndState(String cancelViewName) {
		return new EndState(getDefaultCancelEndStateId(), cancelViewName);
	}

	/**
	 *  
	 */
	protected void addDefaultEndStates() {
		add(createCancelEndState());
		add(createBackEndState());
		add(createFinishEndState());
	}

	/**
	 * @param viewName
	 */
	protected void addDefaultEndStates(String viewName) {
		add(createCancelEndState(viewName));
		add(createBackEndState(viewName));
		add(createFinishEndState(viewName));
	}

	/**
	 * @param eventId
	 * @param newState
	 * @return
	 */
	public Transition onEvent(String eventId, String newState) {
		return new Transition(eventId, newState);
	}

	/**
	 * @param successStateId
	 * @return
	 */
	public Transition onSuccess(String successStateId) {
		return onEvent(getSuccessEventId(), successStateId);
	}

	/**
	 * @return
	 */
	public String getSuccessEventId() {
		return SUCCESS;
	}

	/**
	 * @param getActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessGet(String getActionStateIdPrefix) {
		return onSuccess(get(getActionStateIdPrefix));
	}

	/**
	 * @param populateActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessPopulate(String populateActionStateIdPrefix) {
		return onSuccess(populate(populateActionStateIdPrefix));
	}

	/**
	 * @param editSubFlowStateIdPrefix
	 * @return
	 */
	public Transition onSuccessEdit(String editSubFlowStateIdPrefix) {
		return onSuccess(edit(editSubFlowStateIdPrefix));
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	public Transition onSuccessView(String viewStateIdPrefix) {
		return onSuccess(view(viewStateIdPrefix));
	}

	/**
	 * @param addActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessAdd(String addActionStateIdPrefix) {
		return onSuccess(add(addActionStateIdPrefix));
	}

	/**
	 * @param saveActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessSave(String saveActionStateIdPrefix) {
		return onSuccess(save(saveActionStateIdPrefix));
	}

	/**
	 * @return
	 */
	public Transition onSuccessEnd() {
		return onSuccess(getDefaultSuccessEndStateId());
	}

	/**
	 * @param editSubFlowStateIdPrefix
	 * @return
	 */
	public Transition onEditEdit(String editSubFlowStateIdPrefix) {
		return onEvent(EDIT, edit(editSubFlowStateIdPrefix));
	}

	/**
	 * @param submitActionStateId
	 * @return
	 */
	public Transition onSubmit(String submitActionStateId) {
		return onEvent(getSubmitEventId(), submitActionStateId);
	}

	/**
	 * @return
	 */
	public String getSubmitEventId() {
		return SUBMIT;
	}

	/**
	 * @param bindAndValidateStateIdPrefix
	 * @return
	 */
	public Transition onSubmitBindAndValidate(String bindAndValidateStateIdPrefix) {
		return onSubmit(bindAndValidate(bindAndValidateStateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public Transition onSubmitEdit(String stateIdPrefix) {
		return onSubmit(edit(stateIdPrefix));
	}

	/**
	 * @return
	 */
	public Transition onSubmitEnd() {
		return onSubmit(getDefaultBackEndStateId());
	}

	/**
	 * @param searchActionStateId
	 * @return
	 */
	public Transition onSearch(String searchActionStateId) {
		return onEvent(getSearchEventId(), searchActionStateId);
	}

	/**
	 * @return
	 */
	public String getSearchEventId() {
		return SEARCH;
	}

	/**
	 * @param getSearchResultsActionStateIdPrefix
	 * @return
	 */
	public Transition onSearchGet(String getSearchResultsActionStateIdPrefix) {
		return onSearch(get(getSearchResultsActionStateIdPrefix));
	}

	/**
	 * @param backStateId
	 * @return
	 */
	public Transition onBack(String backStateId) {
		return onEvent(getBackEventId(), backStateId);
	}

	/**
	 * @return
	 */
	public String getBackEventId() {
		return BACK;
	}

	/**
	 * @param populateActionStateIdPrefix
	 * @return
	 */
	public Transition onBackPopulate(String populateActionStateIdPrefix) {
		return onBack(populate(populateActionStateIdPrefix));
	}

	/**
	 * @param viewActionStateIdPrefix
	 * @return
	 */
	public Transition onBackView(String viewActionStateIdPrefix) {
		return onBack(view(viewActionStateIdPrefix));
	}

	/**
	 * @param editSubFlowStateIdPrefix
	 * @return
	 */
	public Transition onBackEdit(String editSubFlowStateIdPrefix) {
		return onBack(edit(editSubFlowStateIdPrefix));
	}

	/**
	 * @return
	 */
	public Transition onBackCancel() {
		return onBack(getDefaultCancelEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultCancelEndStateId() {
		return EndState.DEFAULT_CANCEL_STATE_ID;
	}

	/**
	 * @return
	 */
	public Transition onBackEnd() {
		return onBack(getDefaultBackEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultBackEndStateId() {
		return EndState.DEFAULT_BACK_STATE_ID;
	}

	/**
	 * @param cancelStateId
	 * @return
	 */
	public Transition onCancel(String cancelStateId) {
		return onEvent(getCancelEventId(), cancelStateId);
	}

	/**
	 * @return
	 */
	public String getCancelEventId() {
		return CANCEL;
	}

	/**
	 * @return
	 */
	public Transition onCancelEnd() {
		return onCancel(getDefaultCancelEndStateId());
	}

	/**
	 * @param finishStateId
	 * @return
	 */
	public Transition onFinish(String finishStateId) {
		return onEvent(getFinishEventId(), finishStateId);
	}

	/**
	 * @return
	 */
	public String getFinishEventId() {
		return FINISH;
	}

	/**
	 * @return
	 */
	public Transition onFinishEnd() {
		return onFinish(getDefaultSuccessEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultSuccessEndStateId() {
		return EndState.DEFAULT_FINISH_STATE_ID;
	}

	/**
	 * @param getActionStateIdPrefix
	 * @return
	 */
	public Transition onFinishGet(String getActionStateIdPrefix) {
		return onFinish(get(getActionStateIdPrefix));
	}

	/**
	 * @param populateActionStateIdPrefix
	 * @return
	 */
	public Transition onFinishPopulate(String populateActionStateIdPrefix) {
		return onFinish(populate(populateActionStateIdPrefix));
	}

	/**
	 * @param saveActionStateIdPrefix
	 * @return
	 */
	public Transition onFinishSave(String saveActionStateIdPrefix) {
		return onFinish(save(saveActionStateIdPrefix));
	}

	/**
	 * @param editSubFlowStateIdPrefix
	 * @return
	 */
	public Transition onFinishEdit(String editSubFlowStateIdPrefix) {
		return onFinish(edit(editSubFlowStateIdPrefix));
	}

	/**
	 * @param errorStateIdPrefix
	 * @return
	 */
	public Transition onError(String errorStateIdPrefix) {
		return onEvent(getErrorEventId(), errorStateIdPrefix);
	}

	/**
	 * @return
	 */
	public String getErrorEventId() {
		return ERROR;
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	public Transition onErrorView(String viewStateIdPrefix) {
		return onError(view(viewStateIdPrefix));
	}

	/**
	 * @param createActionStateIdPrefix
	 * @return
	 */
	public String create(String createActionStateIdPrefix) {
		return buildStateId(createActionStateIdPrefix, CREATE);
	}

	/**
	 * @param getActionStateIdPrefix
	 * @return
	 */
	public String get(String getActionStateIdPrefix) {
		return buildStateId(getActionStateIdPrefix, GET);
	}

	/**
	 * @param populateFormActionStateIdPrefix
	 * @return
	 */
	public String populate(String populateFormActionStateIdPrefix) {
		return buildStateId(populateFormActionStateIdPrefix, POPULATE);
	}

	/**
	 * @param viewActionStateIdPrefix
	 * @return
	 */
	public String view(String viewActionStateIdPrefix) {
		return buildStateId(viewActionStateIdPrefix, VIEW);
	}

	/**
	 * @param addActionStateIdPrefix
	 * @return
	 */
	public String add(String addActionStateIdPrefix) {
		return buildStateId(addActionStateIdPrefix, ADD);
	}

	/**
	 * @param saveActionStateIdPrefix
	 * @return
	 */
	public String save(String saveActionStateIdPrefix) {
		return buildStateId(saveActionStateIdPrefix, SAVE);
	}

	/**
	 * @param bindAndValidateStateIdPrefix
	 * @return
	 */
	public String bindAndValidate(String bindAndValidateStateIdPrefix) {
		return buildStateId(bindAndValidateStateIdPrefix, BIND_AND_VALIDATE);
	}

	/**
	 * @param validateActionStateIdPrefix
	 * @return
	 */
	public String validate(String validateActionStateIdPrefix) {
		return buildStateId(validateActionStateIdPrefix, VALIDATE);
	}

	/**
	 * @param deleteActionStateIdPrefix
	 * @return
	 */
	public String delete(String deleteActionStateIdPrefix) {
		return buildStateId(deleteActionStateIdPrefix, DELETE);
	}

	/**
	 * @param editActionStateIdPrefix
	 * @return
	 */
	public String edit(String editActionStateIdPrefix) {
		return buildStateId(editActionStateIdPrefix, EDIT);
	}

	/**
	 * @param stateIdPrefix
	 * @param stateIdSuffix
	 * @return
	 */
	protected String buildStateId(String stateIdPrefix, String stateIdSuffix) {
		return stateIdPrefix + "." + stateIdSuffix;
	}

	/**
	 * @param attributesMapperBeanNamePrefix
	 * @return
	 */
	public String attributesMapper(String attributesMapperBeanNamePrefix) {
		return attributesMapperBeanNamePrefix + ATTRIBUTES_MAPPER_ID_SUFFIX;
	}

	/**
	 * @return
	 */
	public String getDefaultFlowAttributesMapperId() {
		return attributesMapper(getId());
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("startState", startState).append("stateGroups",
				stateGroups).toString();
	}

}