/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

/**
 * Class used to find the current JVM version.
 * Usually we want to find if we're in a 1.4 or higher JVM.
 * Spring does not support 1.2 JVMs.
 * @author Rod Johnson
 * @version $Id: JdkVersion.java,v 1.1 2004-01-31 10:14:28 johnsonr Exp $
 */
public class JdkVersion {
	
	public static final int JAVA_13 = 0;
	
	public static final int JAVA_14 = 1;
	
	public static final int JAVA_15 = 2;
	
	private static int majorJavaVersion = JAVA_13;
	
	static {
		String javaVersion = System.getProperty("java.version");
		// Should look like "1.4.1_02"
		if (javaVersion.indexOf("1.4") != -1) {
			majorJavaVersion = JAVA_14;
		}
		else if (javaVersion.indexOf("1.5") != -1) {
			majorJavaVersion = JAVA_15;
		}
		// else leave as 1.3 default
	}
	
	/**
	 * Get the major version code. This means we can do things like
	 * if getJavaVersion() >= JAVA_14
	 * @return a code comparable to the JAVA_XX codes in this
	 * class. 
	 */
	public static int getMajorJavaVersion() {
		return majorJavaVersion;
	}

}
