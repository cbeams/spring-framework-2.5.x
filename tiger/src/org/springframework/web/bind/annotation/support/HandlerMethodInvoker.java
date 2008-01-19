/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.web.bind.annotation.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartRequest;

/**
 * Support class for invoking an annotated handler method.
 * Operates on the introspection results of a {@link HandlerMethodResolver}
 * for a specific handler type.
 *
 * <p>Used by {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}
 * and {@link org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter}.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see #invokeHandlerMethod
 */
public class HandlerMethodInvoker {

	private final HandlerMethodResolver methodResolver;

	private final WebBindingInitializer bindingInitializer;

	private final SessionAttributeStore sessionAttributeStore;

	private final ParameterNameDiscoverer parameterNameDiscoverer;

	private final SimpleSessionStatus sessionStatus = new SimpleSessionStatus();


	public HandlerMethodInvoker(HandlerMethodResolver methodResolver) {
		this(methodResolver, null);
	}

	public HandlerMethodInvoker(HandlerMethodResolver methodResolver, WebBindingInitializer bindingInitializer) {
		this(methodResolver, bindingInitializer, new DefaultSessionAttributeStore(), null);
	}

	public HandlerMethodInvoker(
			HandlerMethodResolver methodResolver, WebBindingInitializer bindingInitializer,
			SessionAttributeStore sessionAttributeStore, ParameterNameDiscoverer parameterNameDiscoverer) {

		this.methodResolver = methodResolver;
		this.bindingInitializer = bindingInitializer;
		this.sessionAttributeStore = sessionAttributeStore;
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}


	public final Object invokeHandlerMethod(
			Method handlerMethod, Object handler, NativeWebRequest webRequest, ExtendedModelMap implicitModel)
			throws Exception {

		for (Method attributeMethod : this.methodResolver.getModelAttributeMethods()) {
			Object[] args = resolveArguments(attributeMethod, handler, webRequest, implicitModel);
			Object attrValue = doInvokeMethod(attributeMethod, handler, args);
			String attrName = AnnotationUtils.findAnnotation(attributeMethod, ModelAttribute.class).value();
			if ("".equals(attrName)) {
				Class resolvedType = GenericTypeResolver.resolveReturnType(attributeMethod, handler.getClass());
				attrName = Conventions.getVariableNameForReturnType(attributeMethod, resolvedType, attrValue);
			}
			implicitModel.addAttribute(attrName, attrValue);
		}

		Object[] args = resolveArguments(handlerMethod, handler, webRequest, implicitModel);
		return doInvokeMethod(handlerMethod, handler, args);
	}

	@SuppressWarnings("unchecked")
	private Object[] resolveArguments(
			Method handlerMethod, Object handler, NativeWebRequest webRequest, ExtendedModelMap implicitModel)
			throws Exception {

		Class[] paramTypes = handlerMethod.getParameterTypes();
		Object[] args = new Object[paramTypes.length];
		String[] paramNames = null;
		boolean paramNamesResolved = false;
		for (int i = 0; i < args.length; i++) {
			MethodParameter param = new MethodParameter(handlerMethod, i);
			GenericTypeResolver.resolveParameterType(param, handler.getClass());
			boolean isParam = false;
			String paramName = "";
			boolean paramRequired = false;
			String attrName = null;
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
					attrName = attr.value();
				}
			}
			if (isParam && attrName != null) {
				throw new IllegalStateException("@RequestParam and @ModelAttribute are an exclusive choice -" +
						"do not specify both on the same parameter: " + handlerMethod);
			}
			if (!isParam && attrName == null) {
				args[i] = resolveStandardArgument(webRequest, param.getParameterType());
				if (args[i] == null) {
					if (Model.class.isAssignableFrom(param.getParameterType()) ||
							Map.class.isAssignableFrom(param.getParameterType())) {
						args[i] = implicitModel;
					}
					else if (SessionStatus.class.isAssignableFrom(param.getParameterType())) {
						args[i] = this.sessionStatus;
					}
				}
				if (args[i] == null) {
					if (BeanUtils.isSimpleProperty(param.getParameterType())) {
						isParam = true;
					}
					else {
						attrName = "";
					}
				}
			}
			if (isParam) {
				// Request parameter value...
				if ("".equals(paramName)) {
					if (!paramNamesResolved && this.parameterNameDiscoverer != null) {
						paramNames = this.parameterNameDiscoverer.getParameterNames(handlerMethod);
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
				if (webRequest.getNativeRequest() instanceof MultipartRequest) {
					paramValue = ((MultipartRequest) webRequest.getNativeRequest()).getFile(paramName);
				}
				if (paramValue == null) {
					String[] paramValues = webRequest.getParameterValues(paramName);
					if (paramValues != null) {
						paramValue = (paramValues.length == 1 ? paramValues[0] : paramValues);
					}
				}
				if (paramValue == null) {
					if (paramRequired) {
						raiseMissingParameterException(paramName, param.getParameterType());
					}
					if (param.getParameterType().isPrimitive()) {
						throw new IllegalStateException("Optional " + param.getParameterType() + " parameter '" +
								paramName + "' is not present but cannot be translated into a null value due to " +
								"being declared as a primitive type. Consider declaring it as object wrapper type " +
								"for the corresponding primitive type.");
					}
				}
				WebDataBinder binder = createBinder(webRequest, null, paramName);
				initBinder(handler, paramName, binder, webRequest);
				args[i] = binder.convertIfNecessary(paramValue, param.getParameterType(), param);
			}
			else if (attrName != null) {
				// Bind request parameter onto object...
				if ("".equals(attrName)) {
					attrName = Conventions.getVariableNameForParameter(param);
				}
				if (!implicitModel.containsKey(attrName) &&
						this.methodResolver.isSessionAttribute(attrName, param.getParameterType())) {
					Object sessionAttr = this.sessionAttributeStore.retrieveAttribute(webRequest, attrName);
					if (sessionAttr == null) {
						raiseSessionRequiredException("Session attribute '" + attrName + "' required - not found in session");
					}
					implicitModel.addAttribute(attrName, sessionAttr);
				}
				Object bindObject = implicitModel.get(attrName);
				if (bindObject == null) {
					bindObject = BeanUtils.instantiateClass(param.getParameterType());
				}
				WebDataBinder binder = createBinder(webRequest, bindObject, attrName);
				initBinder(handler, attrName, binder, webRequest);
				args[i] = bindObject;
				implicitModel.putAll(binder.getBindingResult().getModel());
				if (args.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i + 1])) {
					args[i + 1] = binder.getBindingResult();
					i++;
					doBind(webRequest, binder, false);
				}
				else {
					doBind(webRequest, binder, true);
				}
			}
		}

		return args;
	}

	private void initBinder(Object handler, String attrName, WebDataBinder binder, NativeWebRequest webRequest)
			throws Exception {

		if (this.bindingInitializer != null) {
			this.bindingInitializer.initBinder(binder, webRequest);
		}
		for (Method initBinderMethod : this.methodResolver.getInitBinderMethods()) {
			String[] targetNames = AnnotationUtils.findAnnotation(initBinderMethod, InitBinder.class).value();
			if (targetNames.length == 0 || Arrays.asList(targetNames).contains(attrName)) {
				Class[] initBinderParams = initBinderMethod.getParameterTypes();
				Object[] initBinderArgs = new Object[initBinderParams.length];
				for (int j = 0; j < initBinderArgs.length; j++) {
					initBinderArgs[j] = resolveStandardArgument(webRequest, initBinderParams[j]);
					if (initBinderArgs[j] == null) {
						if (initBinderParams[j].isInstance(binder)) {
							initBinderArgs[j] = binder;
						}
					}
				}
				Object returnValue = doInvokeMethod(initBinderMethod, handler, initBinderArgs);
				if (returnValue != null) {
					throw new IllegalStateException(
							"InitBinder methods must not have a return value: " + initBinderMethod);
				}
			}
		}
	}

	private static Object doInvokeMethod(Method method, Object target, Object[] args) throws Exception {
		ReflectionUtils.makeAccessible(method);
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			ReflectionUtils.rethrowException(ex.getTargetException());
		}
		throw new IllegalStateException("Should never get here");
	}

	@SuppressWarnings("unchecked")
	public final void updateSessionAttributes(
			Object handler, Map mavModel, ExtendedModelMap implicitModel, NativeWebRequest webRequest)
			throws Exception {

		if (this.methodResolver.hasSessionAttributes()) {
			if (this.sessionStatus.isComplete()) {
				for (String attrName : this.methodResolver.getActualSessionAttributeNames()) {
					this.sessionAttributeStore.cleanupAttribute(webRequest, attrName);
				}
			}
			// Expose model attributes as session attributes, if required.
			Map<String, Object> model = (mavModel != null ? mavModel : implicitModel);
			for (Map.Entry entry : new HashSet<Map.Entry>(model.entrySet())) {
				String attrName = (String) entry.getKey();
				Object attrValue = entry.getValue();
				if (this.methodResolver.isSessionAttribute(attrName, (attrValue != null ? attrValue.getClass() : null))) {
					if (!this.sessionStatus.isComplete()) {
						this.sessionAttributeStore.storeAttribute(webRequest, attrName, attrValue);
					}
					String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + attrName;
					if (mavModel != null && !model.containsKey(bindingResultKey)) {
						WebDataBinder binder = createBinder(webRequest, attrValue, attrName);
						initBinder(handler, attrName, binder, webRequest);
						mavModel.put(bindingResultKey, binder.getBindingResult());
					}
				}
			}
		}
	}


	protected void raiseMissingParameterException(String paramName, Class paramType) throws Exception {
		throw new IllegalStateException("Missing parameter '" + paramName + "' of type [" + paramType.getName() + "]");
	}

	protected void raiseSessionRequiredException(String message) throws Exception {
		throw new IllegalStateException(message);
	}

	protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
			throws Exception {

		return new WebRequestDataBinder(target, objectName);
	}

	protected void doBind(NativeWebRequest webRequest, WebDataBinder binder, boolean failOnErrors)
			throws Exception {

		WebRequestDataBinder requestBinder = (WebRequestDataBinder) binder;
		requestBinder.bind(webRequest);
		if (failOnErrors) {
			requestBinder.closeNoCatch();
		}
	}

	protected Object resolveStandardArgument(NativeWebRequest webRequest, Class<?> parameterType)
			throws Exception {

		if (WebRequest.class.isAssignableFrom(parameterType)) {
			return webRequest;
		}
		else {
			return null;
		}
	}

}
