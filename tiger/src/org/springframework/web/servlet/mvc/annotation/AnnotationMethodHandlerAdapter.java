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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.style.StylerUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerAdapter}
 * interface that maps handler methods based on HTTP paths, HTTP methods and
 * request parameters expressed through the {@link RequestMapping} annotation.
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
 * @see #setPathMatcher
 * @see #setMethodNameResolver
 * @see #setWebBindingInitializer
 * @see #setSessionAttributeStore
 */
public class AnnotationMethodHandlerAdapter extends WebContentGenerator implements HandlerAdapter {

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

	private WebBindingInitializer webBindingInitializer;

	private final Map<Class<?>, HandlerMethodResolver> methodResolverCache =
			new ConcurrentHashMap<Class<?>, HandlerMethodResolver>();

	private final Map<Class<?>, Set<String>> sessionAttributeNames =
			new ConcurrentHashMap<Class<?>, Set<String>>();


	/**
	 * Set if URL lookup should always use the full path within the current servlet
	 * context. Else, the path within the current servlet mapping is used if applicable
	 * (that is, in the case of a ".../*" servlet mapping in web.xml).
	 * <p>Default is "false".
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	/**
	 * Set if context path and request URI should be URL-decoded. Both are returned
	 * <i>undecoded</i> by the Servlet API, in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}

	/**
	 * Set the UrlPathHelper to use for resolution of lookup paths.
	 * <p>Use this to override the default UrlPathHelper with a custom subclass,
	 * or to share common UrlPathHelper settings across multiple HandlerMappings
	 * and HandlerAdapters.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * Set the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Set the MethodNameResolver to use for resolving default handler methods
	 * (carrying an empty <code>@RequestMapping</code> annotation).
	 * <p>Will only kick in when the handler method cannot be resolved uniquely
	 * through the annotation metadata already.
	 */
	public void setMethodNameResolver(MethodNameResolver methodNameResolver) {
		this.methodNameResolver = methodNameResolver;
	}

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
	 * storing session attributes in the HttpSession, using the same
	 * attribute name as in the model.
	 */
	public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
		Assert.notNull(sessionAttributeStore, "SessionAttributeStore must not be null");
		this.sessionAttributeStore = sessionAttributeStore;
	}


	public boolean supports(Object handler) {
		return getMethodResolver(handler).hasHandlerMethods();
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ModelMap implicitModel = new ModelMap();
		SessionAttributes sessionAttributes = handler.getClass().getAnnotation(SessionAttributes.class);
		Set<String> sessionAttrNames = null;

		if (sessionAttributes != null) {
			// Always prevent caching in case of session attribute management.
			checkAndPrepare(request, response, 0, false);
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
		else {
			// Uses configured default cacheSeconds setting.
			checkAndPrepare(request, response, false);
		}

		WebRequest webRequest = new ServletWebRequest(request);
		HandlerMethodResolver methodResolver = getMethodResolver(handler);
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
							ServletRequestDataBinder binder = new ServletRequestDataBinder(attrValue, attrName);
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

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}


	private HandlerMethodResolver getMethodResolver(Object handler) {
		HandlerMethodResolver resolver = this.methodResolverCache.get(handler.getClass());
		if (resolver == null) {
			resolver = new HandlerMethodResolver(handler.getClass());
			this.methodResolverCache.put(handler.getClass(), resolver);
		}
		return resolver;
	}


	private class HandlerMethodResolver {

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

		public Method resolveHandlerMethod(HttpServletRequest request) throws ServletException {
			String lookupPath = urlPathHelper.getLookupPathForRequest(request);
			Map<RequestMappingInfo, Method> targetHandlerMethods = new LinkedHashMap<RequestMappingInfo, Method>();
			Map<RequestMappingInfo, String> targetPathMatches = new LinkedHashMap<RequestMappingInfo, String>();
			String resolvedMethodName = null;
			for (Method handlerMethod : this.handlerMethods) {
				RequestMappingInfo mappingInfo = new RequestMappingInfo();
				RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
				mappingInfo.paths = mapping.value();
				mappingInfo.methods = mapping.method();
				mappingInfo.params = mapping.params();
				boolean match = false;
				if (mappingInfo.paths.length > 0) {
					for (String mappedPath : mappingInfo.paths) {
						if (mappedPath.equals(lookupPath) || pathMatcher.match(mappedPath, lookupPath)) {
							if (checkParameters(request, mappingInfo)) {
								match = true;
								targetPathMatches.put(mappingInfo, mappedPath);
							}
							else {
								break;
							}
						}
					}
				}
				else {
					// No paths specified: parameter match sufficient.
					match = checkParameters(request, mappingInfo);
					if (match && mappingInfo.methods.length == 0 && mappingInfo.params.length == 0 &&
							resolvedMethodName != null && !resolvedMethodName.equals(handlerMethod.getName())) {
						match = false;
					}
				}
				if (match) {
					Method oldMappedMethod = targetHandlerMethods.put(mappingInfo, handlerMethod);
					if (oldMappedMethod != null && oldMappedMethod != handlerMethod) {
						if (methodNameResolver != null && resolvedMethodName == null && mappingInfo.isEmpty()) {
							resolvedMethodName = methodNameResolver.getHandlerMethodName(request);
							if (!resolvedMethodName.equals(oldMappedMethod.getName())) {
								oldMappedMethod = null;
							}
							if (!resolvedMethodName.equals(handlerMethod.getName())) {
								if (oldMappedMethod != null) {
									targetHandlerMethods.put(mappingInfo, oldMappedMethod);
									oldMappedMethod = null;
								}
								else {
									targetHandlerMethods.remove(mappingInfo);
								}
							}
						}
						if (oldMappedMethod != null) {
							throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
									lookupPath + "': {" + oldMappedMethod + ", " + handlerMethod +
									"}. If you intend to handle the same path in multiple methods, then factor " +
									"them out into a dedicated handler class with that path mapped at the type level!");
						}
					}
				}
			}
			if (targetHandlerMethods.size() == 1) {
				return targetHandlerMethods.values().iterator().next();
			}
			else if (!targetHandlerMethods.isEmpty()) {
				RequestMappingInfo bestMappingMatch = null;
				String bestPathMatch = null;
				for (RequestMappingInfo mapping : targetHandlerMethods.keySet()) {
					String mappedPath = targetPathMatches.get(mapping);
					if (bestMappingMatch == null) {
						bestMappingMatch = mapping;
						bestPathMatch = mappedPath;
					}
					else {
						if ((mappedPath != null && (bestPathMatch == null ||
								mappedPath.equals(lookupPath) || bestPathMatch.length() < mappedPath.length())) ||
								(bestMappingMatch.methods.length == 0 && mapping.methods.length > 0) ||
								bestMappingMatch.params.length < mapping.params.length) {
							bestMappingMatch = mapping;
							bestPathMatch = mappedPath;
						}
					}
				}
				return targetHandlerMethods.get(bestMappingMatch);
			}
			else {
				throw new IllegalStateException("No matching handler method found for servlet request: path '" +
						lookupPath + "', method '" + request.getMethod() + "', parameters " +
						StylerUtils.style(request.getParameterMap()));
			}
		}

		private boolean checkParameters(HttpServletRequest request, RequestMappingInfo mapping) {
			if (mapping.methods.length > 0) {
				boolean match = false;
				for (RequestMethod type : mapping.methods) {
					if (type.toString().equals(request.getMethod().toUpperCase())) {
						match = true;
					}
				}
				if (!match) {
					return false;
				}
			}
			String[] params = mapping.params;
			if (params.length > 0) {
				for (String param : params) {
					int separator = param.indexOf('=');
					if (separator == -1) {
						if (!WebUtils.hasSubmitParameter(request, param)) {
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


	private class ArgumentsResolver {

		private final Set<Method> initBinderMethods;

		private final SimpleSessionStatus sessionStatus = new SimpleSessionStatus();

		private boolean responseArgumentUsed = false;

		public ArgumentsResolver(Set<Method> initBinderMethods) {
			this.initBinderMethods = initBinderMethods;
		}

		@SuppressWarnings("unchecked")
		public Object[] resolveArguments(
				Object handler, Method handlerMethod, HttpServletRequest request, HttpServletResponse response,
				WebRequest webRequest, ModelMap implicitModel, Set<String> sessionAttrNames)
				throws ServletException, IOException {

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
						if (request instanceof MultipartHttpServletRequest) {
							paramValue = ((MultipartHttpServletRequest) request).getFile(paramName);
						}
						if (paramValue == null) {
							paramValue = request.getParameterValues(paramName);
						}
						if (paramValue == null && paramRequired) {
							throw new MissingServletRequestParameterException(paramName, param.getParameterType().getName());
						}
						args[i] = converter.convertIfNecessary(paramValue, param.getParameterType());
					}
					else {
						// Bind request parameter onto object...
						if (sessionAttributeSet != null &&
								(sessionAttributeSet.contains(attrName) || sessionAttributeSet.contains(param.getParameterType())) &&
								!implicitModel.containsKey(attrName)) {
							HttpSession session = request.getSession(false);
							if (session == null) {
								throw new HttpSessionRequiredException(
										"No session found - session required for attribute '" + attrName + "'");
							}
							Object sessionAttr = sessionAttributeStore.retrieveAttribute(webRequest, attrName);
							if (sessionAttr == null) {
								throw new HttpSessionRequiredException(
										"Session attribute '" + attrName + "' required - not found in session");
							}
							sessionAttrNames.add(attrName);
							implicitModel.addAttribute(attrName, sessionAttr);
						}
						Object bindObject = implicitModel.get(attrName);
						if (bindObject == null) {
							bindObject = BeanUtils.instantiateClass(param.getParameterType());
						}
						ServletRequestDataBinder binder = new ServletRequestDataBinder(bindObject, attrName);
						if (webBindingInitializer != null) {
							webBindingInitializer.initBinder(binder, webRequest);
						}
						for (Method initBinderMethod : this.initBinderMethods) {
							String[] targetNames = initBinderMethod.getAnnotation(InitBinder.class).value();
							if (targetNames.length == 0 || Arrays.asList(targetNames).contains(attrName)) {
								Class[] initBinderParams = initBinderMethod.getParameterTypes();
								Object[] initBinderArgs = new Object[initBinderParams.length];
								for (int j = 0; j < initBinderArgs.length; j++) {
									initBinderArgs[j] = resolveStandardArgument(initBinderParams[j], request, response, webRequest);
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
				Class<?> parameterType, HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
				throws IOException {

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
				return webRequest;
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
				if (!this.responseArgumentUsed) {
					// Assuming view name translation...
					return new ModelAndView().addAllObjects(implicitModel);
				}
				else {
					return null;
				}
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

		public String[] paths = new String[0];

		public RequestMethod[] methods = new RequestMethod[0];

		public String[] params = new String[0];

		public boolean isEmpty() {
			return (paths.length == 0 && methods.length == 0 && params.length == 0);
		}

		public boolean equals(Object obj) {
			RequestMappingInfo other = (RequestMappingInfo) obj;
			return (this.paths.equals(other.paths) && this.methods.equals(other.methods) &&
					this.params.equals(other.params));
		}

		public int hashCode() {
			return (Arrays.hashCode(this.paths) * 29 + Arrays.hashCode(this.methods) * 31 +
					Arrays.hashCode(this.params));
		}
	}

}
