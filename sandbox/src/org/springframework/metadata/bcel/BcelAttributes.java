/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.bcel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import org.apache.bcel.classfile.ClassParser;
//import org.apache.bcel.classfile.JavaClass;
//import org.apache.bcel.classfile.Unknown;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeParser;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.support.AttributeCreator;
import org.springframework.metadata.support.DotNetAttributeParser;

/**
 * Retrieve attributes stored in the .class file using the BCEL library
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * @since Sep 30, 2003
 */
public abstract class BcelAttributes implements Attributes {

//	/**
//	 * The BCEL in memory bytecode of a JavaClass.
//	 */
//	private JavaClass javaClass;
//
//	/**
//	 * Store the parsed BCEL JavaClasses under the FQN classname.
//	 */
//	private Map javaClassMap = new HashMap();
//
//	private String[] packages;
//
//	/**
//	 * The logging instance.
//	 */
//	private final Log log = LogFactory.getLog(getClass());
//
//	/**
//	 * A do nothing ctor.
//	 *
//	 */
//	public BcelAttributes() {
//
//	}
//
//	/**
//	 * @inheritdoc
//	 * @param pkgs {@inheritdoc}
//	 */
//	public void setAttributePackages(String[] pkgs) {
//		packages = pkgs;
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetClass {@inheritdoc}
//	 */
//	public Collection getAttributes(Class targetClass) {
//		return getAttributes(targetClass, null);
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetClass {@inheritdoc}
//	 * @param filter {@inheritdoc}
//	 */
//	public Collection getAttributes(Class targetClass, Class filter) {
//		ArrayList al = new ArrayList();
//		if (! isAnnotatable(targetClass)) {
//			return al;					
//		}
//		org.apache.bcel.classfile.Attribute[] attribs =
//			getJavaClass(targetClass).getAttributes();
//		for (int i = 0; i < attribs.length; i++) {
//			if (attribs[i] instanceof Unknown) {
//				Unknown unknownAttrib = (Unknown) attribs[i];
//				log.debug(
//					"Found Class attribute #"
//						+ i
//						+ " with name = "
//						+ unknownAttrib.getName());
//				byte[] serializedTextAttribute = unknownAttrib.getBytes();
//
//				String attributeText = new String(serializedTextAttribute);
//
//				Object customAttr = createObject(attributeText);
//
//				if (filter != null) {
//					if (filter.isInstance(customAttr)) {
//						al.add(customAttr);
//					}
//				} else {
//					al.add(customAttr);
//				}
//			}
//
//		}
//		return al;
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetMethod {@inheritdoc}
//	 */
//	public Collection getAttributes(Method targetMethod) {
//		return getAttributes(targetMethod, null);
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetMethod {@inheritdoc}
//	 * @param filter {@inheritdoc}
//	 */
//	public Collection getAttributes(Method targetMethod, Class filter) {
//		ArrayList al = new ArrayList();
//		if (! isAnnotatable(targetMethod.getDeclaringClass())) {
//			return al;					
//		}
//		org.apache.bcel.classfile.Method[] classfileMethod =
//			getJavaClass(targetMethod.getDeclaringClass()).getMethods();
//
//		for (int i = 0; i < classfileMethod.length; i++) {
//			if (classfileMethod[i].getName().equals(targetMethod.getName())) {
//				String javaMethodSig =
//					BcelAttributeWriter.getJavaMethodSignature(targetMethod);
//
//				if (classfileMethod[i].getSignature().compareTo(javaMethodSig)
//					== 0) {
//					org.apache.bcel.classfile.Attribute[] attribs =
//						classfileMethod[i].getAttributes();
//					log.debug("Found " + attribs.length + " method attributes");
//
//					for (int j = 0; j < attribs.length; j++) {
//						if (attribs[j] instanceof Unknown) {
//							Unknown unknownAttrib = (Unknown) attribs[j];
//
//							byte[] serializedTextAttribute =
//								unknownAttrib.getBytes();
//							String attributeText =
//								new String(serializedTextAttribute);
//
//							Object customAttr = createObject(attributeText);
//
//							if (filter != null) {
//								if (filter.isInstance(customAttr)) {
//									al.add(customAttr);
//								}
//							} else {
//								al.add(customAttr);
//							}
//						}
//					}
//				}
//			}
//		}
//		return al;
//
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetField {@inheritdoc}
//	 */
//	public Collection getAttributes(Field targetField) {
//		return getAttributes(targetField, null);
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetField {@inheritdoc}
//	 * @param filter {@inheritdoc}
//	 */
//	public Collection getAttributes(Field targetField, Class filter) {
//		ArrayList al = new ArrayList();
//		if (! isAnnotatable(targetField.getDeclaringClass())) {
//			return al;					
//		}
//		org.apache.bcel.classfile.Field[] classfileField =
//			getJavaClass(targetField.getDeclaringClass()).getFields();
//		for (int i = 0; i < classfileField.length; i++) {
//			if (classfileField[i].getName().equals(targetField.getName())) {
//				org.apache.bcel.classfile.Attribute[] attribs =
//					classfileField[i].getAttributes();
//				for (int j = 0; j < attribs.length; j++) {
//					if (attribs[j] instanceof Unknown) {
//						Unknown unknownAttrib = (Unknown) attribs[j];
//						byte[] serializedTextAttribute =
//							unknownAttrib.getBytes();
//						String attributeText =
//							new String(serializedTextAttribute);
//
//						Object customAttr = createObject(attributeText);
//						if (filter != null) {
//							if (filter.isInstance(customAttr)) {
//								al.add(customAttr);
//							}
//						} else {
//							al.add(customAttr);
//						}
//					}
//				}
//			}
//		}
//
//		return al;
//
//	}
//	/**
//	  * Create the object instance from the attribute txt.
//	  * @param attributeText the attribute text stored in the byte code.
//	  * @return the object created from the attribute text.
//	  */
//	private Object createObject(String attributeText) {
//		AttributeCreator creator = new AttributeCreator();
//		AttributeCreator.setAttributePackages(this.packages);
//		AttributeParser parser = new DotNetAttributeParser();
//		return creator.createAttribute(
//			parser.getAttributeDefinition(attributeText));
//	}
//
//	/**
//	 * Return the BCEL JavaClass for the corresponding java.lang.Class
//	 * The implementation caches the BCEL JavaClass under the 
//	 * fully qualified classname of the java.lang.Class.
//	 * @param targetClass The class that we would like to obtain
//	 * custom attributes from
//	 * @return The corresponding BCEL JavaClass object.
//	 */
//	private JavaClass getJavaClass(Class targetClass) {
//		String fqn = targetClass.getName();
//		if (!this.javaClassMap.containsKey(fqn)) {
//			ClassLoader loader = targetClass.getClassLoader();
//			if (loader == null) {
//				throw new AttributeException("Could not get class loader for class = " + targetClass);
//			}
//			String classFileName = fqn.replace('.', '/') + ".class";
//			try {
//				InputStream clsInput =
//					loader.getResourceAsStream(classFileName);
//				javaClassMap.put(
//					fqn,
//					new ClassParser(clsInput, classFileName).parse());
//			} catch (IOException ioEx) {
//				//TODO What to do here--repackage and throw?
//				ioEx.printStackTrace();
//			}
//		}
//		return (JavaClass) javaClassMap.get(fqn);
//	}
//
//	/**
//	 * If we know it is not possible to have put an attribute on built in
//	 * 'sealed' classes...
//	 * @param targetClass
//	 * @return
//	 */
//	private boolean isAnnotatable(Class targetClass) {
//		//log.debug("target class = " + targetClass.getName());
//		//log.debug("boolean = " + targetClass.getName().startsWith("java."));
//		if (targetClass.isPrimitive()) {
//			return false;
//		}
//		//TODO improve this logic
//		if (targetClass.getName().startsWith("java.")) {
//			return false;
//		}
//		return true;
//		 
//		
//		
//	}
}
