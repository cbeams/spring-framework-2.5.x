package org.springframework.web.servlet.mvc.multiaction;

/**
 * Simple implementation of MethodNameResolver that maps URL to method
 * name. Although this is the default implementation used by the
 * MultiActionController class (because it requires no configuration),
 * it's bit naive for most applications. In particular, we don't usually
 * want to tie URL to implementation methods.
 *
 * <p>Maps the resource name after the last slash, ignoring an extension.
 * E.g. "/foo/bar/baz.html" to "baz", assuming a "/foo/bar/baz.html"
 * controller mapping to the respective MultiActionController.
 * Doesn't support wildcards.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
*/
public class InternalPathMethodNameResolver extends AbstractUrlMethodNameResolver {

	protected String getHandlerMethodNameForUrlPath(String urlPath) {
		String name = urlPath;
		// look at resource name after last slash
		int slashIndex = name.lastIndexOf('/');
		if (slashIndex != -1) {
			name = name.substring(slashIndex+1);
		}
		// ignore extension
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex != -1) {
			name = name.substring(0, dotIndex);
		}
		return name;
	}

}
