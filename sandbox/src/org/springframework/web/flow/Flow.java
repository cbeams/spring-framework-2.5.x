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

    protected static final String CREATE_ACTION_SUFFIX = "create";

    protected static final String ADD_ACTION_SUFFIX = "add";

    protected static final String REMOVE_ACTION_SUFFIX = "remove";

    protected static final String DELETE_ACTION_SUFFIX = "delete";

    protected static final String GET_ACTION_SUFFIX = "get";

    protected static final String POPULATE_FORM_ACTION_SUFFIX = "populate";

    protected static final String VIEW_SUFFIX = "view";

    protected static final String SUBMIT_ACTION_SUFFIX = "submit";

    protected static final String BIND_AND_VALIDATE_FORM_ACTION_SUFFIX = "bindAndValidate";

    protected static final String EDIT_SUFFIX = "edit";

    protected static final String VALIDATE_ACTION_SUFFIX = "validate";

    protected static final String SEARCH_ACTION_SUFFIX = "search";

    protected static final String SAVE_ACTION_SUFFIX = "save";

    protected static final String ATTRIBUTES_MAPPER_ID_SUFFIX = "AttributesMapper";

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
        return addAll(StateGroups.DEFAULT_GROUP_ID, new AbstractState[] { state });
    }

    public boolean addAll(AbstractState[] states) {
        return addAll(StateGroups.DEFAULT_GROUP_ID, states);
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
    
    public static String buildEditSubflowId(String suffix) {
        return "edit" + StringUtils.capitalize(suffix);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, Transition transition) {
        return addSubFlow(buildEditSubflowId(subFlowIdSuffix), transition);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
            String subFlowDefaultFinishStateId) {
        return addSubFlow(buildEditSubflowId(subFlowIdSuffix), subFlowAttributesMapperId, subFlowDefaultFinishStateId);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, Transition[] transitions) {
        return addSubFlow(buildEditSubflowId(subFlowIdSuffix), transitions);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId, Transition[] transitions) {
        return addSubFlow(buildEditSubflowId(subFlowIdSuffix), subFlowAttributesMapperId, transitions);
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

    public ActionState createCreateState(String stateIdPrefix) {
        return createCreateState(stateIdPrefix, onSuccessView(stateIdPrefix));
    }

    public ActionState createCreateState(String stateIdPrefix, Transition transition) {
        return createCreateState(stateIdPrefix, new Transition[] { transition });
    }

    public ActionState createCreateState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(buildStateId(CREATE_ACTION_SUFFIX, stateIdPrefix), transitions);
    }

    public ActionState createGetState(String stateIdPrefix) {
        return createGetState(stateIdPrefix, onSuccessView(stateIdPrefix));
    }

    public ActionState createGetState(String stateIdPrefix, Transition transition) {
        return createGetState(stateIdPrefix, new Transition[] { transition });
    }

    public ActionState createGetState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(buildStateId(GET_ACTION_SUFFIX, stateIdPrefix), transitions);
    }

    public ActionState createPopulateState(String stateIdPrefix) {
        return createPopulateState(stateIdPrefix, onSuccessView(stateIdPrefix));
    }

    public ActionState createPopulateState(String stateIdPrefix, Transition transition) {
        return createPopulateState(stateIdPrefix, new Transition[] { transition });
    }

    public ActionState createPopulateState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(populate(stateIdPrefix), transitions);
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
        return createBindAndValidateState(stateIdPrefix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdPrefix) });
    }

    public ActionState createBindAndValidateState(String stateIdPrefix, Transition transition) {
        return createBindAndValidateState(stateIdPrefix, new Transition[] { transition });
    }

    public ActionState createBindAndValidateState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(buildStateId(BIND_AND_VALIDATE_FORM_ACTION_SUFFIX, stateIdPrefix), transitions);
    }

    public ActionState createAddState(String stateIdPrefix) {
        return createAddState(stateIdPrefix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdPrefix) });
    }

    public ActionState createAddState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(add(stateIdPrefix), transitions);
    }

    public ActionState createAddState(String stateIdPrefix, String successStateId) {
        return createAddState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
    }

    public ActionState createAddState(String stateIdPrefix, String addActionBeanName, Transition[] transitions) {
        ActionState addState = new ActionState(add(stateIdPrefix), addActionBeanName, transitions);
        addState.setUpdateAction(true);
        return addState;
    }

    public ActionState createSaveState(String stateIdPrefix) {
        return createSaveState(stateIdPrefix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdPrefix) });
    }

    public ActionState createSaveState(String stateIdPrefix, String successStateId) {
        return createSaveState(stateIdPrefix,
                new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
    }

    public ActionState createSaveState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(save(stateIdPrefix), transitions);
    }

    public ActionState createSaveState(String stateIdPrefix, String saveActionBeanName, Transition[] transitions) {
        ActionState saveState = new ActionState(save(stateIdPrefix), saveActionBeanName, transitions);
        saveState.setUpdateAction(true);
        return saveState;
    }

    public ActionState createDeleteState(String stateIdPrefix) {
        return createDeleteState(stateIdPrefix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdPrefix) });
    }

    public ActionState createDeleteState(String stateIdPrefix, String successAndErrorStateId) {
        return createDeleteState(stateIdPrefix, new Transition[] { onSuccess(successAndErrorStateId),
                onErrorView(successAndErrorStateId) });
    }

    public ActionState createDeleteState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(delete(stateIdPrefix), transitions);
    }

    public ActionState createDeleteState(String stateIdPrefix, String deleteActionBeanName, Transition[] transitions) {
        ActionState saveState = new ActionState(delete(stateIdPrefix), deleteActionBeanName, transitions);
        saveState.setUpdateAction(true);
        return saveState;
    }

    public ActionState createValidateState(String stateIdPrefix) {
        return createValidateState(stateIdPrefix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdPrefix) });
    }

    public ActionState createValidateState(String stateIdPrefix, Transition[] transitions) {
        return new ActionState(validate(stateIdPrefix), transitions);
    }

    protected Transition onSuccess(String successStateId) {
        return OnEvent.success(successStateId);
    }

    protected Transition onSuccessGet(String getActionStateIdPrefix) {
        return OnEvent.Success.get(getActionStateIdPrefix);
    }

    protected Transition onSuccessPopulate(String populateActionStateIdPrefix) {
        return OnEvent.Success.populate(populateActionStateIdPrefix);
    }

    protected Transition onSuccessView(String viewStateIdPrefix) {
        return OnEvent.Success.view(viewStateIdPrefix);
    }

    protected Transition onSubmit(String submitStateId) {
        return OnEvent.submit(submitStateId);
    }

    protected Transition onSubmitBindAndValidate(String bindAndValidateStateIdPrefix) {
        return onSubmit(bindAndValidate(bindAndValidateStateIdPrefix));
    }

    protected Transition onSubmitEdit(String stateIdPrefix) {
        return onSubmit(edit(stateIdPrefix));
    }

    protected Transition onSubmitEnd() {
        return onSubmit(EndState.DEFAULT_FINISH_STATE_ID);
    }

    protected Transition onSearchGet(String getSearchResultsActionStateIdPrefix) {
        return OnEvent.search(get(getSearchResultsActionStateIdPrefix));
    }

    protected Transition onSuccessEdit(String editSubFlowStateIdPrefix) {
        return OnEvent.Success.edit(editSubFlowStateIdPrefix);
    }

    protected Transition onBack(String backStateId) {
        return OnEvent.back(backStateId);
    }

    protected Transition onBackPopulate(String populateActionStateIdPrefix) {
        return OnEvent.Back.populate(populateActionStateIdPrefix);
    }

    protected Transition onBackView(String viewActionStateIdPrefix) {
        return OnEvent.Back.view(viewActionStateIdPrefix);
    }

    protected Transition onBackEdit(String editSubFlowStateIdPrefix) {
        return OnEvent.Back.edit(editSubFlowStateIdPrefix);
    }

    protected Transition onBackCancel() {
        return OnEvent.Back.cancel();
    }

    protected Transition onBackEnd() {
        return OnEvent.Back.end();
    }

    protected Transition onEditEdit(String editSubFlowStateIdPrefix) {
        return OnEvent.edit(edit(editSubFlowStateIdPrefix));
    }

    protected Transition onCancel(String cancelStateId) {
        return OnEvent.cancel(cancelStateId);
    }

    protected Transition onCancelEnd() {
        return OnEvent.Cancel.end();
    }

    protected Transition onFinish(String finishStateId) {
        return OnEvent.finish(finishStateId);
    }

    protected Transition onFinishEnd() {
        return OnEvent.Finish.end();
    }

    protected Transition onFinishGet(String getActionStateIdPrefix) {
        return OnEvent.Finish.get(getActionStateIdPrefix);
    }

    protected Transition onFinishPopulate(String populateActionStateIdPrefix) {
        return OnEvent.Finish.populate(populateActionStateIdPrefix);
    }

    protected Transition onFinishSave(String saveActionStateIdPrefix) {
        return OnEvent.Finish.save(saveActionStateIdPrefix);
    }

    protected Transition onFinishEdit(String editSubFlowStateIdPrefix) {
        return OnEvent.Finish.edit(editSubFlowStateIdPrefix);
    }

    protected Transition onErrorView(String viewStateIdPrefix) {
        return OnEvent.Error.view(viewStateIdPrefix);
    }

    protected Transition onSuccessSave(String saveActionStateIdPrefix) {
        return OnEvent.Success.save(saveActionStateIdPrefix);
    }

    protected Transition onSuccessAdd(String addActionStateIdPrefix) {
        return OnEvent.Success.add(addActionStateIdPrefix);
    }

    protected Transition onSuccessEnd() {
        return OnEvent.Success.end();
    }

    public String getBackEndStateId() {
        return EndState.DEFAULT_BACK_STATE_ID;
    }

    public String getCancelEndStateId() {
        return EndState.DEFAULT_CANCEL_STATE_ID;
    }

    public String getFinishEndStateId() {
        return EndState.DEFAULT_FINISH_STATE_ID;
    }

    public static String buildStateId(String suffix, String prefix) {
        return prefix + "." + suffix;
    }

    public EndState createFinishEndState() {
        return new EndState(getFinishEndStateId());
    }

    public EndState createFinishEndState(String viewName) {
        return new EndState(getFinishEndStateId(), viewName);
    }

    protected AbstractState createBackEndState() {
        return new EndState(getBackEndStateId());
    }

    protected AbstractState createBackEndState(String backViewName) {
        return new EndState(getBackEndStateId(), backViewName);
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

    protected AbstractState createCancelEndState() {
        return new EndState(EndState.DEFAULT_CANCEL_STATE_ID);
    }

    protected AbstractState createCancelEndState(String cancelViewName) {
        return new EndState(EndState.DEFAULT_CANCEL_STATE_ID, cancelViewName);
    }

    public static String create(String createActionStateIdPrefix) {
        return buildStateId(CREATE_ACTION_SUFFIX, createActionStateIdPrefix);
    }

    public static String get(String getActionStateIdPrefix) {
        return buildStateId(GET_ACTION_SUFFIX, getActionStateIdPrefix);
    }

    public static String populate(String populateFormActionStateIdPrefix) {
        return buildStateId(POPULATE_FORM_ACTION_SUFFIX, populateFormActionStateIdPrefix);
    }

    public static String view(String viewActionStateIdPrefix) {
        return buildStateId(VIEW_SUFFIX, viewActionStateIdPrefix);
    }

    public static String add(String addActionStateIdPrefix) {
        return buildStateId(ADD_ACTION_SUFFIX, addActionStateIdPrefix);
    }

    public static String save(String saveActionStateIdPrefix) {
        return buildStateId(SAVE_ACTION_SUFFIX, saveActionStateIdPrefix);
    }

    public static String bindAndValidate(String bindAndValidateStateIdPrefix) {
        return buildStateId(BIND_AND_VALIDATE_FORM_ACTION_SUFFIX, bindAndValidateStateIdPrefix);
    }
    
    public static String validate(String validateActionStateIdPrefix) {
        return buildStateId(VALIDATE_ACTION_SUFFIX, validateActionStateIdPrefix);
    }

    public static String delete(String deleteActionStateIdPrefix) {
        return buildStateId(DELETE_ACTION_SUFFIX, deleteActionStateIdPrefix);
    }

    public static String edit(String editActionStateIdPrefix) {
        return buildStateId(EDIT_SUFFIX, editActionStateIdPrefix);
    }
    
    public static String attributesMapper(String attributesMapperIdPrefix) {
        return attributesMapperIdPrefix + ATTRIBUTES_MAPPER_ID_SUFFIX;
    }

    public static String action(String actionBeanNamePrefix) {
        // we no longer append any standard suffix to action names
        // this method is left in for the time being in case we change our minds
        return actionBeanNamePrefix;
    }

    public static Transition onEvent(String eventName, String newState) {
        return new Transition(eventName, newState);
    }

    public String getDefaultFlowAttributesMapperId() {
        return getId() + ATTRIBUTES_MAPPER_ID_SUFFIX;
    }

    public String toString() {
        return new ToStringCreator(this).append("id", id).append("startState", startState).append("stateGroups",
                stateGroups).toString();
    }

}