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
 *      add(createSubmitState(PERSON_DETAILS));
 *      add(createDefaultEndState());
 *   }
 * </pre>
 * 
 * What this does is add 4 states to the "EditPersonDetailsFlow"--a "get action"
 * state (the start state), a "view" state, a "submit action" state, and a end
 * marker state.
 * 
 * The first state, an action state, will be indentified as 'personDetails.get'.
 * This action state will automatically be configured with the following
 * defaults:
 * <ol>
 * <li>A action bean named 'personDetails.get' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will go out to the DB ,
 * load the Person, and put it in the Flow's data model.
 * <li>An "success" transition to a default view state, called
 * 'personDetails.view'. This means if the <code>ActionBean</code> returns a
 * "success" event, the 'viewPersonDetails' state will be transitioned to.
 * <li>It will act as the start state for this flow.
 * </ol>
 * 
 * The second state, a view state, will be identified as 'personDetails.view'.
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called 'viewPersonDetails' - this is the logical name of a
 * view resource. This logical view name gets mapped to a physical view resource
 * (jsp, etc.) by the calling front controller.
 * <li>A "submit" transition to a submit action state, indentified by the
 * default ID 'personDetails.submit'. This means when a 'submit' event is
 * signaled by the view (for example, on a submit button click), the submit
 * action state will be entered and the
 * <code>personDetails.submit</code> <code>ActionBean</code> will be
 * executed. This example assumes personDetails.submit is an
 * <code>ActionBean</code> that does data binding, validation, and save/update
 * DAO invocation.
 * </ol>
 * 
 * The third state, an action state, will be indentified as
 * 'personDetails.submit'. This action state will automatically be configured
 * with the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.submit' - this is the name of the
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
 * @author Colin Sampaleanu
 * @see FlowEventProcessor
 */
public class Flow implements FlowEventProcessor, Serializable {

	private static final long serialVersionUID = 3258695403305513015L;

	public static final String FLOW_SESSION_ID_PARAMETER_NAME = "_flowId";

	public static final String EVENT_ID_PARAMETER_NAME = "_eventId";

	public static final String CURRENT_STATE_PARAMETER_NAME = "_currentStateId";

	/**
	 * The <code>ADD</code> action state/event identifier.
	 */
	public static final String ADD = "add";

	/**
	 * The <code>BACK</code> action state/event identifier.
	 */
	public static final String BACK = "back";

	/**
	 * The <code>BIND_AND_VALIDATE</code> action state/event identifier.
	 */
	public static final String BIND_AND_VALIDATE = "bindAndValidate";

	/**
	 * The <code>CANCEL</code> action state/event identifier.
	 */
	public static final String CANCEL = "cancel";

	/**
	 * The <code>CREATE</code> action state/event identifier.
	 */
	public static final String CREATE = "create";

	/**
	 * The <code>DELETE</code> action state/event identifier.
	 */
	public static final String DELETE = "delete";

	/**
	 * The <code>EDIT</code> action state/event identifier.
	 */
	public static final String EDIT = "edit";

	/**
	 * The <code>ERROR</code> event id
	 */
	public static final String ERROR = "error";

	/**
	 * The <code>FINISH</code> action state/event identifier.
	 */
	public static final String FINISH = "finish";

	/**
	 * The <code>GET</code> action state/event identifier.
	 */
	public static final String GET = "get";

	/**
	 * The <code>SET</code> action state/event identifier.
	 */
	public static final String SET = "set";

	/**
	 * The <code>LOAD</code> event identifier.
	 */
	public static final String LOAD = "load";

	/**
	 * The <code>FIND</code> action state/event identifier.
	 */
	public static final String FIND = "find";

	/**
	 * The <code>LINK</code> action state/event identifier.
	 */
	public static final String LINK = "link";

	/**
	 * The <code>REMOVE</code> action state/event identifier.
	 */
	public static final String REMOVE = "remove";

	/**
	 * The <code>POPULATE</code> form action state/event identifier.
	 */
	public static final String POPULATE = "populate";

	/**
	 * The <code>RESET</code> action state/event identifier.
	 */
	public static final String RESET = "reset";

	/**
	 * The <code>RESUME</code> action state/event identifier.
	 */
	public static final String RESUME = "resume";

	/**
	 * The <code>SAVE</code> action state/event identifier.
	 */
	public static final String SAVE = "save";

	/**
	 * The <code>SEARCH</code> action state/event identifier.
	 */
	public static final String SEARCH = "search";

	/**
	 * The <code>SUCCESS</code> action state/event identifier.
	 */
	public static final String SUCCESS = "success";

	/**
	 * The <code>SUBMIT</code> action state/event identifier.
	 */
	public static final String SUBMIT = "submit";

	/**
	 * The <code>UNLINK</code> action state/event identifier.
	 */
	public static final String UNLINK = "unlink";

	/**
	 * The <code>BIND</code> action state/event identifier.
	 */
	public static final String BIND = "bind";

	/**
	 * The <code>VALIDATE</code> action state/event identifier.
	 */
	public static final String VALIDATE = "validate";

	/**
	 * The <code>VIEW</code> view state identifier.
	 */
	public static final String VIEW = "view";

	/**
	 * The <code>SELECT</code> event identifier.
	 */
	public static final String SELECT = "select";

	/**
	 * The default <code>ATTRIBUTES_MAPPER_ID_SUFFIX</code>
	 */
	public static final String ATTRIBUTES_MAPPER_ID_SUFFIX = "attributesMapper";

	protected final Log logger = LogFactory.getLog(getClass());

	private String id;

	private StartState startState;

	private StateGroups stateGroups = new StateGroups();

	private transient FlowDao flowDao;

	private transient FlowLifecycleListener flowLifecycleListener;

	protected Flow() {

	}

	/**
	 * @param id
	 */
	public Flow(String id) {
		setId(id);
	}

	/**
	 * @param id
	 * @param flowDao
	 */
	public Flow(String id, FlowDao flowDao) {
		setId(id);
		setFlowDao(flowDao);
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param states
	 */
	public Flow(String id, String startStateId, FlowDao flowDao, AbstractState[] states) {
		setId(id);
		setFlowDao(flowDao);
		addAll(states);
		setStartState(startStateId);
	}

	protected void setId(String id) {
		Assert.notNull("The flow id is required");
		this.id = id;
	}

	/**
	 * @param dao
	 */
	public void setFlowDao(FlowDao dao) {
		Assert
				.notNull(dao,
						"The flow data access object is required for loading subflows, action state action beans, and attribute mappers");
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
	public FlowDao getFlowDao() {
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
	 * @param subFlowStateId
	 * @param transition
	 * @return
	 */
	public boolean addSubFlowState(String id, Transition[] transitions) {
		return add(new SubFlowState(id, transitions));
	}

	/**
	 * @param subFlowStateId
	 * @param transition
	 * @return
	 */
	public boolean addSubFlowState(String id, Flow subFlow, Transition[] transitions) {
		return add(new SubFlowState(id, subFlow, transitions));
	}

	/**
	 * @param id
	 * @param attributesMapperId
	 * @param subFlowDefaultFinishStateId
	 * @return
	 */
	public boolean addSubFlowState(String id, String attributesMapperId, String subFlowDefaultFinishStateId) {
		return addSubFlowState(id, attributesMapperId, new Transition[] { onBack(subFlowDefaultFinishStateId),
				onCancel(subFlowDefaultFinishStateId), onFinish(subFlowDefaultFinishStateId) });
	}


	/**
	 * @param id
	 * @param subFlow
	 * @param attributesMapper
	 * @param subFlowDefaultFinishStateId
	 * @return
	 */
	public boolean addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			String subFlowDefaultFinishStateId) {
		return addSubFlowState(id, subFlow, attributesMapper, new Transition[] { onBack(subFlowDefaultFinishStateId),
				onCancel(subFlowDefaultFinishStateId), onFinish(subFlowDefaultFinishStateId) });
	}

	/**
	 * @param subFlowStateId
	 * @param attributesMapperId
	 * @param transitions
	 * @return
	 */
	public boolean addSubFlowState(String id, String attributesMapperId, Transition[] transitions) {
		return add(new SubFlowState(id, attributesMapperId, transitions));
	}

	/**
	 * @param id
	 * @param subFlow
	 * @param attributesMapper
	 * @param transitions
	 * @return
	 */
	public boolean addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			Transition[] transitions) {
		return add(new SubFlowState(id, subFlow, attributesMapper, transitions));
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
	public FlowSessionExecutionStartResult start(HttpServletRequest request, HttpServletResponse response,
			Map inputAttributes) throws IllegalStateException {
		if (logger.isDebugEnabled()) {
			logger.debug("A new session for flow '" + getId() + "' was requested; processing...");
		}
		return getStartState().enter(this, request, response, inputAttributes);
	}

	/*
	 * see #FlowEventProcessor.execute
	 */
	public ViewDescriptor execute(String eventId, String stateId, FlowSessionExecutionInfo sessionExecution,
			HttpServletRequest request, HttpServletResponse response) throws FlowNavigationException {
		Assert.isTrue(sessionExecution.isActive(),
				"The currently executing flow stack is not active - this should not happen");
		Flow activeFlow = getActiveFlow(sessionExecution);
		TransitionableState state = activeFlow.getRequiredTransitionableState(stateId);
		ViewDescriptor viewDescriptor = state.execute(eventId, activeFlow, (FlowSessionExecutionStack)sessionExecution,
				request, response);
		return viewDescriptor;
	}

	// javadoc in superclass
	public FlowSessionExecutionStartResult resume(String stateId, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) throws IllegalStateException {
		if (logger.isDebugEnabled()) {
			logger.debug("A new session resuming in state '" + stateId + "' for flow '" + getId()
					+ "' was requested; processing...");
		}
		TransitionableState state = getRequiredTransitionableState(stateId);
		return new StartState(state).enter(this, request, response, inputAttributes);
	}

	/**
	 * @param sessionExecutionStack
	 * @param request
	 * @param response
	 * @param subFlowAttributes
	 * @return
	 */
	public ViewDescriptor spawnIn(FlowSessionExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) {
		return getStartState().enter(this, sessionExecution, request, response, inputAttributes);
	}

	/**
	 * @param sessionExecutionStack
	 * @param request
	 * @param response
	 * @param subFlowAttributes
	 * @return
	 */
	public ViewDescriptor spawnIn(FlowSessionExecutionStack sessionExecution, String stateId,
			HttpServletRequest request, HttpServletResponse response, Map inputAttributes) {
		TransitionableState state = getRequiredTransitionableState(stateId);
		return new StartState(state).enter(this, sessionExecution, request, response, inputAttributes);
	}

	/**
	 * @param sessionExecutionStack
	 * @return
	 */
	Flow getActiveFlow(FlowSessionExecutionInfo sessionExecution) {
		String activeFlowId = sessionExecution.getActiveFlowId();
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
	protected FlowLifecycleListener getLifecycleListener() {
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
	 * @param actionStateId
	 * @param transition
	 * @return
	 */
	public ActionState createActionState(String actionStateId, Transition transition) {
		return new ActionState(actionStateId, transition);
	}

	/**
	 * @param actionStateId
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createActionState(String actionStateId, ActionBean actionBean, Transition transition) {
		return new ActionState(actionStateId, actionBean, transition);
	}

	/**
	 * @param actionStateId
	 * @param actionBeanName
	 * @param transition
	 * @return
	 */
	public ActionState createActionState(String actionStateId, String actionBeanName, Transition transition) {
		return new ActionState(actionStateId, actionBeanName, transition);
	}

	/**
	 * @param actionStateId
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String actionStateId, Transition[] transitions) {
		return new ActionState(actionStateId, transitions);
	}

	/**
	 * @param actionStateId
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String actionStateId, ActionBean actionBean, Transition[] transitions) {
		return new ActionState(actionStateId, actionBean, transitions);
	}

	/**
	 * @param actionStateId
	 * @param actionBeanName
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String actionStateId, String actionBeanName, Transition[] transitions) {
		return new ActionState(actionStateId, actionBeanName, transitions);
	}

	/**
	 * @param actionStateId
	 * @param actionBeans
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String actionStateId, ActionBean[] actionBeans, Transition[] transitions) {
		return new ActionState(actionStateId, actionBeans, transitions);
	}

	/**
	 * @param actionStateId
	 * @param actionBeanNames
	 * @param transitions
	 * @return
	 */
	public ActionState createActionState(String actionStateId, String[] actionBeanNames, Transition[] transitions) {
		return new ActionState(actionStateId, actionBeanNames, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix, ActionBean actionBean) {
		return createCreateState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createCreateState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	public ActionState createCreateState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(create(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix, ActionBean actionBean) {
		return createGetState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createGetState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	public ActionState createGetState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(get(stateIdPrefix), actionBean, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix) {
		return createSetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix, ActionBean actionBean) {
		return createSetState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix, Transition transition) {
		return createSetState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createSetState(stateIdPrefix, actionBean, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(set(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	public ActionState createSetState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(set(stateIdPrefix), actionBean, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix) {
		return createGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix, ActionBean actionBean) {
		return createLoadState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix, Transition transition) {
		return createLoadState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createLoadState(stateIdPrefix, actionBean, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(load(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	public ActionState createLoadState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(load(stateIdPrefix), actionBean, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix) {
		return createSearchState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix, ActionBean actionBean) {
		return createSearchState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix, Transition transition) {
		return createSearchState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createSearchState(stateIdPrefix, actionBean, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix, Transition[] transitions) {
		return createActionState(search(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createSearchState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(search(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix, ActionBean actionBean) {
		return createPopulateState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createPopulateState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param transitions
	 * @return
	 */
	public ActionState createPopulateState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(populate(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createBindAndValidateState(String stateIdPrefix, ActionBean actionBean) {
		return createBindAndValidateState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(),
				onErrorView(stateIdPrefix) });
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
	 * @param transitions
	 * @return
	 */
	public ActionState createBindAndValidateState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(bindAndValidate(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, ActionBean actionBean) {
		return createSaveState(stateIdPrefix, actionBean,
				new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
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
	 * @param actionBean
	 * @param successStateId
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, ActionBean actionBean, String successStateId) {
		return createSaveState(stateIdPrefix,
				new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createSaveState(stateIdPrefix, actionBean, new Transition[] { transition });
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
		return createActionState(save(stateIdPrefix), saveActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createSaveState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(save(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, ActionBean actionBean) {
		return createDeleteState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(),
				onErrorView(stateIdPrefix) });
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
	 * @param actionBean
	 * @param successAndErrorStateId
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, ActionBean actionBean, String successAndErrorStateId) {
		return createDeleteState(stateIdPrefix, actionBean, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, ActionBean actionBean, Transition transition) {
		return createDeleteState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param transitions
	 * @return
	 */
	public ActionState createDeleteState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(delete(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	public ActionState createValidateState(String stateIdPrefix, ActionBean actionBean) {
		return createValidateState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(),
				onErrorView(stateIdPrefix) });
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
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	public ActionState createValidateState(String stateIdPrefix, ActionBean actionBean, Transition[] transitions) {
		return createActionState(validate(stateIdPrefix), actionBean, transitions);
	}

	public EndState createEndState(String endStateId, String viewName) {
		return new EndState(endStateId, viewName);
	}

	public EndState createEndState(String endStateId) {
		return new EndState(endStateId);
	}

	/**
	 * @return
	 */
	public EndState createFinishEndState() {
		return createEndState(getDefaultFinishEndStateId());
	}

	/**
	 * @param viewName
	 * @return
	 */
	public EndState createFinishEndState(String viewName) {
		return createEndState(getDefaultFinishEndStateId(), viewName);
	}

	/**
	 * @return
	 */
	public EndState createBackEndState() {
		return createEndState(getDefaultBackEndStateId());
	}

	/**
	 * @param backViewName
	 * @return
	 */
	public EndState createBackEndState(String viewName) {
		return createEndState(getDefaultBackEndStateId(), viewName);
	}

	/**
	 * @return
	 */
	public EndState createCancelEndState() {
		return createEndState(getDefaultCancelEndStateId());
	}

	/**
	 * @param cancelViewName
	 * @return
	 */
	public EndState createCancelEndState(String viewName) {
		return createEndState(getDefaultCancelEndStateId(), viewName);
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
	 * @param eventId
	 * @param newState
	 * @return
	 */
	public Transition[] onEvents(String[] eventIds, String newState) {
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
	 * @param searchActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessSearch(String searchActionStateIdPrefix) {
		return onSuccess(search(searchActionStateIdPrefix));
	}

	/**
	 * @param populateActionStateIdPrefix
	 * @return
	 */
	public Transition onSuccessPopulate(String populateActionStateIdPrefix) {
		return onSuccess(populate(populateActionStateIdPrefix));
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
		return onSuccess(getDefaultFinishEndStateId());
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
	 * @return
	 */
	public Transition onSubmitEnd() {
		return onSubmit(getDefaultFinishEndStateId());
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
	 * @param successStateId
	 * @return
	 */
	public Transition onEdit(String editStateId) {
		return onEvent(getEditEventId(), editStateId);
	}

	/**
	 * @return
	 */
	protected String getEditEventId() {
		return EDIT;
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
		return onFinish(getDefaultFinishEndStateId());
	}

	/**
	 * @return
	 */
	public String getDefaultFinishEndStateId() {
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
	 * @param stateId
	 * @return
	 */
	public Transition[] onDefaultEndEvents(String stateId) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() }, stateId);
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	public Transition[] onDefaultEndEventsView(String viewStateIdPrefix) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() },
				view(viewStateIdPrefix));
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	public Transition[] onDefaultEndEventsGet(String getStateIdPrefix) {
		return onEvents(new String[] { getBackEventId(), getCancelEventId(), getFinishEventId() },
				get(getStateIdPrefix));
	}

	/**
	 * @param saveActionStateIdPrefix
	 * @return
	 */
	public Transition onFinishSave(String saveActionStateIdPrefix) {
		return onFinish(save(saveActionStateIdPrefix));
	}

	public Transition onReset(String resetStateIdPrefix) {
		return onEvent(getResetEventId(), resetStateIdPrefix);
	}

	/**
	 * @return
	 */
	public String getResetEventId() {
		return RESET;
	}

	public Transition onResume(String resumeStateIdPrefix) {
		return onEvent(getResumeEventId(), resumeStateIdPrefix);
	}

	/**
	 * @return
	 */
	public String getResumeEventId() {
		return RESUME;
	}

	/**
	 * @param selectStateIdPrefix
	 * @return
	 */
	public Transition onSelect(String selectStateIdPrefix) {
		return onEvent(getSelectEventId(), selectStateIdPrefix);
	}

	/**
	 * @return
	 */
	public String getSelectEventId() {
		return SELECT;
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	public Transition onSelectGet(String selectStateIdPrefix) {
		return onSelect(get(selectStateIdPrefix));
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
	 * @param setActionStateIdPrefix
	 * @return
	 */
	public String set(String getActionStateIdPrefix) {
		return buildStateId(getActionStateIdPrefix, SET);
	}

	/**
	 * @param getActionStateIdPrefix
	 * @return
	 */
	public String load(String loadActionStateIdPrefix) {
		return buildStateId(loadActionStateIdPrefix, GET);
	}

	/**
	 * @param searchActionStateIdPrefix
	 * @return
	 */
	public String search(String getActionStateIdPrefix) {
		return buildStateId(getActionStateIdPrefix, SEARCH);
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

	public String bind(String bindActionStateIdPrefix) {
		return buildStateId(bindActionStateIdPrefix, BIND);
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
		if (!attributesMapperBeanNamePrefix.endsWith(ATTRIBUTES_MAPPER_ID_SUFFIX)) {
			return attributesMapperBeanNamePrefix + "." + ATTRIBUTES_MAPPER_ID_SUFFIX;
		}
		else {
			return attributesMapperBeanNamePrefix;
		}
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