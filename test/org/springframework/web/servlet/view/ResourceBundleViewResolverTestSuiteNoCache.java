package org.springframework.web.servlet.view;

public class ResourceBundleViewResolverTestSuiteNoCache extends ResourceBundleViewResolverTestSuite {
	
	protected boolean getCache() {
		return false;
	}
}
 