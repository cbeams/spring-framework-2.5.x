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
import java.util.Arrays;
import java.util.HashSet;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.FormAttributes;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SimpleFormStatus;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.WebContentGenerator;
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
public class AnnotationMethodHandlerAdapter extends WebContentGenerator implements HandlerAdapter {

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
		checkAndPrepare(request, response, false);

		HandlerMethodResolver methodResolver = getMethodResolver(handler.getClass());
		Method handlerMethod = methodResolver.resolveHandlerMethod(request);
		ModelMap implicitModel = new ModelMap();
		ArgumentsResolver argResolver =
				new ArgumentsResolver(this.webBindingInitializer, methodResolver.getInitBinderMethods());

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
					request.getSession().removeAttribute(attrName);
				}
				else {
					// Expose model attributes as session attributes, if required.
					if (mav.getModel().containsKey(attrName)) {
						Object formObject = mav.getModel().get(attrName);
						request.getSession().setAttribute(attrName, formObject);
						String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attrName;
						if (!mav.getModel().containsKey(bindingResultKey)) {
							ServletRequestDataBinder binder = new ServletRequestDataBinder(formObject, attrName);
							if (this.webBindingInitializer != null) {
								this.webBindingInitializer.initBinder(binder, new ServletWebRequest(request));
							}
							mav.addObject(bindingResultKey, binder.getBindingResult());
						}
					}
				}
			}
		}
		return mav;
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



	private static class HandlerMethodResolver {

		private Set<Method> handlerMethods = new LinkedHashSet<Method>();

		private Set<Method> initBinderMethods = new LinkedHashSet<Method>();

		private Set<Method> modelAttributeMethods = new LinkedHashSet<Method>();

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

		public Set<Method> getInitBinderMethods() {
			return this.initBinderMethods;
		}

		public Set<Method> getModelAttributeMethods() {
			return this.modelAttributeMethods;
		}
	}


	private static class ArgumentsResolver {

		private final WebBindingInitializer webBindingInitializer;

		private final Set<Method> initBinderMethods;

		private final SimpleFormStatus formStatus = new SimpleFormStatus();

		private boolean responseArgumentUsed = false;

		public ArgumentsResolver(WebBindingInitializer webBindingInitializer, Set<Method> initBinderMethods) {
			this.webBindingInitializer = webBindingInitializer;
			this.initBinderMethods = initBinderMethods;
		}

		public Object[] resolveArguments(
				Object handler, Method handlerMethod, HttpServletRequest request, HttpServletResponse response,
				ModelMap implicitModel) throws ServletException, IOException {

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
							if (!"".equals(attrName)) {
								attrName = attr.value();
							}
						}
					}
					if (!resolved) {
						if (formAttributeSet != null && formAttributeSet.contains(attrName) &&
								!implicitModel.containsKey(attrName)) {
							HttpSession session = request.getSession(false);
							if (session == null) {
								throw new HttpSessionRequiredException(
										"No session found - session required for attribute '" + attrName + "'");
							}
							Object sessionAttr = session.getAttribute(attrName);
							if (sessionAttr == null) {
								throw new HttpSessionRequiredException(
										"Session attribute '" + attrName + "' required - not found in session");
							}
							implicitModel.addObject(attrName, sessionAttr);
						}
						Object formObject = implicitModel.get(attrName);
						if (formObject == null) {
							formObject = BeanUtils.instantiateClass(param.getParameterType());
						}
						ServletRequestDataBinder binder = new ServletRequestDataBinder(formObject, attrName);
						if (this.webBindingInitializer != null) {
							this.webBindingInitializer.initBinder(binder, new ServletWebRequest(request));
						}
						for (Method initBinderMethod : this.initBinderMethods) {
							String[] targetNames = initBinderMethod.getAnnotation(InitBinder.class).value();
							if (targetNames.length == 0 || Arrays.asList(targetNames).contains(attrName)) {
								Class[] initBinderParams = initBinderMethod.getParameterTypes();
								Object[] initBinderArgs = new Object[initBinderParams.length];
								for (int j = 0; j < initBinderArgs.length; j++) {
									initBinderArgs[j] = resolveStandardArgument(initBinderParams[j], request, response);
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
				Class parameterType, HttpServletRequest request, HttpServletResponse response) throws IOException {

			if (parameterType.isInstance(request)) {
				return request;
			}
			else if (parameterType.isInstance(response)) {
				this.responseArgumentUsed = true;
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
				this.responseArgumentUsed = true;
				return response.getOutputStream();
			}
			else if (Writer.class.equals(parameterType)) {
				this.responseArgumentUsed = true;
				return response.getWriter();
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
				if (!this.responseArgumentUsed) {
					// Assuming view name translation...
					return new ModelAndView();
				}
				else {
					return null;
				}
			}
			else {
				throw new IllegalArgumentException("Invalid handler method return value: " + returnValue);
			}
		}
	}

}
