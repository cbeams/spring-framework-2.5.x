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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.HandlerAdapter;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.MissingPortletRequestParameterException;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.context.PortletWebRequest;

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
 * @since 2.5
 */
public class AnnotationMethodHandlerAdapter implements HandlerAdapter {

	private WebBindingInitializer webBindingInitializer;

	private final Map<Class, HandlerMethodResolver> methodResolverCache =
			new ConcurrentHashMap<Class, HandlerMethodResolver>();


	/**
	 * Specify a WebBindingInitializer which will apply pre-configured
	 * configuration to every DataBinder that this controller uses.
	 */
	public final void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
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
		return doHandle(request, response, handler);
	}

	protected ModelAndView doHandle(PortletRequest request, PortletResponse response, Object handler) throws Exception {
		AnnotationMethodHandlerAdapter.HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		Map<String, Object> implicitModel = new LinkedHashMap<String, Object>();
		for (Method attributeMethod : methodResolver.getModelAttributeMethods()) {
			String attrName = attributeMethod.getAnnotation(ModelAttribute.class).value();
			if (attrName == null) {
				attrName = ClassUtils.getShortNameAsProperty(attributeMethod.getReturnType());
			}
			Object[] args = resolveArguments(handler, attributeMethod, request, response, null);
			ReflectionUtils.makeAccessible(attributeMethod);
			Object attrValue = ReflectionUtils.invokeMethod(attributeMethod, handler, args);
			implicitModel.put(attrName, attrValue);
		}
		Object[] args = resolveArguments(handler, handlerMethod, request, response, implicitModel);
		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = ReflectionUtils.invokeMethod(handlerMethod, handler, args);
		return getModelAndView(result, implicitModel);
	}


	private AnnotationMethodHandlerAdapter.HandlerMethodResolver getMethodResolver(Class handlerType) {
		AnnotationMethodHandlerAdapter.HandlerMethodResolver resolver = this.methodResolverCache.get(handlerType);
		if (resolver == null) {
			resolver = new AnnotationMethodHandlerAdapter.HandlerMethodResolver(handlerType);
			this.methodResolverCache.put(handlerType, resolver);
		}
		return resolver;
	}

	protected Object[] resolveArguments(
			Object handler, Method handlerMethod, PortletRequest request, PortletResponse response,
			Map implicitModel) throws PortletException {

		SimpleTypeConverter converter = new SimpleTypeConverter();
		Object[] args = new Object[handlerMethod.getParameterTypes().length];
		for (int i = 0; i < args.length; i++) {
			MethodParameter param = new MethodParameter(handlerMethod, i);
			args[i] = resolveStandardArgument(param.getParameterType(), request, response);
			if (args[i] == null) {
				boolean resolved = false;
				String attrName = ClassUtils.getShortNameAsProperty(param.getParameterType());
				Annotation[] paramAnns = (Annotation[]) param.getParameterAnnotations();
				for (int j = 0; j < paramAnns.length; j++) {
					Annotation paramAnn = paramAnns[j];
					if (RequestParam.class.isInstance(paramAnn)) {
						RequestParam requestParam = (RequestParam) paramAnn;
						String[] paramValues = request.getParameterValues(requestParam.value());
						if (paramValues == null && requestParam.required()) {
							throw new MissingPortletRequestParameterException(
									requestParam.value(), param.getParameterType().getName());
						}
						args[i] = converter.convertIfNecessary(paramValues, param.getParameterType());
						resolved = true;
						break;
					}
					else if (ModelAttribute.class.isInstance(paramAnn)) {
						ModelAttribute attr = (ModelAttribute) paramAnn;
						if (attr.value() != null) {
							attrName = attr.value();
						}
					}
				}
				if (!resolved) {
					Object command = (implicitModel != null ? implicitModel.get(attrName) : null);
					if (command == null) {
						command = BeanUtils.instantiateClass(param.getParameterType());
					}
					PortletRequestDataBinder binder = new PortletRequestDataBinder(command, attrName);
					if (this.webBindingInitializer != null) {
						this.webBindingInitializer.initBinder(binder, new PortletWebRequest(request));
					}
					binder.bind(request);
					args[i] = command;
					if (implicitModel != null) {
						implicitModel.putAll(binder.getBindingResult().getModel());
						if (args.length > i + 1 && Errors.class.isAssignableFrom(handlerMethod.getParameterTypes()[i + 1])) {
							args[i + 1] = binder.getBindingResult();
							i++;
						}
					}
					else {
						binder.closeNoCatch();
					}
				}
			}
		}
		return args;
	}

	protected Object resolveStandardArgument(
			Class parameterType, PortletRequest request, PortletResponse response) {

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
			return new PortletWebRequest(request);
		}
		else if (Locale.class.equals(parameterType)) {
			return request.getLocale();
		}
		else {
			return null;
		}
	}

	protected ModelAndView getModelAndView(Object returnValue, Map<String, Object> implicitModel) {
		if (returnValue instanceof ModelAndView) {
			ModelAndView mav = (ModelAndView) returnValue;
			for (Map.Entry<String, Object> entry : implicitModel.entrySet()) {
				if (!mav.getModel().containsKey(entry.getKey())) {
					mav.addObject(entry.getKey(), entry.getValue());
				}
			}
			return mav;
		}
		else if (returnValue instanceof Map) {
			return new ModelAndView().addAllObjects(implicitModel).addAllObjects((Map) returnValue);
		}
		else if (returnValue instanceof String) {
			return new ModelAndView((String) returnValue).addAllObjects(implicitModel);
		}
		else {
			// Either returned null or was 'void' return.
			return null;
		}
	}


	private static class HandlerMethodResolver {

		private Set<Method> handlerMethods = new LinkedHashSet<Method>();

		private Set<Method> modelAttributeMethods = new LinkedHashSet<Method>();

		public HandlerMethodResolver(final Class<?> handlerType) {
			ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						handlerMethods.add(method);
					}
					else if (method.isAnnotationPresent(ModelAttribute.class)) {
						modelAttributeMethods.add(method);
					}
				}
			});
		}

		public Method resolveHandlerMethod(PortletRequest request) {
			String lookupMode = request.getPortletMode().toString();
			List<Method> specificDefaultHandlerMethods = new LinkedList();
			List<Method> defaultHandlerMethods = new LinkedList();
			for (Method handlerMethod : this.handlerMethods) {
				RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
				String[] mappedModes = mapping.value();
				if (mappedModes.length > 0) {
					for (String mappedPath : mappedModes) {
						if (mappedPath.toLowerCase().equals(lookupMode)) {
							if (checkParameters(request, mapping)) {
								if (mapping.type().equals("") && mapping.params().length == 0) {
									specificDefaultHandlerMethods.add(handlerMethod);
								}
								else {
									return handlerMethod;
								}
							}
							else {
								break;
							}
						}
					}
				}
				else {
					// No paths specified: parameter match sufficient.
					if (checkParameters(request, mapping)) {
						if (mapping.type().equals("") && mapping.params().length == 0) {
							defaultHandlerMethods.add(handlerMethod);
						}
						else {
							return handlerMethod;
						}
					}
				}
			}
			if (!specificDefaultHandlerMethods.isEmpty()) {
				return resolveDefaultHandlerMethod(request, specificDefaultHandlerMethods);
			}
			else if (!defaultHandlerMethods.isEmpty()) {
				return resolveDefaultHandlerMethod(request, defaultHandlerMethods);
			}
			else {
				throw new IllegalStateException("No matching handler method found for request");
			}
		}

		private boolean checkParameters(PortletRequest request, RequestMapping mapping) {
			if (!mapping.type().equals("")) {
				String requestType = (request instanceof RenderRequest ? "render" : "action");
				if (!mapping.type().toLowerCase().equals(requestType)) {
					return false;
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

		private Method resolveDefaultHandlerMethod(PortletRequest request, List<Method> defaultHandlerMethods) {
			if (defaultHandlerMethods.size() == 1) {
				return defaultHandlerMethods.get(0);
			}
			else if (defaultHandlerMethods.size() == 2) {
				if (request instanceof RenderRequest) {
					Method candidate1 = defaultHandlerMethods.get(0);
					Method candidate2 = defaultHandlerMethods.get(1);
					boolean isRenderMethod1 = isRenderMethod(candidate1);
					boolean isRenderMethod2 = isRenderMethod(candidate2);
					if (isRenderMethod1 != isRenderMethod2) {
						return (isRenderMethod1 ? candidate1 : candidate2);
					}
				}
				else if (request instanceof ActionRequest) {
					Method candidate1 = defaultHandlerMethods.get(0);
					Method candidate2 = defaultHandlerMethods.get(1);
					boolean isActionMethod1 = isActionMethod(candidate1);
					boolean isActionMethod2 = isActionMethod(candidate2);
					if (isActionMethod1 != isActionMethod2) {
						return (isActionMethod1 ? candidate1 : candidate2);
					}
				}
			}
			throw new IllegalStateException("Ambiguous handler methods mapped for portlet mode '" +
					request.getPortletMode() + "': " + defaultHandlerMethods);
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

		public Set<Method> getModelAttributeMethods() {
			return this.modelAttributeMethods;
		}
	}

}
