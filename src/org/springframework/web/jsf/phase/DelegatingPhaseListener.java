/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.web.jsf.phase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * PhaseListener implementation that delegates to one or more Spring-managed
 * PhaseListeners coming from a Spring ApplicationContext.
 * <p>
 * <p>Configure this listener in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;application>
 *   ...
 *   &lt;phase-listenerr>org.springframework.web.jsf.phase.DelegatingPhaseListener&lt;/phase-listener>
 * &lt;/application></pre>
 * 
 * On the first invocation by JSF to the beforePhase() or afterPhase() methods,
 * the ApplicationContext will be queried for a bean with the name
 * {@link #PHASE_LISTENER_BEAN}. This bean must be a PhaseListener,
 * or a java.util.List of PhaseListeners. The method invocation, along with
 * subsequent invocations of the same methods will then be delegated to the
 * listener or listeners that were obtained from the context. Note that the
 * context is queried for the listener or listeners only once, with the results
 * cached for future use.
 * <p>
 * This implementation may be subclassed to change the default bean name that is
 * used to obtain the listener(s). Subclases may also change the
 * strategy to obtain the list of listeners, or the application context.
 * 
 * @author Colin Sampaleanu
 * @since 1.2.7
 * @see org.springframework.webflow.jsf.util.navigation.JsfChainingNavigationHandler
 */
public class DelegatingPhaseListener implements PhaseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The default bean name used to look up in the ApplicationContext the
	 * PhaseListener or List of PhaseListeners to be delegated to.
	 */
	public final static String PHASE_LISTENER_BEAN = "jsfPhaseListener";

	protected String		   phaseListenerBean   = PHASE_LISTENER_BEAN;

	/**
     * A list of PhaseListenrs we are delegating to
     */
	protected List			 delegates;

	/**
     * Creates a new DelegatingPhaseListener object.
     */
	public DelegatingPhaseListener() {
	}

	/**
     * Extension point for subclasses. This method returns a List of
     * PhaseListeners that is to be delegated to in sequence.
     * <p>
     * The default implementation of this method looks for a bean in the Spring
     * application context with the name {@link #PHASE_LISTENER_BEAN}, doing so
     * in a lazy fashion, then caching the info. This bean must be a
     * PhaseListener, or a List of PhaseListeners.
     * 
     * @param facesContext
     *            Current FacesContext
     * @return a List PhaseListeners that is to be delegated to in sequence.
     */
	protected List getDelegates(final FacesContext facesContext) throws BeansException {

		if (delegates == null) {

			ApplicationContext appContext = getApplicationContext(facesContext);
			Object bean = appContext.getBean(phaseListenerBean);
			if (bean instanceof PhaseListener) {
				delegates = new ArrayList(1);
				delegates.add(bean);
			}
			else {
				
				String errMsg = "Bean with id '"
					+ phaseListenerBean
					+ "' must be of type PhaseListener or List (containing one or more PhaseListeners only"; 

				if (!(bean instanceof List))
					throw new FatalBeanException(errMsg);
							
				List listeners = (List) bean;
				
				if (listeners.size() == 0)
					throw new FatalBeanException(errMsg);
					
				Iterator it = listeners.iterator();
				while (it.hasNext()) {
					Object element = (Object) it.next();
					if (!(element instanceof PhaseListener))
						throw new FatalBeanException(errMsg);
				}

				delegates = listeners;
			}
		}

		return delegates;
	}

	/**
     * Template method to get WebApplicationContext. Broken out more for unit
     * tests than anything else. Normally retrieves ApplicationContext via
     * FacesContextUtils
     * 
     * @param facesContext
     *            Current FacesContext
     * @return a WebApplicatioContext
     * @throws BeansException
     */
	protected WebApplicationContext getApplicationContext(final FacesContext facesContext)
			throws BeansException {

		return FacesContextUtils.getWebApplicationContext(facesContext);
	}

	public void afterPhase(PhaseEvent event) {
		List listeners = getDelegates(event.getFacesContext());
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			PhaseListener listener = (PhaseListener) it.next();
			listener.afterPhase(event);
		}
	}

	public void beforePhase(PhaseEvent event) {
		List listeners = getDelegates(event.getFacesContext());
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			PhaseListener listener = (PhaseListener) it.next();
			listener.beforePhase(event);
		}
	}

	/**
     * Implementation of standard PhaseListener interface method. It is supposed 
     * to return the identifier of the request processing phase during which this
     * listener is interested in processing PhaseEvent events.
     * <p>
     * The behavior in the default implementation is as follows:
     * <p>
     * If the beforePhase or afterPhase methods have not been called yet, then
     * this method returns PhaseId.ANY_PHASE, as it does not actually have
     * knowledge yet of the PhaseListener(s) being delegated to.
     * <p>
     * If the beforePhase or afterPhase methods have been called, then in the 
     * default implementation the PhaseListener(s) being delegated to will have 
     * been retrieved and cached. If there is only one PhaseListener, then the
     * result of its getPhaseId() will be returned. If there are multiple
     * PhaseListeners in the list, then PhaseId.ANY_PHASE is returned since
     * each phase listener may be interested in different phases.
     * <p>
     * When overriding the getDelegates method, make sure to override this
     * method as appropriate, to match the semantics of any changes to that
     * method.  
     */
	public PhaseId getPhaseId() {
		
		if (delegates == null || delegates.size() > 1)
			return PhaseId.ANY_PHASE;
		
		return ((PhaseListener) delegates.get(0)).getPhaseId();
	}
}
