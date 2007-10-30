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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
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

/**
 * Implementation of the {@link org.springframework.web.portlet.HandlerAdapter}
 * interface that maps handler methods based on portlet modes, action/render phases
 * and request parameters expressed through the {@link RequestMapping} annotation.
 *
 * <p>Supports request parameter binding through the {@link RequestParam} annotation.
 * Also supports the {@link ModelAttribute} annotation for exposing model attribute
 * values to the view.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 2.5
 */
public class AnnotationMethodHandlerAdapter extends PortletContentGenerator implements HandlerAdapter {

	private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

	private WebBindingInitializer webBindingInitializer;

	private final Map<Class<?>, HandlerMethodResolver> methodResolverCache =
			new ConcurrentHashMap<Class<?>, HandlerMethodResolver>();


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

	/**
	 * Specify a WebBindingInitializer which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 */
	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
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
		WebRequest webRequest = new PortletWebRequest(request);
		HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		ModelMap implicitModel = new ModelMap();
		ArgumentsResolver argResolver = new ArgumentsResolver(methodResolver.getInitBinderMethods());

		for (Method attributeMethod : methodResolver.getModelAttributeMethods()) {
			String attrName = attributeMethod.getAnnotation(ModelAttribute.class).value();
			if ("".equals(attrName)) {
				attrName = ClassUtils.getShortNameAsProperty(attributeMethod.getReturnType());
			}
			Object[] args = argResolver.resolveArguments(
					handler, attributeMethod, request, response, webRequest, implicitModel);
			ReflectionUtils.makeAccessible(attributeMethod);
			Object attrValue = ReflectionUtils.invokeMethod(attributeMethod, handler, args);
			implicitModel.addAttribute(attrName, attrValue);
		}

		Object[] args = argResolver.resolveArguments(
				handler, handlerMethod, request, response, webRequest, implicitModel);
		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = ReflectionUtils.invokeMethod(handlerMethod, handler, args);
		ModelAndView mav = argResolver.getModelAndView(result, implicitModel);

		SessionAttributes sessionAttributes = handler.getClass().getAnnotation(SessionAttributes.class);
		if (sessionAttributes != null) {
			for (String attrName : sessionAttributes.value()) {
				if (argResolver.isProcessingComplete()) {
					this.sessionAttributeStore.cleanupAttribute(webRequest, attrName);
				}
				else {
					// Expose model attributes as session attributes, if required.
					if (mav.getModel().containsKey(attrName)) {
						Object modelAttribute = mav.getModel().get(attrName);
						this.sessionAttributeStore.storeAttribute(webRequest, attrName, modelAttribute);
						String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attrName;
						if (!mav.getModel().containsKey(bindingResultKey)) {
							PortletRequestDataBinder binder = new PortletRequestDataBinder(modelAttribute, attrName);
							if (this.webBindingInitializer != null) {
								this.webBindingInitializer.initBinder(binder, webRequest);
							}
							mav.addObject(bindingResultKey, binder.getBindingResult());
						}
					}
				}
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
			Map<RequestMapping, Method> targetHandlerMethods = new LinkedHashMap<RequestMapping, Method>();
			for (Method handlerMethod : this.handlerMethods) {
				RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
				boolean match = false;
				String[] mappedModes = mapping.value();
				if (mappedModes.length > 0) {
					for (String mappedMode : mappedModes) {
						if (mappedMode.toLowerCase().equals(lookupMode)) {
							if (checkParameters(request, mapping, handlerMethod)) {
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
					match = checkParameters(request, mapping, handlerMethod);
				}
				if (match) {
					Method oldMappedMethod = targetHandlerMethods.put(mapping, handlerMethod);
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
					RequestMapping bestMappingMatch = null;
					for (RequestMapping mapping : targetHandlerMethods.keySet()) {
						if (bestMappingMatch == null) {
							bestMappingMatch = mapping;
						}
						else {
							if ((bestMappingMatch.value().length == 0 && mapping.value().length > 0) ||
									("".equals(bestMappingMatch.type()) && !"".equals(mapping.type())) ||
									bestMappingMatch.params().length < mapping.params().length) {
								bestMappingMatch = mapping;
							}
						}
					}
					return targetHandlerMethods.get(bestMappingMatch);
				}
			}
			else {
				throw new IllegalStateException("No matching handler method found for request");
			}
		}

		private boolean checkParameters(PortletRequest request, RequestMapping mapping, Method handlerMethod) {
			if (!mapping.type().equals("")) {
				String requestType = (request instanceof RenderRequest ? "render" : "action");
				if (!mapping.type().toLowerCase().equals(requestType)) {
					return false;
				}
			}
			else {
				if (request instanceof RenderRequest) {
					if (isActionMethod(handlerMethod)) {
						return false;
					}
				}
				else {
					if (isRenderMethod(handlerMethod)) {
						return false;
					}
				}
			}
			String[] params = mapping.params();
			if (params.length > 0) {
				for (String param : params) {
					int separator = param.indexOf('=');
					if (separator == -1) {
						if (request.getParameter(param) == null) {
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
				if (ActionRequest.class.isAssignableFrom(argType) || ActionResponse.class.isAssignableFrom(argType)) {
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
				if (RenderRequest.class.isAssignableFrom(argType) || RenderResponse.class.isAssignableFrom(argType)) {
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
				WebRequest webRequest, ModelMap implicitModel) throws PortletException, IOException {

			Set<String> sessionAttributeSet = null;
			SessionAttributes sessionAttributes = handler.getClass().getAnnotation(SessionAttributes.class);
			if (sessionAttributes != null) {
				sessionAttributeSet = new HashSet<String>(Arrays.asList(sessionAttributes.value()));
			}
			SimpleTypeConverter converter = new SimpleTypeConverter();
			Object[] args = new Object[handlerMethod.getParameterTypes().length];
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
					boolean resolved = false;
					String attrName = ClassUtils.getShortNameAsProperty(param.getParameterType());
					Annotation[] paramAnns = (Annotation[]) param.getParameterAnnotations();
					for (int j = 0; j < paramAnns.length; j++) {
						Annotation paramAnn = paramAnns[j];
						if (RequestParam.class.isInstance(paramAnn)) {
							RequestParam requestParam = (RequestParam) paramAnn;
							Object paramValue = null;
							if (request instanceof MultipartActionRequest) {
								paramValue = ((MultipartActionRequest) request).getFile(requestParam.value());
							}
							if (paramValue == null) {
								paramValue = request.getParameterValues(requestParam.value());
							}
							if (paramValue == null && requestParam.required()) {
								throw new MissingPortletRequestParameterException(
										requestParam.value(), param.getParameterType().getName());
							}
							args[i] = converter.convertIfNecessary(paramValue, param.getParameterType());
							resolved = true;
							break;
						}
						else if (ModelAttribute.class.isInstance(paramAnn)) {
							ModelAttribute attr = (ModelAttribute) paramAnn;
							if (!"".equals(attrName)) {
								attrName = attr.value();
							}
						}
					}
					if (!resolved) {
						if (sessionAttributeSet != null && sessionAttributeSet.contains(attrName) &&
								!implicitModel.containsKey(attrName)) {
							Object sessionAttr = sessionAttributeStore.retrieveAttribute(webRequest, attrName);
							if (sessionAttr == null) {
								throw new PortletSessionRequiredException(
										"Required session attribute '" + attrName + "' not found");
							}
							implicitModel.addAttribute(attrName, sessionAttr);
						}
						Object commandObject = implicitModel.get(attrName);
						if (commandObject == null) {
							commandObject = BeanUtils.instantiateClass(param.getParameterType());
						}
						PortletRequestDataBinder binder = new PortletRequestDataBinder(commandObject, attrName);
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
									throw new IllegalStateException("InitBinder methods must not have a return value");
								}
							}
						}
						binder.bind(request);
						args[i] = commandObject;
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
		public ModelAndView getModelAndView(Object returnValue, ModelMap implicitModel) {
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
			else {
				throw new IllegalArgumentException("Invalid handler method return value: " + returnValue);
			}
		}
	}

}
