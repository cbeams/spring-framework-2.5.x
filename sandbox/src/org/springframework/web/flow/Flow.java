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

	public Flow(String id) {
		this.id = id;
		initFlow();
	}

	public Flow(String id, FlowDao flowDao) {
		this.id = id;
		setFlowDao(flowDao);
		initFlow();
	}

	public Flow(String id, String startStateId, AbstractState[] states) {
		this.id = id;
		addAll(states);
		setStartState(startStateId);
		initFlow();
	}

	public void setFlowDao(FlowDao dao) {
		Assert.notNull(dao, "The flow data access object is required for loading subflows and action beans");
		this.flowDao = dao;
	}

	public void setFlowLifecycleListener(FlowLifecycleListener listener) {
		this.flowLifecycleListener = listener;
	}

	protected void initFlow() {

	}

	protected FlowDao getFlowDao() {
		assertFlowDaoSet();
		return this.flowDao;
	}

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

	public String getId() {
		return id;
	}

	public boolean add(AbstractState state) {
		return addAll(getDefaultStateGroupId(), new AbstractState[] { state });
	}

	/**
	 * @return
	 */
	protected String getDefaultStateGroupId() {
		return StateGroups.DEFAULT_GROUP_ID;
	}

	public boolean addAll(AbstractState[] states) {
		return addAll(getDefaultStateGroupId(), states);
	}

	public boolean add(String groupId, AbstractState state) {
		return addAll(groupId, new AbstractState[] { state });
	}

	public boolean addAll(String groupId, AbstractState state) {
		return addAll(groupId, new AbstractState[] { state });
	}

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

	public boolean addSubFlow(String subFlowId, Transition transition) {
		return add(new SubFlowState(subFlowId, transition));
	}

	public boolean addSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
			String subFlowDefaultFinishStateId) {
		return addSubFlow(subFlowIdSuffix, subFlowAttributesMapperId, new Transition[] {
				onBack(subFlowDefaultFinishStateId), onCancel(subFlowDefaultFinishStateId),
				onFinish(subFlowDefaultFinishStateId) });
	}

	public boolean addSubFlow(String subFlowId, Transition[] transitions) {
		return add(new SubFlowState(subFlowId, transitions));
	}

	public boolean addSubFlow(String subFlowId, String subFlowAttributesMapperId, Transition[] transitions) {
		return add(new SubFlowState(subFlowId, subFlowAttributesMapperId, transitions));
	}

	public boolean addEditSubFlow(String editSubFlowIdSuffix, Transition transition) {
		return addSubFlow(buildEditFlowId(editSubFlowIdSuffix), transition);
	}

	public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
			String subFlowDefaultFinishStateId) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), subFlowAttributesMapperId, subFlowDefaultFinishStateId);
	}

	public boolean addEditSubFlow(String subFlowIdSuffix, Transition[] transitions) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), transitions);
	}

	public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId, Transition[] transitions) {
		return addSubFlow(buildEditFlowId(subFlowIdSuffix), subFlowAttributesMapperId, transitions);
	}

	public static String buildEditFlowId(String suffix) {
		return EDIT + StringUtils.capitalize(suffix);
	}

	public boolean add(StateGroup stateGroup) {
		return this.stateGroups.add(stateGroup);
	}

	public Iterator statesIterator() {
		return this.stateGroups.statesIterator();
	}

	public void setStartState(TransitionableState state) throws NoSuchFlowStateException {
		assertValidState(state);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting start state for flow '" + getId() + "' as '" + state + "'");
		}
		this.startState = new StartState(state);
	}

	public void setStartState(String startStateId) throws NoSuchFlowStateException {
		this.startState = new StartState((ViewState)getRequiredState(startStateId));
	}

	private void assertValidState(AbstractState state) throws NoSuchFlowStateException {
		getRequiredState(state.getId());
	}

	public AbstractState getRequiredState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

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

	public TransitionableState getRequiredTransitionableState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getRequiredState(stateId);
		Assert.state(state.isTransitionable(), "This state '" + stateId + "' of flow '" + getId()
				+ "' must be transitionable");
		return (TransitionableState)state;
	}

	public StartState getStartState() throws IllegalStateException {
		Assert.state(startState != null, "No state has been marked as the start state for this flow '" + getId()
				+ "' -- programmer error?");
		return startState;
	}

	public int getViewStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isViewState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	public int getActionStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isActionState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	public int getSubFlowStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isSubFlowState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	public int getEndStateCount() {
		return new AbstractConstraint() {
			public boolean test(Object o) {
				return ((AbstractState)o).isEndState();
			}
		}.findAll(stateGroups.statesIterator()).size();
	}

	public ViewDescriptor start(FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) throws IllegalStateException {
		if (logger.isDebugEnabled()) {
			logger.debug("A new session for flow '" + getId() + "' was requested; processing...");
		}
		return getStartState().enter(this, sessionExecutionStack, request, response, inputAttributes);
	}

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

	Flow getActiveFlow(FlowSessionExecutionStack sessionExecutionStack) {
		String activeFlowId = sessionExecutionStack.getActiveFlowId();
		if (getId().equals(activeFlowId)) {
			return this;
		}
		else {
			return getFlowDao().getFlow(activeFlowId);
		}
	}

	FlowLifecycleListener getLifecycleListener() {
		return flowLifecycleListener;
	}

	boolean isLifecycleListenerSet() {
		return flowLifecycleListener != null;
	}

	public FlowSession createSession() {
		return new FlowSession(getId(), getStartState().getState().getId());
	}

	public FlowSession createSession(Map input) {
		return new FlowSession(getId(), null, input);
	}

	// flow config factory methods

	public ActionState createActionState(String stateId, Transition transition) {
		return new ActionState(stateId, transition);
	}

	public ActionState createActionState(String stateId, String actionBeanName, Transition transition) {
		return new ActionState(stateId, actionBeanName, transition);
	}

	public ActionState createActionState(String stateId, Transition[] transitions) {
		return new ActionState(stateId, transitions);
	}

	public ActionState createActionState(String stateId, String actionBeanName, Transition[] transitions) {
		return new ActionState(stateId, actionBeanName, transitions);
	}

	public ActionState createCreateState(String stateIdPrefix) {
		return createCreateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	public ActionState createCreateState(String stateIdPrefix, Transition transition) {
		return createCreateState(stateIdPrefix, new Transition[] { transition });
	}

	public ActionState createCreateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(create(stateIdPrefix), transitions);
	}

	public ActionState createGetState(String stateIdPrefix) {
		return createGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	public ActionState createGetState(String stateIdPrefix, Transition transition) {
		return createGetState(stateIdPrefix, new Transition[] { transition });
	}

	public ActionState createGetState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(get(stateIdPrefix), transitions);
	}

	public ActionState createPopulateState(String stateIdPrefix) {
		return createPopulateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	public ActionState createPopulateState(String stateIdPrefix, Transition transition) {
		return createPopulateState(stateIdPrefix, new Transition[] { transition });
	}

	public ActionState createPopulateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(populate(stateIdPrefix), transitions);
	}

	public ViewState createViewState(String stateIdPrefix) {
		return createViewState(stateIdPrefix, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	public ViewState createViewState(String stateIdPrefix, String viewName) {
		return createViewState(stateIdPrefix, viewName, new Transition[] { onBackEnd(), onCancelEnd(),
				onSubmitBindAndValidate(stateIdPrefix) });
	}

	public ViewState createViewState(String stateIdPrefix, Transition transition) {
		return new ViewState(view(stateIdPrefix), transition);
	}

	public ViewState createViewState(String stateIdPrefix, String viewName, Transition transition) {
		return new ViewState(view(stateIdPrefix), viewName, transition);
	}

	public ViewState createViewState(String stateIdPrefix, Transition[] transitions) {
		return new ViewState(view(stateIdPrefix), transitions);
	}

	public ViewState createViewState(String stateIdPrefix, String viewName, Transition[] transitions) {
		return new ViewState(view(stateIdPrefix), viewName, transitions);
	}

	public ActionState createBindAndValidateState(String stateIdPrefix) {
		return createBindAndValidateState(stateIdPrefix,
				new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	public ActionState createBindAndValidateState(String stateIdPrefix, Transition transition) {
		return createBindAndValidateState(stateIdPrefix, new Transition[] { transition });
	}

	public ActionState createBindAndValidateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(bindAndValidate(stateIdPrefix), transitions);
	}

	public ActionState createAddState(String stateIdPrefix) {
		return createAddState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	public ActionState createAddState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), transitions);
	}

	public ActionState createAddState(String stateIdPrefix, String successStateId) {
		return createAddState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	public ActionState createAddState(String stateIdPrefix, String addActionBeanName, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), addActionBeanName, transitions);
	}

	public ActionState createSaveState(String stateIdPrefix) {
		return createSaveState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	public ActionState createSaveState(String stateIdPrefix, String successStateId) {
		return createSaveState(stateIdPrefix,
				new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	public ActionState createSaveState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(save(stateIdPrefix), transitions);
	}

	public ActionState createSaveState(String stateIdPrefix, String saveActionBeanName, Transition[] transitions) {
		return createActionState(add(stateIdPrefix), saveActionBeanName, transitions);
	}

	public ActionState createDeleteState(String stateIdPrefix) {
		return createDeleteState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	public ActionState createDeleteState(String stateIdPrefix, String successAndErrorStateId) {
		return createDeleteState(stateIdPrefix, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	public ActionState createDeleteState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(delete(stateIdPrefix), transitions);
	}

	public ActionState createDeleteState(String stateIdPrefix, String deleteActionBeanName, Transition[] transitions) {
		return createActionState(delete(stateIdPrefix), deleteActionBeanName, transitions);
	}

	public ActionState createValidateState(String stateIdPrefix) {
		return createValidateState(stateIdPrefix, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
	}

	public ActionState createValidateState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(validate(stateIdPrefix), transitions);
	}

	public EndState createFinishEndState() {
		return new EndState(getDefaultSuccessEndStateId());
	}

	public EndState createFinishEndState(String viewName) {
		return new EndState(getDefaultSuccessEndStateId(), viewName);
	}

	public AbstractState createBackEndState() {
		return new EndState(getDefaultBackEndStateId());
	}

	public AbstractState createBackEndState(String backViewName) {
		return new EndState(getDefaultBackEndStateId(), backViewName);
	}

	public EndState createCancelEndState() {
		return new EndState(getDefaultCancelEndStateId());
	}

	public EndState createCancelEndState(String cancelViewName) {
		return new EndState(getDefaultCancelEndStateId(), cancelViewName);
	}

	protected void addDefaultEndStates() {
		add(createCancelEndState());
		add(createBackEndState());
		add(createFinishEndState());
	}

	protected void addDefaultEndStates(String viewName) {
		add(createCancelEndState(viewName));
		add(createBackEndState(viewName));
		add(createFinishEndState(viewName));
	}

	public Transition onEvent(String eventId, String newState) {
		return new Transition(eventId, newState);
	}

	public Transition onSuccess(String successStateId) {
		return onEvent(getSuccessEventId(), successStateId);
	}

	/**
	 * @return
	 */
	public String getSuccessEventId() {
		return SUCCESS;
	}

	public Transition onSuccessGet(String getActionStateIdPrefix) {
		return onSuccess(get(getActionStateIdPrefix));
	}

	public Transition onSuccessPopulate(String populateActionStateIdPrefix) {
		return onSuccess(populate(populateActionStateIdPrefix));
	}

	public Transition onSuccessEdit(String editSubFlowStateIdPrefix) {
		return onSuccess(edit(editSubFlowStateIdPrefix));
	}

	public Transition onSuccessView(String viewStateIdPrefix) {
		return onSuccess(view(viewStateIdPrefix));
	}

	public Transition onSuccessAdd(String addActionStateIdPrefix) {
		return onSuccess(add(addActionStateIdPrefix));
	}

	public Transition onSuccessSave(String saveActionStateIdPrefix) {
		return onSuccess(save(saveActionStateIdPrefix));
	}

	public Transition onSuccessEnd() {
		return onSuccess(getDefaultSuccessEndStateId());
	}

	public Transition onEditEdit(String editSubFlowStateIdPrefix) {
		return onEvent(EDIT, edit(editSubFlowStateIdPrefix));
	}

	public Transition onSubmit(String submitActionStateId) {
		return onEvent(getSubmitEventId(), submitActionStateId);
	}

	/**
	 * @return
	 */
	public String getSubmitEventId() {
		return SUBMIT;
	}

	public Transition onSubmitBindAndValidate(String bindAndValidateStateIdPrefix) {
		return onSubmit(bindAndValidate(bindAndValidateStateIdPrefix));
	}

	public Transition onSubmitEdit(String stateIdPrefix) {
		return onSubmit(edit(stateIdPrefix));
	}

	public Transition onSubmitEnd() {
		return onSubmit(getDefaultBackEndStateId());
	}

	public Transition onSearch(String searchActionStateId) {
		return onEvent(getSearchEventId(), searchActionStateId);
	}

	/**
	 * @return
	 */
	public String getSearchEventId() {
		return SEARCH;
	}

	public Transition onSearchGet(String getSearchResultsActionStateIdPrefix) {
		return onSearch(get(getSearchResultsActionStateIdPrefix));
	}

	public Transition onBack(String backStateId) {
		return onEvent(getBackEventId(), backStateId);
	}

	/**
	 * @return
	 */
	public String getBackEventId() {
		return BACK;
	}

	public Transition onBackPopulate(String populateActionStateIdPrefix) {
		return onBack(populate(populateActionStateIdPrefix));
	}

	public Transition onBackView(String viewActionStateIdPrefix) {
		return onBack(view(viewActionStateIdPrefix));
	}

	public Transition onBackEdit(String editSubFlowStateIdPrefix) {
		return onBack(edit(editSubFlowStateIdPrefix));
	}

	public Transition onBackCancel() {
		return onBack(getDefaultCancelEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultCancelEndStateId() {
		return EndState.DEFAULT_CANCEL_STATE_ID;
	}

	public Transition onBackEnd() {
		return onBack(getDefaultBackEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultBackEndStateId() {
		return EndState.DEFAULT_BACK_STATE_ID;
	}

	public Transition onCancel(String cancelStateId) {
		return onEvent(getCancelEventId(), cancelStateId);
	}

	/**
	 * @return
	 */
	public String getCancelEventId() {
		return CANCEL;
	}

	public Transition onCancelEnd() {
		return onCancel(getDefaultCancelEndStateId());
	}

	public Transition onFinish(String finishStateId) {
		return onEvent(getFinishEventId(), finishStateId);
	}

	/**
	 * @return
	 */
	public String getFinishEventId() {
		return FINISH;
	}

	public Transition onFinishEnd() {
		return onFinish(getDefaultSuccessEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultSuccessEndStateId() {
		return EndState.DEFAULT_FINISH_STATE_ID;
	}

	public Transition onFinishGet(String getActionStateIdPrefix) {
		return onFinish(get(getActionStateIdPrefix));
	}

	public Transition onFinishPopulate(String populateActionStateIdPrefix) {
		return onFinish(populate(populateActionStateIdPrefix));
	}

	public Transition onFinishSave(String saveActionStateIdPrefix) {
		return onFinish(save(saveActionStateIdPrefix));
	}

	public Transition onFinishEdit(String editSubFlowStateIdPrefix) {
		return onFinish(edit(editSubFlowStateIdPrefix));
	}

	public Transition onError(String errorStateIdPrefix) {
		return onEvent(getErrorEventId(), errorStateIdPrefix);
	}

	/**
	 * @return
	 */
	public String getErrorEventId() {
		return ERROR;
	}

	public Transition onErrorView(String viewStateIdPrefix) {
		return onError(view(viewStateIdPrefix));
	}

	public String create(String createActionStateIdPrefix) {
		return buildStateId(createActionStateIdPrefix, CREATE);
	}

	public String get(String getActionStateIdPrefix) {
		return buildStateId(getActionStateIdPrefix, GET);
	}

	public String populate(String populateFormActionStateIdPrefix) {
		return buildStateId(populateFormActionStateIdPrefix, POPULATE);
	}

	public String view(String viewActionStateIdPrefix) {
		return buildStateId(viewActionStateIdPrefix, VIEW);
	}

	public String add(String addActionStateIdPrefix) {
		return buildStateId(addActionStateIdPrefix, ADD);
	}

	public String save(String saveActionStateIdPrefix) {
		return buildStateId(saveActionStateIdPrefix, SAVE);
	}

	public String bindAndValidate(String bindAndValidateStateIdPrefix) {
		return buildStateId(bindAndValidateStateIdPrefix, BIND_AND_VALIDATE);
	}

	public String validate(String validateActionStateIdPrefix) {
		return buildStateId(validateActionStateIdPrefix, VALIDATE);
	}

	public String delete(String deleteActionStateIdPrefix) {
		return buildStateId(deleteActionStateIdPrefix, DELETE);
	}

	public String edit(String editActionStateIdPrefix) {
		return buildStateId(editActionStateIdPrefix, EDIT);
	}

	public String buildStateId(String stateIdPrefix, String stateIdSuffix) {
		return stateIdPrefix + "." + stateIdSuffix;
	}

	public String attributesMapper(String attributesMapperBeanNamePrefix) {
		return attributesMapperBeanNamePrefix + ATTRIBUTES_MAPPER_ID_SUFFIX;
	}

	public String getDefaultFlowAttributesMapperId() {
		return attributesMapper(getId());
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("startState", startState).append("stateGroups",
				stateGroups).toString();
	}

}