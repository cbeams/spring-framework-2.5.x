/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.portlet.mvc.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortalContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.style.StylerUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.HandlerAdapter;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.MissingPortletRequestParameterException;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.context.PortletWebRequest;
import org.springframework.web.portlet.handler.PortletContentGenerator;
import org.springframework.web.portlet.handler.PortletSessionRequiredException;
import org.springframework.web.portlet.multipart.MultipartActionRequest;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * Implementation of the {@link org.springframework.web.portlet.HandlerAdapter}
 * interface that maps handler methods based on portlet modes, action/render phases
 * and request parameters expressed through the {@link RequestMapping} annotation.
 *
 * <p>Supports request parameter binding through the {@link RequestParam} annotation.
 * Also supports the {@link ModelAttribute} annotation for exposing model attribute
 * values to the view, as well as {@link InitBinder} for binder initialization methods
 * and {@link SessionAttributes} for automatic session management of specific attributes.
 *
 * <p>This adapter can be customized through various bean properties.
 * A common use case is to apply shared binder initialization logic through
 * a custom {@link #setWebBindingInitializer WebBindingInitializer}.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 2.5
 * @see #setWebBindingInitializer
 * @see #setSessionAttributeStore
 */
public class AnnotationMethodHandlerAdapter extends PortletContentGenerator implements HandlerAdapter {

	private static final String IMPLICIT_MODEL_ATTRIBUTE = "org.springframework.web.portlet.mvc.ImplicitModel";


	private WebBindingInitializer webBindingInitializer;

	private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

	private final Map<Class<?>, HandlerMethodResolver> methodResolverCache =
			new ConcurrentHashMap<Class<?>, HandlerMethodResolver>();

	private final Map<Class<?>, Set<String>> sessionAttributeNames =
			new ConcurrentHashMap<Class<?>, Set<String>>();


	/**
	 * Specify a WebBindingInitializer which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 */
	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}

	/**
	 * Specify the strategy to store session attributes with.
	 * <p>Default is {@link org.springframework.web.bind.support.DefaultSessionAttributeStore},
	 * storing session attributes in the PortletSession, using the same
	 * attribute name as in the model.
	 */
	public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
		Assert.notNull(sessionAttributeStore, "SessionAttributeStore must not be null");
		this.sessionAttributeStore = sessionAttributeStore;
	}


	public boolean supports(Object handler) {
		return getMethodResolver(handler.getClass()).hasHandlerMethods();
	}

	public void handleAction(ActionRequest request, ActionResponse response, Object handler) throws Exception {
		Object returnValue = doHandle(request, response, handler);
		if (returnValue != null) {
			throw new IllegalStateException("Invalid action method return value: " + returnValue);
		}
	}

	public ModelAndView handleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
		checkAndPrepare(request, response);
		return doHandle(request, response, handler);
	}

	protected ModelAndView doHandle(PortletRequest request, PortletResponse response, Object handler) throws Exception {
		ModelMap implicitModel = null;
		SessionAttributes sessionAttributes = handler.getClass().getAnnotation(SessionAttributes.class);
		Set<String> sessionAttrNames = null;

		if (sessionAttributes != null) {
			// Prepare cached set of session attributes names.
			sessionAttrNames = this.sessionAttributeNames.get(handler.getClass());
			if (sessionAttrNames == null) {
				synchronized (this.sessionAttributeNames) {
					sessionAttrNames = this.sessionAttributeNames.get(handler.getClass());
					if (sessionAttrNames == null) {
						sessionAttrNames = Collections.synchronizedSet(new HashSet<String>(4));
						this.sessionAttributeNames.put(handler.getClass(), sessionAttrNames);
					}
				}
			}
		}

		if (request instanceof RenderRequest) {
			RenderRequest renderRequest = (RenderRequest) request;
			RenderResponse renderResponse = (RenderResponse) response;
			// Detect implicit model from associated action phase.
			if (renderRequest.getParameter(IMPLICIT_MODEL_ATTRIBUTE) != null) {
				PortletSession session = request.getPortletSession(false);
				if (session != null) {
					implicitModel = (ModelMap) session.getAttribute(IMPLICIT_MODEL_ATTRIBUTE);
				}
			}
			if (sessionAttributes != null) {
				// Always prevent caching in case of session attribute management.
				checkAndPrepare(renderRequest, renderResponse, 0);
			}
			else {
				// Uses configured default cacheSeconds setting.
				checkAndPrepare(renderRequest, renderResponse);
			}
		}

		if (implicitModel == null) {
			implicitModel = new ModelMap();
		}

		WebRequest webRequest = new PortletWebRequest(request);
		HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		ArgumentsResolver argResolver = new ArgumentsResolver(methodResolver.getInitBinderMethods());

		for (Method attributeMethod : methodResolver.getModelAttributeMethods()) {
			Object[] args = argResolver.resolveArguments(
					handler, attributeMethod, request, response, webRequest, implicitModel, sessionAttrNames);
			ReflectionUtils.makeAccessible(attributeMethod);
			Object attrValue = ReflectionUtils.invokeMethod(attributeMethod, handler, args);
			String attrName = attributeMethod.getAnnotation(ModelAttribute.class).value();
			if ("".equals(attrName)) {
				implicitModel.addAttribute(attrValue);
			}
			else {
				implicitModel.addAttribute(attrName, attrValue);
			}
		}

		Object[] args = argResolver.resolveArguments(
				handler, handlerMethod, request, response, webRequest, implicitModel, sessionAttrNames);
		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = ReflectionUtils.invokeMethod(handlerMethod, handler, args);
		ModelAndView mav = argResolver.getModelAndView(handlerMethod, result, implicitModel);

		if (sessionAttributes != null) {
			if (argResolver.isProcessingComplete()) {
				if (sessionAttrNames != null) {
					for (String attrName : sessionAttrNames) {
						this.sessionAttributeStore.cleanupAttribute(webRequest, attrName);
					}
				}
			}
			else {
				// Expose model attributes as session attributes, if required.
				Map<String, Object> model = (mav != null ? mav.getModel() : implicitModel);
				Set<Object> sessionAttributeSet = new HashSet<Object>();
				sessionAttributeSet.addAll(Arrays.asList(sessionAttributes.value()));
				sessionAttributeSet.addAll(Arrays.asList(sessionAttributes.types()));
				for (Map.Entry entry : new HashSet<Map.Entry>(model.entrySet())) {
					String attrName = (String) entry.getKey();
					Object attrValue = entry.getValue();
					if (sessionAttributeSet.contains(attrName) ||
							(attrValue != null && sessionAttributeSet.contains(attrValue.getClass()))) {
						sessionAttrNames.add(attrName);
						this.sessionAttributeStore.storeAttribute(webRequest, attrName, attrValue);
						String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attrName;
						if (mav != null && !model.containsKey(bindingResultKey)) {
							PortletRequestDataBinder binder = new PortletRequestDataBinder(attrValue, attrName);
							if (this.webBindingInitializer != null) {
								this.webBindingInitializer.initBinder(binder, webRequest);
							}
							mav.addObject(bindingResultKey, binder.getBindingResult());
						}
					}
				}
			}
		}

		// Expose implicit model for subsequent render phase.
		if (response instanceof ActionResponse && !implicitModel.isEmpty()) {
			ActionResponse actionResponse = (ActionResponse) response;
			try {
				actionResponse.setRenderParameter(IMPLICIT_MODEL_ATTRIBUTE, Boolean.TRUE.toString());
				request.getPortletSession().setAttribute(IMPLICIT_MODEL_ATTRIBUTE, implicitModel);
			}
			catch (IllegalStateException ex) {
				// Probably sendRedirect called... no need to expose model to render phase.
			}
		}

		return mav;
	}


	private HandlerMethodResolver getMethodResolver(Class<?> handlerType) {
		HandlerMethodResolver resolver = this.methodResolverCache.get(handlerType);
		if (resolver == null) {
			resolver = new HandlerMethodResolver(handlerType);
			this.methodResolverCache.put(handlerType, resolver);
		}
		return resolver;
	}


	private static class HandlerMethodResolver {

		private final Set<Method> handlerMethods = new LinkedHashSet<Method>();

		private final Set<Method> initBinderMethods = new LinkedHashSet<Method>();

		private final Set<Method> modelAttributeMethods = new LinkedHashSet<Method>();

		public HandlerMethodResolver(final Class<?> handlerType) {
			ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						handlerMethods.add(method);
					}
					else if (method.isAnnotationPresent(InitBinder.class)) {
						initBinderMethods.add(method);
					}
					else if (method.isAnnotationPresent(ModelAttribute.class)) {
						modelAttributeMethods.add(method);
					}
				}
			});
		}

		public Method resolveHandlerMethod(PortletRequest request) {
			String lookupMode = request.getPortletMode().toString();
			Map<RequestMappingInfo, Method> targetHandlerMethods = new LinkedHashMap<RequestMappingInfo, Method>();
			for (Method handlerMethod : this.handlerMethods) {
				RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
				RequestMappingInfo mappingInfo = new RequestMappingInfo();
				mappingInfo.modes = mapping.value();
				mappingInfo.params = mapping.params();
				mappingInfo.action = isActionMethod(handlerMethod);
				mappingInfo.render = isRenderMethod(handlerMethod);
				boolean match = false;
				if (mappingInfo.modes.length > 0) {
					for (String mappedMode : mappingInfo.modes) {
						if (mappedMode.equalsIgnoreCase(lookupMode)) {
							if (checkParameters(request, mappingInfo)) {
								match = true;
							}
							else {
								break;
							}
						}
					}
				}
				else {
					// No modes specified: parameter match sufficient.
					match = checkParameters(request, mappingInfo);
				}
				if (match) {
					Method oldMappedMethod = targetHandlerMethods.put(mappingInfo, handlerMethod);
					if (oldMappedMethod != null && oldMappedMethod != handlerMethod) {
						throw new IllegalStateException("Ambiguous handler methods mapped for portlet mode '" +
								lookupMode + "': {" + oldMappedMethod + ", " + handlerMethod +
								"}. If you intend to handle the same mode in multiple methods, then factor " +
								"them out into a dedicated handler class with that mode mapped at the type level!");
					}
				}
			}
			if (!targetHandlerMethods.isEmpty()) {
				if (targetHandlerMethods.size() == 1) {
					return targetHandlerMethods.values().iterator().next();
				}
				else {
					RequestMappingInfo bestMappingMatch = null;
					for (RequestMappingInfo mapping : targetHandlerMethods.keySet()) {
						if (bestMappingMatch == null) {
							bestMappingMatch = mapping;
						}
						else {
							if ((bestMappingMatch.modes.length == 0 && mapping.modes.length > 0) ||
									bestMappingMatch.params.length < mapping.params.length) {
								bestMappingMatch = mapping;
							}
						}
					}
					return targetHandlerMethods.get(bestMappingMatch);
				}
			}
			else {
				throw new IllegalStateException("No matching handler method found for portlet request: mode '" +
						request.getPortletMode() + "', type '" + (request instanceof ActionRequest ? "action" : "render") +
						"', parameters " + StylerUtils.style(request.getParameterMap()));
			}
		}

		private boolean checkParameters(PortletRequest request, RequestMappingInfo mapping) {
			if (request instanceof RenderRequest) {
				if (mapping.action) {
					return false;
				}
			}
			else if (request instanceof ActionRequest) {
				if (mapping.render) {
					return false;
				}
			}
			String[] params = mapping.params;
			if (params.length > 0) {
				for (String param : params) {
					int separator = param.indexOf('=');
					if (separator == -1) {
						if (!PortletUtils.hasSubmitParameter(request, param)) {
							return false;
						}
					}
					else {
						String key = param.substring(0, separator);
						String value = param.substring(separator + 1);
						if (!value.equals(request.getParameter(key))) {
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean isActionMethod(Method handlerMethod) {
			if (!void.class.equals(handlerMethod.getReturnType())) {
				return false;
			}
			for (Class<?> argType : handlerMethod.getParameterTypes()) {
				if (ActionRequest.class.isAssignableFrom(argType) || ActionResponse.class.isAssignableFrom(argType) ||
						InputStream.class.isAssignableFrom(argType) || Reader.class.isAssignableFrom(argType)) {
					return true;
				}
			}
			return false;
		}

		private boolean isRenderMethod(Method handlerMethod) {
			if (!void.class.equals(handlerMethod.getReturnType())) {
				return true;
			}
			for (Class<?> argType : handlerMethod.getParameterTypes()) {
				if (RenderRequest.class.isAssignableFrom(argType) || RenderResponse.class.isAssignableFrom(argType) ||
						OutputStream.class.isAssignableFrom(argType) || Writer.class.isAssignableFrom(argType)) {
					return true;
				}
			}
			return false;
		}

		public boolean hasHandlerMethods() {
			return !this.handlerMethods.isEmpty();
		}

		public Set<Method> getInitBinderMethods() {
			return this.initBinderMethods;
		}

		public Set<Method> getModelAttributeMethods() {
			return this.modelAttributeMethods;
		}
	}


	private class ArgumentsResolver {

		private final Set<Method> initBinderMethods;

		private final SimpleSessionStatus sessionStatus = new SimpleSessionStatus();

		public ArgumentsResolver(Set<Method> initBinderMethods) {
			this.initBinderMethods = initBinderMethods;
		}

		@SuppressWarnings("unchecked")
		public Object[] resolveArguments(
				Object handler, Method handlerMethod, PortletRequest request, PortletResponse response,
				WebRequest webRequest, ModelMap implicitModel, Set<String> sessionAttrNames)
				throws PortletException, IOException {

			SessionAttributes sessionAttributes = handler.getClass().getAnnotation(SessionAttributes.class);
			Set sessionAttributeSet = null;
			if (sessionAttributes != null) {
				sessionAttributeSet = new HashSet();
				sessionAttributeSet.addAll(Arrays.asList(sessionAttributes.value()));
				sessionAttributeSet.addAll(Arrays.asList(sessionAttributes.types()));
			}
			SimpleTypeConverter converter = new SimpleTypeConverter();
			Object[] args = new Object[handlerMethod.getParameterTypes().length];
			String[] paramNames = null;
			boolean paramNamesResolved = false;
			for (int i = 0; i < args.length; i++) {
				MethodParameter param = new MethodParameter(handlerMethod, i);
				args[i] = resolveStandardArgument(param.getParameterType(), request, response, webRequest);
				if (args[i] == null) {
					if (param.getParameterType().isInstance(implicitModel)) {
						args[i] = implicitModel;
					}
					else if (param.getParameterType().isInstance(this.sessionStatus)) {
						args[i] = this.sessionStatus;
					}
				}
				if (args[i] == null) {
					boolean isParam = false;
					String paramName = "";
					boolean paramRequired = false;
					String attrName = ClassUtils.getShortNameAsProperty(param.getParameterType());
					Annotation[] paramAnns = (Annotation[]) param.getParameterAnnotations();
					for (int j = 0; j < paramAnns.length; j++) {
						Annotation paramAnn = paramAnns[j];
						if (RequestParam.class.isInstance(paramAnn)) {
							RequestParam requestParam = (RequestParam) paramAnn;
							isParam = true;
							paramName = requestParam.value();
							paramRequired = requestParam.required();
							break;
						}
						else if (ModelAttribute.class.isInstance(paramAnn)) {
							ModelAttribute attr = (ModelAttribute) paramAnn;
							if (!"".equals(attr.value())) {
								attrName = attr.value();
							}
						}
					}
					if (isParam || BeanUtils.isSimpleProperty(param.getParameterType())) {
						// Request parameter value...
						if ("".equals(paramName)) {
							if (!paramNamesResolved) {
								ParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
								paramNames = discoverer.getParameterNames(handlerMethod);
								paramNamesResolved = true;
							}
							if (paramNames == null) {
								throw new IllegalStateException("No parameter specified for @RequestParam argument " +
										"of type [" + param.getParameterType().getName() + "], and no parameter name " +
										"information found in class file either.");
							}
							paramName = paramNames[i];
						}
						Object paramValue = null;
						if (request instanceof MultipartActionRequest) {
							paramValue = ((MultipartActionRequest) request).getFile(paramName);
						}
						if (paramValue == null) {
							paramValue = request.getParameterValues(paramName);
						}
						if (paramValue == null && paramRequired) {
							throw new MissingPortletRequestParameterException(paramName, param.getParameterType().getName());
						}
						args[i] = converter.convertIfNecessary(paramValue, param.getParameterType());
					}
					else {
						// Bind request parameter onto object...
						if (sessionAttributeSet != null &&
								(sessionAttributeSet.contains(attrName) || sessionAttributeSet.contains(param.getParameterType())) &&
								!implicitModel.containsKey(attrName)) {
							Object sessionAttr = sessionAttributeStore.retrieveAttribute(webRequest, attrName);
							if (sessionAttr == null) {
								throw new PortletSessionRequiredException(
										"Required session attribute '" + attrName + "' not found");
							}
							sessionAttrNames.add(attrName);
							implicitModel.addAttribute(attrName, sessionAttr);
						}
						Object bindObject = implicitModel.get(attrName);
						if (bindObject == null) {
							bindObject = BeanUtils.instantiateClass(param.getParameterType());
						}
						PortletRequestDataBinder binder = new PortletRequestDataBinder(bindObject, attrName);
						if (webBindingInitializer != null) {
							webBindingInitializer.initBinder(binder, webRequest);
						}
						for (Method initBinderMethod : this.initBinderMethods) {
							String[] targetNames = initBinderMethod.getAnnotation(InitBinder.class).value();
							if (targetNames.length == 0 || Arrays.asList(targetNames).contains(attrName)) {
								Class<?>[] initBinderParams = initBinderMethod.getParameterTypes();
								Object[] initBinderArgs = new Object[initBinderParams.length];
								for (int j = 0; j < initBinderArgs.length; j++) {
									initBinderArgs[j] = resolveStandardArgument(
											initBinderParams[j], request, response, webRequest);
									if (initBinderArgs[j] == null) {
										if (initBinderParams[j].isInstance(binder)) {
											initBinderArgs[j] = binder;
										}
									}
								}
								ReflectionUtils.makeAccessible(initBinderMethod);
								Object attrValue = ReflectionUtils.invokeMethod(initBinderMethod, handler, initBinderArgs);
								if (attrValue != null) {
									throw new IllegalStateException(
											"InitBinder methods must not have a return value: " + initBinderMethod);
								}
							}
						}
						binder.bind(request);
						args[i] = bindObject;
						implicitModel.putAll(binder.getBindingResult().getModel());
						if (args.length > i + 1 && Errors.class.isAssignableFrom(handlerMethod.getParameterTypes()[i + 1])) {
							args[i + 1] = binder.getBindingResult();
							i++;
						}
						else {
							binder.closeNoCatch();
						}
					}
				}
			}
			return args;
		}

		private Object resolveStandardArgument(
				Class<?> parameterType, PortletRequest request, PortletResponse response, WebRequest webRequest)
				throws IOException {

			if (parameterType.isInstance(request)) {
				return request;
			}
			else if (parameterType.isInstance(response)) {
				return response;
			}
			else if (PortletSession.class.isAssignableFrom(parameterType)) {
				return request.getPortletSession();
			}
			else if (PortletPreferences.class.isAssignableFrom(parameterType)) {
				return request.getPreferences();
			}
			else if (PortletMode.class.isAssignableFrom(parameterType)) {
				return request.getPortletMode();
			}
			else if (WindowState.class.isAssignableFrom(parameterType)) {
				return request.getWindowState();
			}
			else if (PortalContext.class.isAssignableFrom(parameterType)) {
				return request.getPortalContext();
			}
			else if (WebRequest.class.isAssignableFrom(parameterType)) {
				return webRequest;
			}
			else if (Locale.class.equals(parameterType)) {
				return request.getLocale();
			}
			else if (InputStream.class.equals(parameterType)) {
				if (!(request instanceof ActionRequest)) {
					throw new IllegalStateException("InputStream can only get obtained for ActionRequest");
				}
				return ((ActionRequest) request).getPortletInputStream();
			}
			else if (Reader.class.equals(parameterType)) {
				if (!(request instanceof ActionRequest)) {
					throw new IllegalStateException("Reader can only get obtained for ActionRequest");
				}
				return ((ActionRequest) request).getReader();
			}
			else if (OutputStream.class.equals(parameterType)) {
				if (!(response instanceof RenderResponse)) {
					throw new IllegalStateException("OutputStream can only get obtained for RenderResponse");
				}
				return ((RenderResponse) response).getPortletOutputStream();
			}
			else if (Writer.class.equals(parameterType)) {
				if (!(response instanceof RenderResponse)) {
					throw new IllegalStateException("Writer can only get obtained for RenderResponse");
				}
				return ((RenderResponse) response).getWriter();
			}
			else {
				return null;
			}
		}

		public boolean isProcessingComplete() {
			return this.sessionStatus.isComplete();
		}

		@SuppressWarnings("unchecked")
		public ModelAndView getModelAndView(Method handlerMethod, Object returnValue, ModelMap implicitModel) {
			if (returnValue instanceof ModelAndView) {
				ModelAndView mav = (ModelAndView) returnValue;
				mav.getModelMap().mergeAttributes(implicitModel);
				return mav;
			}
			else if (returnValue instanceof Map) {
				return new ModelAndView().addAllObjects(implicitModel).addAllObjects((Map) returnValue);
			}
			else if (returnValue instanceof String) {
				return new ModelAndView((String) returnValue).addAllObjects(implicitModel);
			}
			else if (returnValue == null) {
				// Either returned null or was 'void' return.
				return null;
			}
			else if (!BeanUtils.isSimpleProperty(returnValue.getClass())) {
				// Assume a single model attribute...
				String attrName = handlerMethod.getAnnotation(ModelAttribute.class).value();
				ModelAndView mav = new ModelAndView().addAllObjects(implicitModel);
				if ("".equals(attrName)) {
					return mav.addObject(returnValue);
				}
				else {
					return mav.addObject(attrName, returnValue);
				}
			}
			else {
				throw new IllegalArgumentException("Invalid handler method return value: " + returnValue);
			}
		}
	}


	private static class RequestMappingInfo {

		public String[] modes = new String[0];

		public String[] params = new String[0];

		private boolean action = false;

		private boolean render = false;

		public boolean equals(Object obj) {
			RequestMappingInfo other = (RequestMappingInfo) obj;
			return (this.action == other.action && this.render == other.render &&
					this.modes.equals(other.modes) && this.params.equals(other.params));
		}

		public int hashCode() {
			return (Arrays.hashCode(this.modes) * 29 + Arrays.hashCode(this.params));
		}
	}

}
