package org.springframework.web.servlet.handler.metadata;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.annotation.Url;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.WebParamHandlerMethodAdapter;

/**
 * Implementation of the {@link HandlerMapping}<code>EndpointMapping</code> interface that uses the {@link Url}
 * annotation to map methods to url paths.
 * <p/>
 * Controllers typically have the following form:
 * <pre>
 * &#64;Controller
 * public class MyController {
 *    &#64;Url("/index.html")
 *    public ModelAndView handleIndexRequest() {
 *       ...
 *    }
 * }
 * </pre>
 * <p/>
 * This handler mapping is typically combined with the {@link WebParamHandlerMethodAdapter}.
 *
 * @author Arjen Poutsma
 */
public class UrlAnnotationHandlerMapping extends AbstractAnnotationHandlerMapping {

    protected String[] getUrlPaths(Method method) {
        Url url = AnnotationUtils.findAnnotation(method, Url.class);
        if (url != null) {
            return url.value();
        }
        else {
            return new String[0];
        }
    }
}
