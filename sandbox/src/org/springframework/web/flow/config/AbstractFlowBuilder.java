/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.ViewState;

/**
 * @author Keith Donald
 */
public abstract class AbstractFlowBuilder extends FlowConstants implements FlowBuilder {

	protected static final String DOT_SEPARATOR = ".";

	private Flow flow;

	private FlowServiceLocator flowServiceLocator;

	private Collection flowExecutionListeners = new ArrayList(6);

	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.add(listener);
	}

	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.addAll(Arrays.asList(listeners));
	}

	public final void init() {
		this.flow = createFlow();
	}

	protected Flow createFlow() {
		return new Flow(flowId());
	}

	protected abstract String flowId();

	public abstract void buildStates();

	public void buildExecutionListeners() {
		Iterator it = flowExecutionListeners.iterator();
		while (it.hasNext()) {
			FlowExecutionListener listener = (FlowExecutionListener)it.next();
			getResult().addFlowExecutionListener(listener);
		}
	}

	public Flow getResult() {
		return getFlow();
	}

	protected Flow getFlow() {
		return flow;
	}
	
	protected void addSubFlowState(String id, String subFlowId, Transition[] transitions) {
		addSubFlowState(id, spawnFlow(subFlowId), transitions);
	}

	protected void addSubFlowState(String id, Flow subFlow, Transition[] transitions) {
		new SubFlowState(flow, id, subFlow, transitions);
	}

	protected void addSubFlowState(String id, String subFlowId, String attributesMapperId,
			String subFlowDefaultFinishStateId) {
		addSubFlowState(id, spawnFlow(subFlowId), useAttributesMapper(attributesMapperId), subFlowDefaultFinishStateId);
	}

	protected void addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			String subFlowDefaultFinishStateId) {
		addSubFlowState(id, subFlow, attributesMapper, new Transition[] { onBack(subFlowDefaultFinishStateId),
				onCancel(subFlowDefaultFinishStateId), onFinish(subFlowDefaultFinishStateId) });
	}

	protected void addSubFlowState(String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
		addSubFlowState(id, spawnFlow(subFlowId), useAttributesMapper(attributesMapperId), transitions);
	}

	protected void addSubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			Transition[] transitions) {
		new SubFlowState(flow, id, subFlow, attributesMapper, transitions);
	}

	// flow config factory methods

	protected Flow spawnFlow(Class flowImplementationClass) {
		return getFlowServiceLocator().getFlow(flowImplementationClass);
	}

	protected Flow spawnFlow(String flowId) {
		return getFlowServiceLocator().getFlow(flowId);
	}

	protected FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	protected Action executeAction(String actionBeanName) throws NoSuchActionBeanException {
		return getFlowServiceLocator().getActionBean(actionBeanName);
	}

	protected Action[] executeActions(String[] actionBeanNames) throws NoSuchActionBeanException {
		Action[] actionBeans = new Action[actionBeanNames.length];
		for (int i = 0; i < actionBeanNames.length; i++) {
			actionBeans[i] = getFlowServiceLocator().getActionBean(actionBeanNames[i]);
		}
		return actionBeans;
	}

	protected Action executeAction(Class actionBeanImplementationClass) {
		return getFlowServiceLocator().getActionBean(actionBeanImplementationClass);
	}

	protected Action[] executeActions(Class[] actionBeanImplementationClasses) throws NoSuchActionBeanException {
		Action[] actionBeans = new Action[actionBeanImplementationClasses.length];
		for (int i = 0; i < actionBeanImplementationClasses.length; i++) {
			actionBeans[i] = getFlowServiceLocator().getActionBean(actionBeanImplementationClasses[i]);
		}
		return actionBeans;
	}

	protected FlowAttributesMapper useAttributesMapper(String attributesMapperBeanNamePrefix) {
		return getFlowServiceLocator().getFlowAttributesMapper(attributesMapper(attributesMapperBeanNamePrefix));
	}

	protected FlowAttributesMapper useAttributesMapper(Class flowAttributesMapperImplementationClass) {
		return getFlowServiceLocator().getFlowAttributesMapper(flowAttributesMapperImplementationClass);
	}

	/**
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
		return new ViewState(flow, view(stateIdPrefix), addViewName(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transitions
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition[] transitions) {
		return new ViewState(flow, view(stateIdPrefix), viewName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, Transition transition) {
		return new ViewState(flow, view(stateIdPrefix), addViewName(stateIdPrefix), transition);
	}

	protected String addViewName(String stateIdPrefix) {
		return view(stateIdPrefix);
	}

	/**
	 * @param stateIdPrefix
	 * @param viewName
	 * @param transition
	 * @return
	 */
	protected ViewState addViewState(String stateIdPrefix, String viewName, Transition transition) {
		return new ViewState(flow, view(stateIdPrefix), viewName, transition);
	}

	/**
	 * @param actionStateId
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, Transition transition) {
		return new ActionState(flow, stateId, executeAction(actionBeanName(stateId)), transition);
	}

	protected String actionBeanName(String stateId) {
		return stateId;
	}

	/**
	 * @param actionStateId
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action actionBean, Transition transition) {
		return new ActionState(flow, stateId, actionBean, transition);
	}

	/**
	 * @param stateId
	 * @param actionBeanName
	 * @param transition
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionBeanName, Transition transition) {
		return new ActionState(flow, stateId, executeAction(actionBeanName), transition);
	}

	/**
	 * @param stateId
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Transition[] transitions) {
		return new ActionState(flow, stateId, executeAction(actionBeanName(stateId)), transitions);
	}

	/**
	 * @param stateId
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action actionBean, Transition[] transitions) {
		return new ActionState(flow, stateId, actionBean, transitions);
	}

	/**
	 * @param stateId
	 * @param actionBeanName
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String actionBeanName, Transition[] transitions) {
		return new ActionState(flow, stateId, executeAction(actionBeanName), transitions);
	}

	/**
	 * @param stateId
	 * @param actionBeans
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, Action[] actionBeans, Transition[] transitions) {
		return new ActionState(flow, stateId, actionBeans, transitions);
	}

	/**
	 * @param stateId
	 * @param actionBeanNames
	 * @param transitions
	 * @return
	 */
	protected ActionState addActionState(String stateId, String[] actionBeanNames, Transition[] transitions) {
		return new ActionState(flow, stateId, executeActions(actionBeanNames), transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action actionBean) {
		return addCreateState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addCreateState(stateIdPrefix, actionBean, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(add(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	protected ActionState addCreateState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(add(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action actionBean) {
		return addGetState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addGetState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	protected ActionState addGetState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(get(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action actionBean) {
		return addSetState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addSetState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	protected ActionState addSetState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(set(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action actionBean) {
		return addLoadState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addLoadState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param actionBean
	 * @param transitions
	 * @return
	 */
	protected ActionState addLoadState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(load(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action actionBean) {
		return addSearchState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
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
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addSearchState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addSearchState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	protected ActionState addSearchState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(search(stateIdPrefix), actionBean, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix) {
		return addPopulateState(stateIdPrefix, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix, Action actionBean) {
		return addPopulateState(stateIdPrefix, actionBean, onSuccessView(stateIdPrefix));
	}

	/**
	 * @param stateIdPrefix
	 * @param transition
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix, Transition transition) {
		return addPopulateState(stateIdPrefix, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addPopulateState(stateIdPrefix, actionBean, new Transition[] { transition });
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix, Transition[] transitions) {
		return addActionState(populate(stateIdPrefix), transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addPopulateState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(populate(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action actionBean) {
		return addBindAndValidateState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(),
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
	protected ActionState addBindAndValidateState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(bindAndValidate(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action actionBean) {
		return addSaveState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
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
	 * @param actionBean
	 * @param successStateId
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action actionBean, String successStateId) {
		return addSaveState(stateIdPrefix, new Transition[] { onSuccess(successStateId), onErrorView(stateIdPrefix) });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addSaveState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param saveActionBeanName
	 * @param transitions
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, String saveActionBeanName, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), saveActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addSaveState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(save(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action actionBean) {
		return addDeleteState(stateIdPrefix, actionBean,
				new Transition[] { onSuccessEnd(), onErrorView(stateIdPrefix) });
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
	 * @param actionBean
	 * @param successAndErrorStateId
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action actionBean, String successAndErrorStateId) {
		return addDeleteState(stateIdPrefix, actionBean, new Transition[] { onSuccess(successAndErrorStateId),
				onErrorView(successAndErrorStateId) });
	}

	/**
	 * @param stateIdPrefix
	 * @param actionBean
	 * @param transition
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action actionBean, Transition transition) {
		return addDeleteState(stateIdPrefix, actionBean, new Transition[] { transition });
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
	 * @param deleteActionBeanName
	 * @param transitions
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, String deleteActionBeanName, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), deleteActionBeanName, transitions);
	}

	/**
	 * @param stateIdPrefix
	 * @param transitions
	 * @return
	 */
	protected ActionState addDeleteState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(delete(stateIdPrefix), actionBean, transitions);
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
	 * @param actionBean
	 * @return
	 */
	protected ActionState addValidateState(String stateIdPrefix, Action actionBean) {
		return addValidateState(stateIdPrefix, actionBean, new Transition[] { onSuccessEnd(),
				onErrorView(stateIdPrefix) });
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
	protected ActionState addValidateState(String stateIdPrefix, Action actionBean, Transition[] transitions) {
		return addActionState(validate(stateIdPrefix), actionBean, transitions);
	}

	protected EndState addEndState(String endStateId, String viewName) {
		return new EndState(flow, endStateId, viewName);
	}

	protected EndState addEndState(String endStateId) {
		return new EndState(flow, endStateId);
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
	 * @return
	 */
	protected String getSuccessEventId() {
		return SUCCESS;
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
	protected Transition onSuccessPopulate(String stateIdPrefix) {
		return onSuccess(populate(stateIdPrefix));
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
		return SUBMIT;
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
		return SEARCH;
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
		return EDIT;
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
		return BACK;
	}

	/**
	 * @param stateIdPrefix
	 * @return
	 */
	protected Transition onBackPopulate(String stateIdPrefix) {
		return onBack(populate(stateIdPrefix));
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
		return CANCEL;
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
		return BACK;
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
		return CANCEL;
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
		return FINISH;
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
		return FINISH;
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
	protected Transition onFinishPopulate(String stateIdPrefix) {
		return onFinish(populate(stateIdPrefix));
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
		return RESET;
	}

	protected Transition onResume(String stateIdPrefix) {
		return onEvent(getResumeEventId(), stateIdPrefix);
	}

	/**
	 * @return
	 */
	protected String getResumeEventId() {
		return RESUME;
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
		return SELECT;
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
	 * @return
	 */
	protected String getErrorEventId() {
		return ERROR;
	}

	/**
	 * @param viewStateIdPrefix
	 * @return
	 */
	protected Transition onErrorView(String stateIdPrefix) {
		return onError(view(stateIdPrefix));
	}

	protected String create(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, CREATE);
	}

	protected String get(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, GET);
	}

	protected String set(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, SET);
	}

	protected String load(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, LOAD);
	}

	protected String search(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, SEARCH);
	}

	protected String populate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, POPULATE);
	}

	protected String view(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, VIEW);
	}

	protected String add(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, ADD);
	}

	protected String save(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, SAVE);
	}

	protected String bindAndValidate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, BIND_AND_VALIDATE);
	}

	protected String bind(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, BIND);
	}

	protected String validate(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, VALIDATE);
	}

	protected String delete(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, DELETE);
	}

	protected String edit(String stateIdPrefix) {
		return buildStateId(stateIdPrefix, EDIT);
	}

	/**
	 * @param stateIdPrefix
	 * @param stateIdSuffix
	 * @return
	 */
	protected String buildStateId(String stateIdPrefix, String stateIdSuffix) {
		return stateIdPrefix + DOT_SEPARATOR + stateIdSuffix;
	}

	/**
	 * @param attributesMapperBeanNamePrefix
	 * @return
	 */
	protected String attributesMapper(String attributesMapperBeanNamePrefix) {
		if (!attributesMapperBeanNamePrefix.endsWith(ATTRIBUTES_MAPPER_ID_SUFFIX)) {
			return attributesMapperBeanNamePrefix + DOT_SEPARATOR + ATTRIBUTES_MAPPER_ID_SUFFIX;
		}
		else {
			return attributesMapperBeanNamePrefix;
		}
	}

	/**
	 * @return
	 */
	protected String getDefaultFlowAttributesMapperId() {
		return attributesMapper(flow.getId());
	}

}