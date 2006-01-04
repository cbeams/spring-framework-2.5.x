package org.springframework.scripting.groovy;

import org.springframework.scripting.AbstractScriptFactory;
import org.springframework.scripting.Script;
import org.springframework.scripting.ScriptSource;

/**
 * @author Rod Johnson
 * @author Rob Harrop
 */
public class GroovyScriptFactory extends AbstractScriptFactory {

	protected Script getScript(ScriptSource scriptSource, Class[] interfaces) {
		return new GroovyScript(scriptSource);
	}
}
