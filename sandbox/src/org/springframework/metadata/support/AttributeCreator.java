/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.support;

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.metadata.AttributeException;

/**
 * This is a helper class to create an instance of an Attribute
 * given the attribute classname as specified in the javadoc comment.
 * In order to reduce typing when specifying attributes, only parts of the
 * fully qualified classname (FQN) can be used. 
 * 
 * If the fully qualified classname ends in 'Attribute', then you can
 * leave the word 'Attribute' off of the javadoc comment when applying
 * the attribute.  This helper class will append it to resolve the FQN. 
 *  
 * If you do not specify the FQN you can specify a 
 * list of package names that will be prepended to the classname that was
 * used when applying the attribute.  In addition to prepending these package
 * names, the word 'Attribute' will be appened to resolve the FQN.
 * abbreviated classname.
 * 
 * TODO maybe make the methods static...
 * 
 * @author Mark Pollack
 * @since Oct 6, 2003
 */
public class AttributeCreator {

	/**
	 * The storage for the names of packages to use when looking for
	 * an attribute to instantiate.
	 */
	private static String[] attributePackages;

	private static String classPath;

	private static URLClassLoader classLoader;

	/**
	 * The logging instance.
	 */
	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Do nothing ctor
	 *
	 */
	public AttributeCreator() {

	}

	/**
	 * Create an attribute instance given the description in a 
	 * AttributeDefinition class.
	 * @param pa A description of the attribute to create.
	 * @return the corresponding attribute instance.  If it is not possible
	 * to create the instance, null is returned
	 * TODO: Should null be returned?
	 */
	public Object createAttribute(AttributeDefinition pa) {
		if (pa == null) {
			throw new IllegalArgumentException("Can not pass null value for AttributeDefinition");
		}
		Class customAttributeClass = null;
		String classname = pa.getClassname();
		customAttributeClass = resolveClassname(classname);

		//check for constructor that matches parameter numbers
		Constructor[] ctors = customAttributeClass.getDeclaredConstructors();
		if (ctors.length == 0) {
			//TODO we might have an interface, use dynamic proxy.
			throw new AttributeException("Do not yet support interface only attribute definitions");
		} else {
			return createFromConstructor(customAttributeClass, pa);
		}

	}

	public static Class resolveClassname(String classname) {
		Class customAttributeClass = null;
		List candidateNames = new ArrayList();
		candidateNames.add(classname);
		//TODO do we need to explicit in specifying the classloader?

		if (classPath == null) {
			classPath = System.getProperty("java.class.path");
		}
		if (classLoader == null) {
			classLoader =
				new URLClassLoader(
					AttributeCreator.pathArrayToURLArray(classPath),
					AttributeCreator.class.getClassLoader());
		}

		try {
			//Try with the classname as specified in the attribute text.
			customAttributeClass =
				classLoader.loadClass(candidateNames.get(0).toString());
		} catch (ClassNotFoundException e) {
			//Try appending the word 'Attribute' to the classname
			candidateNames.add(classname + "Attribute");
			try {
				customAttributeClass =
					classLoader.loadClass(candidateNames.get(1).toString());
			} catch (ClassNotFoundException e1) {
				//Try prepending package names if any
				if (attributePackages != null) {
					customAttributeClass =
						createFromPackageSearch(classname, candidateNames);
				} else {

					throw new AttributeException(
						"Could not create class using classnames "
							+ candidateNames
							+ " and classpath = "
							+ classPath);
				}
			}

		}
		return customAttributeClass;
	}

	/**
	 * Packages were attributes are expected to be located are prepended
	 * to the candidate classname in addition to appending the word
	 * "Attribute" to the candidate classname.
	 * 
	 * @param classname  The 'base' name that will be used to search for 
	 * a matching attribute class.
	 * @param candidateNames  The running list of classnames used to
	 * instantiate the java.lang.Class object.
	 * @return a matching java.lang.Class using the above search algorithm.
	 */
	private static Class createFromPackageSearch(
		String baseClassname,
		List candidateNames) {
		boolean foundClass = false;
		Class returnClass = null;
		Exception returnException = null;

		String candidateName = null;
		if (attributePackages == null) {
			foundClass = false;
		} else {
			for (int i = 0; i < attributePackages.length; i++) {
				try {
					candidateName = attributePackages[i] + "." + baseClassname;
					candidateNames.add(candidateName);
					returnClass = classLoader.loadClass(candidateName);
					foundClass = true;
					break;
				} catch (ClassNotFoundException e) {
					try {
						candidateName =
							attributePackages[i]
								+ "."
								+ baseClassname
								+ "Attribute";
						candidateNames.add(candidateName);
						returnClass = classLoader.loadClass(candidateName);
						foundClass = true;
						break;
					} catch (ClassNotFoundException e2) {
						//fall through
					}
				}
			}
		}
		if (!foundClass) {
			throw new AttributeException(
				"Could not create class using classnames " + candidateNames);
		} else {
			return returnClass;
		}
	}

	/**
	 * Set the packages to prepend to a class search when trying to
	 * create an attribute.
	 * @param attributePackages An array of package names to try and prefix
	 * to an attribute name specified in the javadoc comment.
	 */
	public static void setAttributePackages(String[] packages) {
		attributePackages = packages;
	}

	/**
	/**
	 * Set the classpath to use when trying to locate resolve classes.
	 * Not all implementations will need to call this method.
	 * @param cp A File.pathSeparator separated list of .jar
	 * and directories.  
	 */
	public static void setClasspath(String cp) {
		classPath = cp;
	}

	/**
	 * Create an instance of the class using the provided Class reference. 
	 * Use the constuctor arguments and the propererties in the AttributeDefinition
	 * class.  
	 * @param attributeClass The java.lang.Class to use for creating new
	 * instances.
	 * @param pa A AttributeDefinition class the contains the constructor and
	 * properties information to use when creating the new instance.
	 * @return a new instance.
	 */
	private Object createFromConstructor(
		Class attributeClass,
		AttributeDefinition pa) {

		Constructor[] ctors = attributeClass.getDeclaredConstructors();
		boolean okCtor = checkForPublicCtor(ctors);
		if (!okCtor) {
			throw new AttributeException(
				attributeClass.getName()
					+ " must have at least one public constructor");
		}

		Object attribObject = null;
		String[] attribCtorParams =
			(String[]) pa.getConstructorArgs().toArray(new String[0]);
		for (int i = 0; i < ctors.length; i++) {
			Class[] ctorParamTypes = ctors[i].getParameterTypes();
			if (ctorParamTypes.length == attribCtorParams.length) {
				Object[] ctorArgs = new Object[ctorParamTypes.length];
				for (int j = 0; j < ctorParamTypes.length; j++) {
					ctorArgs[j] =
						guessParam(ctorParamTypes[j], attribCtorParams[j]);
				}

				try {
					attribObject = ctors[i].newInstance(ctorArgs);
				} catch (Exception x) {
					//Ctor args did not match, keep trying.
					continue;
				}
				//Found a matching ctor, end the loop
				break;
			}
		}

		//See if we managed to create an instance.
		if (attribObject == null) {
			throw new AttributeException(
				"No constructor in "
					+ attributeClass.getName()
					+ " matched "
					+ pa.getConstructorArgs().toString());
		}

		//TODO: Should be use the bean class, too much coupling to main
		//stuff inside of spring?
		BeanWrapper beanWrapper = new BeanWrapperImpl(attribObject);
		beanWrapper.setPropertyValues(pa.getProperties());

		return beanWrapper.getWrappedInstance();

	}

	/**
	 * A simple loop to check that at least one of the defined constructors
	 * are public.
	 * @param ctors The array of class constructors
	 * @return true if there is at least one public constructor, false otherwise.
	 */
	private boolean checkForPublicCtor(Constructor[] ctors) {
		boolean okCtor = false;
		//TODO Maybe check this at parse time as well....
		for (int i = 0; i < ctors.length; i++) {
			if (Modifier.isPublic(ctors[i].getModifiers())) {
				okCtor = true;
				break;
			}
		}
		return okCtor;
	}

	/**
	 * Internal routine to guess (and create) the parameter instance from 
	 * the text passed in and the type it's supposed to be (from the 
	 * constructor). This method handles String, Boolean, Byte, Short,
	 * Integer, Long, Float, and Double parameter types.
	 *
	 * @param ctorParamType The class of the positional parameter in the
	 * attribute constructor.
	 * @param paramText The string value of this paramemter as given in
	 * in the Javadoc tag.
	 * @return An instance of the correct parameter class initialized to
	 * the value specified by the parameter text.
	 */
	protected static Object guessParam(Class ctorParamType, String paramText) {
		if (ctorParamType.equals(String.class)) {
			if (paramText.startsWith("\"") && paramText.endsWith("\"")) {
				return paramText.substring(1, paramText.length() - 1);
			}
			return null;
		}
		if (ctorParamType.equals(Boolean.class)
			|| ctorParamType.equals(boolean.class)) {
			if (paramText.equals("true")) {
				return new Boolean(true);
			}
			if (paramText.equals("false")) {
				return new Boolean(false);
			}
			return null;
		}
		if (ctorParamType.equals(Byte.class)
			|| ctorParamType.equals(byte.class)) {
			try {
				return new Byte(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}
		if (ctorParamType.equals(Short.class)
			|| ctorParamType.equals(short.class)) {
			try {
				return new Short(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}
		if (ctorParamType.equals(Integer.class)
			|| ctorParamType.equals(int.class)) {
			try {
				return new Integer(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}
		if (ctorParamType.equals(Long.class)
			|| ctorParamType.equals(long.class)) {
			try {
				return new Long(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}
		if (ctorParamType.equals(Float.class)
			|| ctorParamType.equals(float.class)) {
			try {
				return new Float(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}
		if (ctorParamType.equals(Double.class)
			|| ctorParamType.equals(double.class)) {
			try {
				return new Double(paramText);
			} catch (NumberFormatException pe) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Returns an array of URL objects that represent the 
	 * pathSeparator-separated list of directories passed in.
	 * 
	 * @param path a value of type 'String'
	 * @return a value of type 'URL[]'
	*/
	public static URL[] pathArrayToURLArray(String path) {
		URL[] urls = null;
		try {
			StringTokenizer st = new StringTokenizer(path, File.pathSeparator);

			int count = st.countTokens();
			urls = new URL[count];
			for (int i = 0; i < count; i++) {
				urls[i] = new File(st.nextToken()).toURL();
			}
		} catch (MalformedURLException malURLEx) {
			malURLEx.printStackTrace();
		}
		return urls;
	}

}
