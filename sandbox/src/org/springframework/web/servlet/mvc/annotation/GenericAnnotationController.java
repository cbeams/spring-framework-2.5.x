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

/**
 * @author Mark Fisher
 */
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

public class GenericAnnotationController extends AbstractController {

	private final Map<String, Method> getMethodMappings = new ConcurrentHashMap<String, Method>();

	private final Map<String, Method> postMethodMappings = new ConcurrentHashMap<String, Method>();

	private final Map<Class<?>, Method> validateMethodMappings = new ConcurrentHashMap<Class<?>, Method>();

	private boolean bindOnGet = false;


	public GenericAnnotationController() {
		initMethodMappings();
	}

	public void setBindOnGet(boolean bindOnGet) {
		this.bindOnGet = bindOnGet;
	}

	private void initMethodMappings() {
		ReflectionUtils.doWithMethods(getClass(), new MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Annotation[] annotations = method.getAnnotations();
				for (Annotation annotation : annotations) {
					Class<? extends Annotation> annotationType = annotation.annotationType();
					String[] paths = null;
					if (annotationType.equals(Post.class)) {
						paths = ((Post) annotation).value();
						if (paths != null) {
							for (String path : paths) {
								if (postMethodMappings.get(path) != null) {
									throw new IllegalStateException(
											"only one POST method may be mapped to path: '" + path + "'");
								}
								postMethodMappings.put(path, method);
							}
						}
					}
					else if (annotationType.equals(Get.class)) {
						paths = ((Get) annotation).value();
						if (paths != null) {
							for (String path : paths) {
								if (getMethodMappings.get(path) != null) {
									throw new IllegalStateException(
											"only one GET method may be mapped to path: '" + path + "'");
								}
								getMethodMappings.put(path, method);
							}
						}
					}
					else if (annotationType.equals(Validate.class)) {
						Class[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length != 2 || !parameterTypes[1].equals(Errors.class)) {
							throw new IllegalStateException(
									"validation method must have two parameters with second of type [" + 
									Errors.class.getName() + "]");
						}
						Class targetType = parameterTypes[0];
						if (validateMethodMappings.get(targetType) != null) {
							throw new IllegalStateException(
									"only one validation method may be mapped to type [" + targetType.getName() + "]");
						}
						validateMethodMappings.put(targetType, method);
					}
				}
			}
		});
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String path = request.getContextPath();
		String requestMethod = request.getMethod();
		Method method = null;
		if ("POST".equals(requestMethod)) {
			method = this.postMethodMappings.get(path);
		}
		else if ("GET".equals(requestMethod)) {
			method = this.getMethodMappings.get(path);
		}
		return this.invokeMethod(method, path, request, response);
	}

	protected final ModelAndView invokeMethod(Method method, String path,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (method == null) {
			throw new NoSuchRequestHandlingMethodException(method.getName(), getClass());
		}

		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length > 3) {
			throw new IllegalStateException("handler methods accept at most 3 parameters");
		}
		Object[] parameterValues = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> type = parameterTypes[i];
			if (HttpServletRequest.class.isAssignableFrom(type)) {
				parameterValues[i] = request;
			}
			else if (HttpServletResponse.class.isAssignableFrom(type)) {
				parameterValues[i] = response;
			}
			else {
				try {
					Object target = createCommandObject(type);
					boolean isPost = "POST".equals(request.getMethod());
					BindingResult result = null;
					if (isPost || this.bindOnGet) { 
						ServletRequestDataBinder binder = new ServletRequestDataBinder(target);
						binder.bind(request);
						result = binder.getBindingResult();
					}
					if (isPost) {
						Method validateMethod = validateMethodMappings.get(type);
						if (validateMethod != null) {
							if (result == null) {
								result = new BindException(target, ClassUtils.getShortNameAsProperty(type));
							}
							validateMethod.invoke(this, target, result);
						}
					}
					if (result != null && result.hasErrors()) {
						return new ModelAndView(path, result.getModel());
					}
					parameterValues[i] = target;
				}
				catch (BeanInstantiationException e) {
					throw new IllegalStateException("unable to create object for binding", e);
				}
			}
		}
		Object returnValue = method.invoke(this, parameterValues);
		if (method.getReturnType().equals(void.class)) {
			return new ModelAndView(path);
		}
		if (returnValue instanceof ModelAndView) { 
			return (ModelAndView) returnValue;
		}
		if (returnValue instanceof Map) {
			return new ModelAndView(path, (Map) returnValue);
		}
		return new ModelAndView(path).addObject(returnValue);
	}

	protected Object createCommandObject(Class type) {
		return BeanUtils.instantiateClass(type);
	}

}
