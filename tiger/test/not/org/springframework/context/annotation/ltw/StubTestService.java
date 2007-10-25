package not.org.springframework.context.annotation.ltw;

import org.springframework.stereotype.Component;

/**
 * Needed for tests in org.springframework.context.annotation.ltw.
 * 
 * Note: This class must be in a non-org.springframework package as 
 * such package is filtered out by ContextTypeMatchClassLoader
 * and thus interfering with tests for ContextTypeMatchClassLoader.
 * 
 * @author Ramnivas Laddad
 */
@Component
public class StubTestService {
	public void serve() {
	}
}
