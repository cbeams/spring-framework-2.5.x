/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
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

    protected static final String ACTION_BEAN_NAME_SUFFIX = "Action";

    protected static final String CREATE_ACTION_PREFIX = "create";

    protected static final String ADD_ACTION_PREFIX = "add";

    protected static final String REMOVE_ACTION_PREFIX = "remove";

    protected static final String DELETE_ACTION_PREFIX = "delete";

    protected static final String GET_ACTION_PREFIX = "get";

    protected static final String POPULATE_FORM_ACTION_PREFIX = "populate";

    protected static final String VIEW_PREFIX = "view";

    protected static final String SUBMIT_ACTION_PREFIX = "submit";

    protected static final String BIND_AND_VALIDATE_FORM_ACTION_PREFIX = "bindAndValidate";

    protected static final String EDIT_PREFIX = "edit";

    protected static final String VALIDATE_ACTION_PREFIX = "validate";

    protected static final String SEARCH_ACTION_PREFIX = "search";

    protected static final String SAVE_ACTION_PREFIX = "save";

    protected static final String FORM_SUFFIX = "Form";

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

    public boolean addEditSubFlow(String subFlowIdSuffix, Transition transition) {
        return addSubFlow(edit(subFlowIdSuffix), transition);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId,
            String subFlowDefaultFinishStateId) {
        return addSubFlow(edit(subFlowIdSuffix), subFlowAttributesMapperId, subFlowDefaultFinishStateId);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, Transition[] transitions) {
        return addSubFlow(edit(subFlowIdSuffix), transitions);
    }

    public boolean addEditSubFlow(String subFlowIdSuffix, String subFlowAttributesMapperId, Transition[] transitions) {
        return addSubFlow(edit(subFlowIdSuffix), subFlowAttributesMapperId, transitions);
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

    public ActionState createCreateState(String stateIdSuffix) {
        return createCreateState(stateIdSuffix, onSuccessView(stateIdSuffix));
    }

    public ActionState createCreateState(String stateIdSuffix, Transition transition) {
        return createCreateState(stateIdSuffix, new Transition[] { transition });
    }

    public ActionState createCreateState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(buildStateId(CREATE_ACTION_PREFIX, stateIdSuffix), transitions);
    }

    public ActionState createGetState(String stateIdSuffix) {
        return createGetState(stateIdSuffix, onSuccessView(stateIdSuffix));
    }

    public ActionState createGetState(String stateIdSuffix, Transition transition) {
        return createGetState(stateIdSuffix, new Transition[] { transition });
    }

    public ActionState createGetState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(buildStateId(GET_ACTION_PREFIX, stateIdSuffix), transitions);
    }

    public ActionState createPopulateState(String stateIdSuffix) {
        return createPopulateState(stateIdSuffix, onSuccessView(stateIdSuffix));
    }

    public ActionState createPopulateState(String stateIdSuffix, Transition transition) {
        return createPopulateState(stateIdSuffix, new Transition[] { transition });
    }

    public ActionState createPopulateState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(populate(stateIdSuffix), transitions);
    }

    public ViewState createViewState(String stateIdSuffix) {
        return createViewState(stateIdSuffix, new Transition[] { onSubmitBindAndValidate(stateIdSuffix),
                OnEvent.cancel(), OnEvent.back(getBackEndStateId()) });
    }

    public ViewState createViewState(String stateIdSuffix, String submitStateIdSuffix) {
        return createViewState(stateIdSuffix, new Transition[] { onSubmitBindAndValidate(submitStateIdSuffix),
                OnEvent.cancel(), OnEvent.back(getBackEndStateId()) });
    }

    public ViewState createViewState(String stateIdSuffix, Transition transition) {
        return createViewState(stateIdSuffix, new Transition[] { transition });
    }

    public ViewState createViewState(String stateIdSuffix, Transition[] transitions) {
        return new ViewState(buildStateId(VIEW_PREFIX, stateIdSuffix), transitions);
    }

    public ActionState createBindAndValidateState(String stateIdSuffix) {
        return createBindAndValidateState(stateIdSuffix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdSuffix) });
    }

    public ActionState createBindAndValidateState(String stateIdSuffix, Transition transition) {
        return createBindAndValidateState(stateIdSuffix, new Transition[] { transition });
    }

    public ActionState createBindAndValidateState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(buildStateId(BIND_AND_VALIDATE_FORM_ACTION_PREFIX, stateIdSuffix), transitions);
    }

    public ActionState createAddState(String stateIdSuffix) {
        return createAddState(stateIdSuffix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdSuffix) });
    }

    public ActionState createAddState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(add(stateIdSuffix), transitions);
    }

    public ActionState createAddState(String stateIdSuffix, String successStateId) {
        return createAddState(stateIdSuffix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdSuffix) });
    }

    public ActionState createAddState(String stateIdSuffix, String addActionBeanName, Transition[] transitions) {
        ActionState addState = new ActionState(add(stateIdSuffix), addActionBeanName, transitions);
        addState.setUpdateAction(true);
        return addState;
    }

    public ActionState createSaveState(String stateIdSuffix) {
        return createSaveState(stateIdSuffix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdSuffix) });
    }

    public ActionState createSaveState(String stateIdSuffix, String successStateId) {
        return createSaveState(stateIdSuffix,
                new Transition[] { onSuccess(successStateId), onErrorView(stateIdSuffix) });
    }

    public ActionState createSaveState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(save(stateIdSuffix), transitions);
    }

    public ActionState createSaveState(String stateIdSuffix, String saveActionBeanName, Transition[] transitions) {
        ActionState saveState = new ActionState(save(stateIdSuffix), saveActionBeanName, transitions);
        saveState.setUpdateAction(true);
        return saveState;
    }

    public ActionState createDeleteState(String stateIdSuffix) {
        return createDeleteState(stateIdSuffix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdSuffix) });
    }

    public ActionState createDeleteState(String stateIdSuffix, String successAndErrorStateId) {
        return createDeleteState(stateIdSuffix, new Transition[] { onSuccess(successAndErrorStateId),
                onErrorView(successAndErrorStateId) });
    }

    public ActionState createDeleteState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(delete(stateIdSuffix), transitions);
    }

    public ActionState createDeleteState(String stateIdSuffix, String deleteActionBeanName, Transition[] transitions) {
        ActionState saveState = new ActionState(delete(stateIdSuffix), deleteActionBeanName, transitions);
        saveState.setUpdateAction(true);
        return saveState;
    }

    public ActionState createValidateState(String stateIdSuffix) {
        return createValidateState(stateIdSuffix, new Transition[] { onSuccess(getFinishEndStateId()),
                onErrorView(stateIdSuffix) });
    }

    public ActionState createValidateState(String stateIdSuffix, Transition[] transitions) {
        return new ActionState(validate(stateIdSuffix), transitions);
    }

    protected Transition onSuccess(String successStateId) {
        return OnEvent.success(successStateId);
    }

    protected Transition onSuccessGet(String getActionStateIdSuffix) {
        return OnEvent.Success.get(getActionStateIdSuffix);
    }

    protected Transition onSuccessPopulate(String populateActionStateIdSuffix) {
        return OnEvent.Success.populate(populateActionStateIdSuffix);
    }

    protected Transition onSuccessView(String viewStateIdSuffix) {
        return OnEvent.Success.view(viewStateIdSuffix);
    }

    protected Transition onSubmit(String submitStateId) {
        return OnEvent.submit(submitStateId);
    }

    protected Transition onSubmitBindAndValidate(String bindAndValidateStateIdSuffix) {
        return OnEvent.Submit.bindAndValidate(bindAndValidateStateIdSuffix);
    }

    protected Transition onSubmitEnd() {
        return OnEvent.Submit.end();
    }

    protected Transition onSearchGet(String getSearchResultsActionStateIdSuffix) {
        return OnEvent.search(get(getSearchResultsActionStateIdSuffix));
    }

    protected Transition onSuccessEdit(String editSubFlowStateIdSuffix) {
        return OnEvent.Success.edit(editSubFlowStateIdSuffix);
    }

    protected Transition onBack(String backStateId) {
        return OnEvent.back(backStateId);
    }

    protected Transition onBackPopulate(String populateActionStateIdSuffix) {
        return OnEvent.Back.populate(populateActionStateIdSuffix);
    }
    
    protected Transition onBackView(String viewActionStateIdSuffix) {
        return OnEvent.Back.view(viewActionStateIdSuffix);
    }

    protected Transition onBackEdit(String editSubFlowStateIdSuffix) {
        return OnEvent.Back.edit(editSubFlowStateIdSuffix);
    }

    protected Transition onBackCancel() {
        return OnEvent.Back.cancel();
    }

    protected Transition onBackEnd() {
        return OnEvent.Back.end();
    }

    protected Transition onEditEdit(String editSubFlowStateIdSuffix) {
        return OnEvent.edit(edit(editSubFlowStateIdSuffix));
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
    
    protected Transition onFinishGet(String getActionStateIdSuffix) {
        return OnEvent.Finish.get(getActionStateIdSuffix);
    }

    protected Transition onFinishPopulate(String populateActionStateIdSuffix) {
        return OnEvent.Finish.populate(populateActionStateIdSuffix);
    }

    protected Transition onFinishSave(String saveActionStateIdSuffix) {
        return OnEvent.Finish.save(saveActionStateIdSuffix);
    }

    protected Transition onFinishEdit(String editSubFlowStateIdSuffix) {
        return OnEvent.Finish.edit(editSubFlowStateIdSuffix);
    }

    protected Transition onErrorView(String viewStateIdSuffix) {
        return OnEvent.Error.view(viewStateIdSuffix);
    }

    protected Transition onSuccessSave(String saveActionStateIdSuffix) {
        return OnEvent.Success.save(saveActionStateIdSuffix);
    }

    protected Transition onSuccessAdd(String addActionStateIdSuffix) {
        return OnEvent.Success.add(addActionStateIdSuffix);
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

    public Transition bindAndValidate(String stateIdSuffix) {
        return OnEvent.bindAndValidate(buildStateId(BIND_AND_VALIDATE_FORM_ACTION_PREFIX, stateIdSuffix));
    }

    public static String buildStateId(String prefix, String suffix) {
        return prefix + StringUtils.capitalize(suffix);
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

    public static String create(String createActionStateIdSuffix) {
        return buildStateId(CREATE_ACTION_PREFIX, createActionStateIdSuffix);
    }

    public static String get(String getActionStateIdSuffix) {
        return buildStateId(GET_ACTION_PREFIX, getActionStateIdSuffix);
    }

    public static String populate(String populateFormActionStateIdSuffix) {
        return buildStateId(POPULATE_FORM_ACTION_PREFIX, populateFormActionStateIdSuffix + FORM_SUFFIX);
    }
    
    public static String view(String viewActionStateIdSuffix) {
        return buildStateId(VIEW_PREFIX, viewActionStateIdSuffix);
    }

    public static String add(String addActionStateIdSuffix) {
        return buildStateId(ADD_ACTION_PREFIX, addActionStateIdSuffix);
    }

    public static String save(String saveActionStateIdSuffix) {
        return buildStateId(SAVE_ACTION_PREFIX, saveActionStateIdSuffix);
    }

    public static String validate(String validateActionStateIdSuffix) {
        return buildStateId(VALIDATE_ACTION_PREFIX, validateActionStateIdSuffix);
    }

    public static String delete(String deleteActionStateIdSuffix) {
        return buildStateId(DELETE_ACTION_PREFIX, deleteActionStateIdSuffix);
    }

    public static String edit(String editActionStateIdSuffix) {
        return buildStateId(EDIT_PREFIX, editActionStateIdSuffix);
    }

    public static String attributesMapper(String attributesMapperIdPrefix) {
        return attributesMapperIdPrefix + ATTRIBUTES_MAPPER_ID_SUFFIX;
    }

    public static String action(String actionBeanNamePrefix) {
        return actionBeanNamePrefix + ACTION_BEAN_NAME_SUFFIX;
    }

    public String getDefaultFlowAttributesMapperId() {
        return getId() + ATTRIBUTES_MAPPER_ID_SUFFIX;
    }

    public String toString() {
        return new ToStringCreator(this).append("id", id).append("startState", startState).append("stateGroups",
                stateGroups).toString();
    }

}