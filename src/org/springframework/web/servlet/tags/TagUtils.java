package org.springframework.web.servlet.tags;

import javax.servlet.jsp.PageContext;

/**
 * Utility class to transform Strings to scopes.<br>
 * <code>page</code> will be transformed to 
 * {@link javax.servlet.jsp.PageContext#PAGE_SCOPE PageContext.PAGE_SCOPE}
 *  * <code>request</code> will be transformed to 
 * {@link javax.servlet.jsp.PageContext#REQUEST_SCOPE PageContext.REQUEST_SCOPE}<br>
 * <code>session</code> will be transformed to 
 * {@link javax.servlet.jsp.PageContext#SESSION_SCOPE PageContext.SESSION_SCOPE}<br>
 * <code>application</code> will be transformed to 
 * {@link javax.servlet.jsp.PageContext#APPLICATION_SCOPE PageContext.APPLICATION_SCOPE}<br>
 * 
 * @author Alef Arendsen
 * @since 1.0
 */
public class TagUtils {

	/** constant identifying the REQUEST scope String */
	public static final String REQUEST = "request";
	/** constant identifying the SESSION scope String */
	public static final String SESSION = "session";
	/** constant identifying the PAGE scope String */
	public static final String PAGE = "page";
	/** constant identifying the APPLICATION scope String */
	public static final String APPLICATION = "application";

    /** Prevent construction */
    private TagUtils() {
    }

    /**
     * Determines the scope for a given input String. If the string does not match
     * 'request', 'session', 'page' or 'application', the method will return 
     * PageContext.PAGE_SCOPE.
     * @param scope the string to inspect
     * @return the scope found, or PageContext.PAGE_SCOPE if no scope matched.
     * @throws IllegalArgumentException if the scope is null
     */
    public static int getScope(String scope)
    {
        if (scope == null) {
        	throw new IllegalArgumentException(
				"Scope to search for cannot be null");
        }
        
        int ret = 1;
        if(scope.equals(REQUEST)) {
            ret = PageContext.REQUEST_SCOPE;
        } else if(scope.equals(SESSION)) {
            ret = PageContext.SESSION_SCOPE;
    	} else if (scope.equals(APPLICATION)) {
            ret = PageContext.APPLICATION_SCOPE;
        }
        return ret;
    }
}
