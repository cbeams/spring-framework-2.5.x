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

package org.springframework.web.servlet.mvc.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerAdapter}
 * interface that maps handler methods based on HTTP paths, HTTP methods and
 * request parameters expressed through the {@link RequestMapping} annotation.
 *
 * <p>Supports request parameter binding through the {@link RequestParam} annotation.
 * Also supports the {@link ModelAttribute} annotation for exposing model attribute
 * values to the view.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
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

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		ModelMap implicitModel = new ModelMap();
		for (Method attributeMethod : methodResolver.getModelAttributeMethods()) {
			String attrName = attributeMethod.getAnnotation(ModelAttribute.class).value();
			if (attrName == null) {
				attrName = ClassUtils.getShortNameAsProperty(attributeMethod.getReturnType());
			}
			Object[] args = resolveArguments(handler, attributeMethod, request, response, implicitModel);
			ReflectionUtils.makeAccessible(attributeMethod);
			Object attrValue = ReflectionUtils.invokeMethod(attributeMethod, handler, args);
			implicitModel.addObject(attrName, attrValue);
		}
		Object[] args = resolveArguments(handler, handlerMethod, request, response, implicitModel);
		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = ReflectionUtils.invokeMethod(handlerMethod, handler, args);
		return getModelAndView(result, implicitModel);
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}


	private HandlerMethodResolver getMethodResolver(Class handlerType) {
		HandlerMethodResolver resolver = this.methodResolverCache.get(handlerType);
		if (resolver == null) {
			resolver = new HandlerMethodResolver(handlerType);
			this.methodResolverCache.put(handlerType, resolver);
		}
		return resolver;
	}

	protected Object[] resolveArguments(
			Object handler, Method handlerMethod, HttpServletRequest request, HttpServletResponse response,
			ModelMap implicitModel) throws ServletException, IOException {

		SimpleTypeConverter converter = new SimpleTypeConverter();
		Object[] args = new Object[handlerMethod.getParameterTypes().length];
		for (int i = 0; i < args.length; i++) {
			MethodParameter param = new MethodParameter(handlerMethod, i);
			args[i] = resolveStandardArgument(param.getParameterType(), request, response);
			if (args[i] == null && param.getParameterType().isInstance(implicitModel)) {
				args[i] = implicitModel;
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
						if (request instanceof MultipartHttpServletRequest) {
							paramValue = ((MultipartHttpServletRequest) request).getFile(requestParam.value());
						}
						if (paramValue == null) {
							paramValue = request.getParameterValues(requestParam.value());
						}
						if (paramValue == null && requestParam.required()) {
							throw new MissingServletRequestParameterException(
									requestParam.value(), param.getParameterType().getName());
						}
						args[i] = converter.convertIfNecessary(paramValue, param.getParameterType());
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
					Object command = implicitModel.get(attrName);
					if (command == null) {
						command = BeanUtils.instantiateClass(param.getParameterType());
					}
					ServletRequestDataBinder binder = new ServletRequestDataBinder(command, attrName);
					if (this.webBindingInitializer != null) {
						this.webBindingInitializer.initBinder(binder, new ServletWebRequest(request));
					}
					binder.bind(request);
					args[i] = command;
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
			Class parameterType, HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (parameterType.isInstance(request)) {
			return request;
		}
		else if (parameterType.isInstance(response)) {
			return response;
		}
		else if (HttpSession.class.isAssignableFrom(parameterType)) {
			return request.getSession();
		}
		else if (WebRequest.class.isAssignableFrom(parameterType)) {
			return new ServletWebRequest(request);
		}
		else if (Locale.class.equals(parameterType)) {
			return RequestContextUtils.getLocale(request);
		}
		else if (InputStream.class.equals(parameterType)) {
			return request.getInputStream();
		}
		else if (Reader.class.equals(parameterType)) {
			return request.getReader();
		}
		else if (OutputStream.class.equals(parameterType)) {
			return response.getOutputStream();
		}
		else if (Writer.class.equals(parameterType)) {
			return response.getWriter();
		}
		else {
			return null;
		}
	}

	protected ModelAndView getModelAndView(Object returnValue, ModelMap implicitModel) {
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

		public Method resolveHandlerMethod(HttpServletRequest request) {
			String lookupPath = new UrlPathHelper().getLookupPathForRequest(request);
			Set<Method> specificDefaultHandlerMethods = new LinkedHashSet();
			Set<Method> defaultHandlerMethods = new LinkedHashSet();
			for (Method handlerMethod : this.handlerMethods) {
				RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
				String[] mappedPaths = mapping.value();
				if (mappedPaths.length > 0) {
					for (String mappedPath : mappedPaths) {
						if (mappedPath.equals(lookupPath)) {
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
						if (mapping.params().length == 0) {
							defaultHandlerMethods.add(handlerMethod);
						}
						else {
							return handlerMethod;
						}
					}
				}
			}
			if (!specificDefaultHandlerMethods.isEmpty()) {
				if (specificDefaultHandlerMethods.size() == 1) {
					return specificDefaultHandlerMethods.iterator().next();
				}
				else {
					throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
							lookupPath + "': " + specificDefaultHandlerMethods);
				}
			}
			else if (!defaultHandlerMethods.isEmpty()) {
				if (defaultHandlerMethods.size() == 1) {
					return defaultHandlerMethods.iterator().next();
				}
				else {
					throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
							lookupPath + "': " + defaultHandlerMethods);
				}
			}
			else {
				throw new IllegalStateException("No matching handler method found for request");
			}
		}

		private boolean checkParameters(HttpServletRequest request, RequestMapping mapping) {
			if (!mapping.type().equals("") &&
					!mapping.type().toUpperCase().equals(request.getMethod().toUpperCase())) {
				return false;
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

		public boolean hasHandlerMethods() {
			return !this.handlerMethods.isEmpty();
		}

		public Set<Method> getModelAttributeMethods() {
			return this.modelAttributeMethods;
		}
	}

}
