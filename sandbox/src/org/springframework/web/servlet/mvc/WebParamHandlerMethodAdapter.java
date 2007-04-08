package org.springframework.web.servlet.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.annotation.WebParam;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerMethod;
import org.springframework.web.servlet.handler.metadata.UrlAnnotationHandlerMapping;

/**
 * Adapter that supports handler methods that use the {@link WebParam} annotation. Supports methods with the following
 * signature:
 * <pre>
 * void handleRequest(&#64;WebParam(value = "id", required = true)int param);
 * </pre>
 * or
 * <pre>
 * ModelAndView handleOtherRequest(&#64;WebParam("id")int id, &#64;WebParam("name")String name);
 * </pre>
 * I.e. methods that return either <code>void</code> or a {@link ModelAndView}, and have parameters annotated with
 * {@link WebParam} that specify the request parameter that should be bound to that parameter. The parameter can be of
 * the following types:
 * <ul>
 * <li><code>boolean</code>, or {@link Boolean}</li>
 * <li><code>double</code>, or {@link Double}</li>
 * <li><code>float</code>, or {@link Float}</li>
 * <li><code>int</code>, or {@link Integer}</li>
 * <li><code>long</code>, or {@link Long}</li>
 * <li>{@link String}</li>
 * </ul>
 * <p/>
 * This handler adpater is typically combined with the {@link UrlAnnotationHandlerMapping}.
 *
 * @author Arjen Poutsma
 */
public class WebParamHandlerMethodAdapter extends AbstractHandlerMethodAdapter {

    protected boolean supportsInternal(Method method) {
        if (!(ModelAndView.class.isAssignableFrom(method.getReturnType()) ||
                Void.TYPE.equals(method.getReturnType()))) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (getWebParamAnnotation(method, i) == null || !isSuportedType(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private WebParam getWebParamAnnotation(Method method, int paramIdx) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int annIdx = 0; annIdx < paramAnnotations[paramIdx].length; annIdx++) {
            if (paramAnnotations[paramIdx][annIdx].annotationType().equals(WebParam.class)) {
                return (WebParam) paramAnnotations[paramIdx][annIdx];
            }
        }
        return null;
    }

    private boolean isSuportedType(Class<?> clazz) {
        return Boolean.class.isAssignableFrom(clazz) || Boolean.TYPE.isAssignableFrom(clazz) ||
                Double.class.isAssignableFrom(clazz) || Double.TYPE.isAssignableFrom(clazz) ||
                Float.class.isAssignableFrom(clazz) || Float.TYPE.isAssignableFrom(clazz) ||
                Integer.class.isAssignableFrom(clazz) || Integer.TYPE.isAssignableFrom(clazz) ||
                Long.class.isAssignableFrom(clazz) || Long.TYPE.isAssignableFrom(clazz) ||
                String.class.isAssignableFrom(clazz);
    }

    protected ModelAndView handlerInternal(HttpServletRequest request,
                                           HttpServletResponse response,
                                           HandlerMethod handlerMethod) throws Exception {
        Object[] args = getMethodArguments(request, handlerMethod.getMethod());
        Object result = handlerMethod.invoke(args);
        if (result != null && result instanceof ModelAndView) {
            return (ModelAndView) result;
        }
        else {
            return null;
        }
    }

    private Object[] getMethodArguments(HttpServletRequest request, Method method)
            throws ServletRequestBindingException {
        Class[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            WebParam webParam = getWebParamAnnotation(method, i);
            if (Boolean.class.isAssignableFrom(parameterTypes[i]) || Boolean.TYPE.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredBooleanParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getBooleanParameter(request, webParam.value());
                }
            }
            else
            if (Double.class.isAssignableFrom(parameterTypes[i]) || Double.TYPE.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredDoubleParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getDoubleParameter(request, webParam.value());
                }
            }
            else
            if (Float.class.isAssignableFrom(parameterTypes[i]) || Float.TYPE.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredFloatParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getFloatParameter(request, webParam.value());
                }
            }
            else
            if (Integer.class.isAssignableFrom(parameterTypes[i]) || Integer.TYPE.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredIntParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getIntParameter(request, webParam.value());
                }
            }
            else if (Long.class.isAssignableFrom(parameterTypes[i]) || Long.TYPE.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredLongParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getLongParameter(request, webParam.value());
                }
            }
            else if (String.class.isAssignableFrom(parameterTypes[i])) {
                if (webParam.required()) {
                    args[i] = ServletRequestUtils.getRequiredStringParameter(request, webParam.value());
                }
                else {
                    args[i] = ServletRequestUtils.getStringParameter(request, webParam.value());
                }
            }
        }
        return args;
    }
}
