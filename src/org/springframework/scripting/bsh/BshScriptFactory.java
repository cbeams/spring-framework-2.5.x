package org.springframework.scripting.bsh;

import java.io.IOException;

import bsh.EvalError;

import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;

/**
 * ScriptFactory implementation for a BeanShell script.
 *
 * <p>Typically used in combination with ScriptFactoryPostProcessor;
 * see the latter's javadoc for a configuration example.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor
 * @see BshScriptUtils
 */
public class BshScriptFactory implements ScriptFactory {

	private String scriptSourceLocator;

	private Class[] scriptInterfaces;


	/**
	 * Create a new BshScriptFactory for the given script source.
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 * @param scriptInterfaces the Java interfaces that the scripted object
	 * is supposed to implement
	 */
	public BshScriptFactory(String scriptSourceLocator, Class[] scriptInterfaces) {
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = scriptInterfaces;
	}


	public String getScriptSourceLocator() {
		return this.scriptSourceLocator;
	}

	public Class[] getScriptInterfaces() {
		return this.scriptInterfaces;
	}

	/**
	 * BeanShell scripts do require a config interface.
	 */
	public boolean requiresConfigInterface() {
		return true;
	}

	/**
	 * Load and parse the BeanShell script via BshScriptUtils.
	 * @see BshScriptUtils#createBshObject(String, Class[])
	 */
	public Object getScriptedObject(ScriptSource actualScriptSource, Class[] actualInterfaces)
			throws IOException, ScriptCompilationException {
		try {
			return BshScriptUtils.createBshObject(actualScriptSource.getScriptAsString(), actualInterfaces);
		}
		catch (EvalError ex) {
			throw new ScriptCompilationException("Could not compile BeanShell script: " + actualScriptSource, ex);
		}
	}

}
