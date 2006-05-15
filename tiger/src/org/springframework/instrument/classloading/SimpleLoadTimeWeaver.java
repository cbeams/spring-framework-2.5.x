package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;

/**
 * Intended for use only in simple environments, such as an IDE.
 * 
 * @author Rod Johnson
 * 
 */
public class SimpleLoadTimeWeaver extends AbstractLoadTimeWeaver {

	private InstrumentableClassLoader instrumentableClassLoader;

	
	public void setAspectJWeavingEnabled(boolean flag) {
//		if (flag == true && !instrumentableClassLoader.isAspectJWeavingEnabled()) {
//			instrumentableClassLoader.setAspectJWeavingEnabled(true);
//		}
	}

	public SimpleLoadTimeWeaver() {
		instrumentableClassLoader = new InstrumentableClassLoader(getContextClassLoader());
	}

	public ClassLoader getInstrumentableClassLoader() {
		return instrumentableClassLoader;
	}

	public void addClassFileTransformer(final ClassFileTransformer classFileTransformer) {
		instrumentableClassLoader.addTransformer(classFileTransformer);
	}

	//
	// public void addClassNameToExcludeFromWeaving(String className) {
	// instrumentableClassLoader.addClassNameToExcludeFromUndelegation(className);
	// }
	//	
	// public void setExplicitInclusions(Collection<String> explicitClassNames)
	// {
	// instrumentableClassLoader.setExplicitInclusions(explicitClassNames);
	// }
}
