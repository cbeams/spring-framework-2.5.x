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
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.FormAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SimpleFormStatus;
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
		checkAndPrepare(request, response);
		return doHandle(request, response, handler);
	}

	protected ModelAndView doHandle(PortletRequest request, PortletResponse response, Object handler) throws Exception {
		HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		ModelMap implicitModel = new ModelMap();
		ArgumentsResolver argResolver = new ArgumentsResolver(this.webBindingInitializer);

		for (Method attributeMethod : methodResolver.getModelAttributeMethods()) {
			String attrName = attributeMethod.getAnnotation(ModelAttribute.class).value();
			if ("".equals(attrName)) {
				attrName = ClassUtils.getShortNameAsProperty(attributeMethod.getReturnType());
			}
			Object[] args = argResolver.resolveArguments(handler, attributeMethod, request, response, implicitModel);
			ReflectionUtils.makeAccessible(attributeMethod);
			Object attrValue = ReflectionUtils.invokeMethod(attributeMethod, handler, args);
			implicitModel.addObject(attrName, attrValue);
		}

		Object[] args = argResolver.resolveArguments(handler, handlerMethod, request, response, implicitModel);
		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = ReflectionUtils.invokeMethod(handlerMethod, handler, args);
		ModelAndView mav = argResolver.getModelAndView(result, implicitModel);

		FormAttributes formAttributes = handler.getClass().getAnnotation(FormAttributes.class);
		if (formAttributes != null) {
			for (String attrName : formAttributes.value()) {
				if (argResolver.isFormComplete()) {
					request.getPortletSession().removeAttribute(attrName);
				}
				else {
					// Expose model attributes as session attributes, if required.
					if (mav.getModel().containsKey(attrName)) {
						Object formObject = mav.getModel().get(attrName);
						request.getPortletSession().setAttribute(attrName, formObject);
						String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attrName;
						if (!mav.getModel().containsKey(bindingResultKey)) {
							PortletRequestDataBinder binder = new PortletRequestDataBinder(formObject, attrName);
							if (this.webBindingInitializer != null) {
								this.webBindingInitializer.initBinder(binder, new PortletWebRequest(request));
							}
							mav.addObject(bindingResultKey, binder.getBindingResult());
						}
					}
				}
			}
		}
		return mav;
	}


	private HandlerMethodResolver getMethodResolver(Class handlerType) {
		HandlerMethodResolver resolver = this.methodResolverCache.get(handlerType);
		if (resolver == null) {
			resolver = new HandlerMethodResolver(handlerType);
			this.methodResolverCache.put(handlerType, resolver);
		}
		return resolver;
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


	private static class ArgumentsResolver {

		private final WebBindingInitializer webBindingInitializer;

		private final SimpleFormStatus formStatus = new SimpleFormStatus();

		public ArgumentsResolver(WebBindingInitializer webBindingInitializer) {
			this.webBindingInitializer = webBindingInitializer;
		}

		public Object[] resolveArguments(
				Object handler, Method handlerMethod, PortletRequest request, PortletResponse response,
				ModelMap implicitModel) throws PortletException, IOException {

			Set<String> formAttributeSet = null;
			FormAttributes formAttributes = handler.getClass().getAnnotation(FormAttributes.class);
			if (formAttributes != null) {
				formAttributeSet = new HashSet<String>(Arrays.asList(formAttributes.value()));
			}
			SimpleTypeConverter converter = new SimpleTypeConverter();
			Object[] args = new Object[handlerMethod.getParameterTypes().length];
			for (int i = 0; i < args.length; i++) {
				MethodParameter param = new MethodParameter(handlerMethod, i);
				args[i] = resolveStandardArgument(param.getParameterType(), request, response);
				if (args[i] == null) {
					if (param.getParameterType().isInstance(implicitModel)) {
						args[i] = implicitModel;
					}
					else if (param.getParameterType().isInstance(this.formStatus)) {
						args[i] = this.formStatus;
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
						if (formAttributeSet != null && formAttributeSet.contains(attrName) &&
								!implicitModel.containsKey(attrName)) {
							PortletSession session = request.getPortletSession(false);
							if (session == null) {
								throw new PortletSessionRequiredException(
										"No session found - session required for attribute '" + attrName + "'");
							}
							Object sessionAttr = session.getAttribute(attrName);
							if (sessionAttr == null) {
								throw new PortletSessionRequiredException(
										"Session attribute '" + attrName + "' required - not found in session");
							}
							implicitModel.addObject(attrName, sessionAttr);
						}
						Object formObject = implicitModel.get(attrName);
						if (formObject == null) {
							formObject = BeanUtils.instantiateClass(param.getParameterType());
						}
						PortletRequestDataBinder binder = new PortletRequestDataBinder(formObject, attrName);
						if (this.webBindingInitializer != null) {
							this.webBindingInitializer.initBinder(binder, new PortletWebRequest(request));
						}
						binder.bind(request);
						args[i] = formObject;
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

		protected Object resolveStandardArgument(
				Class parameterType, PortletRequest request, PortletResponse response) throws IOException {

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

		public boolean isFormComplete() {
			return this.formStatus.isComplete();
		}

		public ModelAndView getModelAndView(Object returnValue, ModelMap implicitModel) {
			if (returnValue instanceof ModelAndView) {
				ModelAndView mav = (ModelAndView) returnValue;
				mav.getModelMap().mergeObjects(implicitModel);
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
