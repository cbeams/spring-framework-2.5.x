/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.bcel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;

//import org.apache.bcel.classfile.Attribute;
//import org.apache.bcel.classfile.ClassParser;
//import org.apache.bcel.classfile.ConstantPool;
//import org.apache.bcel.classfile.Field;
//import org.apache.bcel.classfile.JavaClass;
//import org.apache.bcel.classfile.Method;
//import org.apache.bcel.classfile.Unknown;
//import org.apache.bcel.classfile.Utility;
//import org.apache.bcel.generic.ClassGen;
//import org.apache.bcel.generic.ConstantPoolGen;
//import org.apache.bcel.generic.FieldGen;
//import org.apache.bcel.generic.MethodGen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeParser;
import org.springframework.metadata.AttributeWriter;
import org.springframework.metadata.support.AttributeCreator;
import org.springframework.metadata.support.AttributeDefinition;
import org.springframework.metadata.support.DotNetAttributeParser;
import org.springframework.metadata.support.JavaDocTags;

/**
 * The Spring implementation for adding metadata to classes.  This uses
 * the bytecode library BCEL to add the <b>TEXT</b> description of 
 * the attribute contained in the javadoc tag to the bytecode of the
 * target class. 
 * @author Mark Pollack
 * @since Sep 28, 2003
 * 
 */
public abstract class BcelAttributeWriter implements AttributeWriter {
//
//	/**
//	 * The class that will be enchanced with metadata information.
//	 */
//	private String targetClass;
//
//	/**
//	 * The logging instance.
//	 */
//	private final Log log = LogFactory.getLog(getClass());
//
//	/**
//	 * The classPath used to create the URLClassLoader to resolve
//	 * locating .class files.
//	 */
//	private String classPath;
//	/**
//	 * The classloader used to resolve .class files.
//	 */
//	private URLClassLoader classLoader;
//
//	protected ClassGen classGen;
//
//	protected ConstantPool constantPool;
//
//	protected ConstantPoolGen constantPoolGen;
//
//	protected JavaClass javaClass;
//
//	/**
//	 * {@inheritdoc}
//	 * @param packages {@inheritdoc}
//	 */
//	public void setAttributePackages(String[] packages) {
//		//This is the class that needs the package information.
//		AttributeCreator.setAttributePackages(packages);
//	}
//	
//	/**
//	 * {@inheritdoc}
//	 * @param cp {@inheritdoc}
//	 */
//	public void setClassPath(String cp) {
//		this.classPath = cp;
//	}
//
//	/**
//	 * {@inheritDoc}
//	 * @param targetClazz {@inheritdoc}
//	 */
//	public void initializeClass(final String targetClazz) {
//		this.targetClass = targetClazz;
//		initialize();
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param classAttribute {@inheritdoc}
//	 */
//	public void addClassAttribute(String classAttribute) {
//		//TODO: make this plugable
//		log.debug("Raw Attribute Text = " + classAttribute);
//		AttributeParser parser = new DotNetAttributeParser();
//		AttributeDefinition pa = parser.getAttributeDefinition(classAttribute);
//		if (pa.isValid()) {
//			//The attribute text had the correct syntax, now check
//			//that the classname supplied can be resolved.
//			if (!JavaDocTags
//				.getJavadocClassTags()
//				.contains(pa.getClassname())) {
//				AttributeCreator.resolveClassname(pa.getClassname());
//
//				//Now store the text in the bytecode.
//				byte[] serializedAttribute = pa.getAttributeText().getBytes();
//
//				int nameIndex = this.constantPoolGen.addUtf8("Custom");
//
//				Attribute attr =
//					new Unknown(
//						nameIndex,
//						serializedAttribute.length,
//						serializedAttribute,
//						this.constantPoolGen.getConstantPool());
//				log.debug("Adding Attribute to class " + classGen.getClassName());
//				this.classGen.addAttribute(attr);
//			}
//		}
//
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param method {@inheritdoc}
//	 * @param attributeText {@inheritdoc}
//	 */
//	public void addMethodAttribute(
//		java.lang.reflect.Method targetMethod,
//		String attributeText) {
//
//		AttributeParser parser = new DotNetAttributeParser();
//		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
//		if (pa.isValid()) {
//			//The attribute text had the correct syntax, now check
//			//that the classname supplied can be resolved.
//			if (!JavaDocTags
//				.getJavadocMethodTags()
//				.contains(pa.getClassname())) {
//
//				AttributeCreator.resolveClassname(pa.getClassname());
//
//				byte[] serializedAttribute = pa.getAttributeText().getBytes();
//
//				String javaMethodSig = getJavaMethodSignature(targetMethod);
//
//				//get list of methods in the class from bcel
//				Method[] classfileMethod = this.classGen.getMethods();
//				for (int i = 0; i < classfileMethod.length; i++) {
//					//log.debug(
//					//	"classfileMethod = " + classfileMethod[i].getName());
//					//match classname
//					if (classfileMethod[i]
//						.getName()
//						.equals(targetMethod.getName())) {
//						//log.debug(
//						//	"BCEL method sig = "
//						//		+ classfileMethod[i].getSignature());
//						//log.debug("Java method sig = " + javaMethodSig);
//						if (classfileMethod[i]
//							.getSignature()
//							.compareTo(javaMethodSig)
//							== 0) {
//						}
//
//						log.debug(
//							"matched methodname = " + targetMethod.getName());
//
//						MethodGen methodGen =
//							new MethodGen(
//								classfileMethod[i],
//								this.javaClass.getClassName(),
//								this.constantPoolGen);
//
//						int nameIndex = this.constantPoolGen.addUtf8("Custom");
//						Attribute attr =
//							new Unknown(
//								nameIndex,
//								serializedAttribute.length,
//								serializedAttribute,
//								this.constantPoolGen.getConstantPool());
//						methodGen.addAttribute(attr);
//						Method newMethod = methodGen.getMethod();
//						this.classGen.replaceMethod(
//							classfileMethod[i],
//							newMethod);
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * {@inheritdoc}
//	 * @param targetField {@inheritdoc}
//	 * @param attributeText {@inheritdoc}
//	 */
//	public void addFieldAttribute(
//		java.lang.reflect.Field targetField,
//		String attributeText) {
//		AttributeParser parser = new DotNetAttributeParser();
//		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
//		if (pa.isValid()) {
//			//The attribute text had the correct syntax, now check
//			//that the classname supplied can be resolved.
//			if (!JavaDocTags
//				.getJavadocFieldTags()
//				.contains(pa.getClassname())) {
//				AttributeCreator.resolveClassname(pa.getClassname());
//
//				byte[] serializedAttribute = pa.getAttributeText().getBytes();
//				//get list of fields inthe class from bcel.
//				Field[] classfileField = this.classGen.getFields();
//				for (int i = 0; i < classfileField.length; i++) {
//					if (classfileField[i]
//						.getName()
//						.equals(targetField.getName())) {
//						FieldGen fieldGen =
//							new FieldGen(
//								classfileField[i],
//								this.constantPoolGen);
//						int nameIndex = this.constantPoolGen.addUtf8("Custom");
//						Attribute attr =
//							new Unknown(
//								nameIndex,
//								serializedAttribute.length,
//								serializedAttribute,
//								this.constantPoolGen.getConstantPool());
//						fieldGen.addAttribute(attr);
//						Field newField = fieldGen.getField();
//						this.classGen.replaceField(classfileField[i], newField);
//					}
//				}
//			}
//		}
//	}
//	/**
//	 * A utility method to return the JVM like signature from the
//	 * java.lang.reflect signature.
//	 * @param method The java.lang.reflect.Method to process.
//	 * @return The JVM signature.
//	 */
//	public static String getJavaMethodSignature(
//		java.lang.reflect.Method method) {
//		Class[] methodParamClass = method.getParameterTypes();
//		String[] methodParamTypes = new String[methodParamClass.length];
//
//		//get the unqualified classname.....
//		StringBuffer sb = new StringBuffer("(");
//		for (int i = 0; i < methodParamTypes.length; i++) {
//			String fqn = methodParamClass[i].getName();
//			sb.append(Utility.getSignature(fqn));
//		}
//		sb.append(")");
//		Class returnType = method.getReturnType();
//		sb.append(Utility.getSignature(returnType.getName()));
//		String javaMethodSig = sb.toString();
//		return javaMethodSig;
//	}
//
//	/**
//	 * Write out the modified bytecode to the destination directory.
//	 * @param destDir the location where to write the new bytecode
//	 */
//	public void finishClass(String destDir) {
//		try {
//			this.classGen.setConstantPool(this.constantPoolGen);
//			JavaClass newJavaClass = this.classGen.getJavaClass();
//
//			String path =
//				destDir
//					+ "/"
//					+ newJavaClass.getClassName().replace('.', '/')
//					+ ".class";
//			File f = new File(path);
//			File parentFile = f.getParentFile();
//
//			boolean exists = parentFile.exists();
//			if (!exists) {
//				//Directory does not exist
//				//create all directories in the path
//				boolean success = parentFile.mkdirs();
//				if (!success) {
//					//TODO: Introduce exception handling into interface?
//					//throw new AttributeException("Could not create directory " + parentFile.toString());
//					log.debug(
//						"Could not create directory to write new .class file. "
//							+ parentFile.toString());
//				}
//			}
//
//			FileOutputStream fout = new FileOutputStream(path);
//			DataOutputStream out = new DataOutputStream(fout);
//			log.debug("Writing modified .class file to " + path);
//			newJavaClass.dump(out);
//			fout.close();
//		} catch (IOException ioEx) {
//			ioEx.printStackTrace();
//		}
//
//	}
//
//	/**
//	 * Load the bytecode for the java class into BCEL classes.  Modify
//	 * them to remove any existing attributes.  This is done so that we
//	 * can keep the modified bytecode in the same location as the original
//	 * bytecode.  
//	 * TODO There might be other options here...
//	 * @throws AttributeException
//	 */
//	private void initialize() throws AttributeException {
//		try {
//			String fqn = this.targetClass;
//			String classFileName = fqn.replace('.', '/') + ".class";
//			log.debug("Loading class from filename = " + classFileName);
//			if (this.classPath == null) {
//				this.classPath = System.getProperty("java.class.path");
//			}
//			AttributeCreator.setClasspath(classPath);
//
//			if (classLoader == null) {
//			//String defaultClasspath = System.getProperty("java.class.path");
//			log.debug("Using classpath = " + this.classPath);
//			this.classLoader =
//				new URLClassLoader(
//					AttributeCreator.pathArrayToURLArray(this.classPath),
//					BcelAttributeWriter.class.getClassLoader());
//			}
//			
//			InputStream clsInputStream =
//				this.classLoader.getResourceAsStream(classFileName);
//
//			if (clsInputStream == null) {
//				throw new AttributeException(
//					"Could not create input stream from classfile name = "
//						+ classFileName + " using classpath = " + this.classPath);
//			}
//			this.javaClass =
//				new ClassParser(clsInputStream, classFileName).parse();
//			log.debug(
//				"Created BCEL JavaClass = " + this.javaClass.getClassName());
//			this.constantPool = this.javaClass.getConstantPool();
//			this.constantPoolGen = new ConstantPoolGen(this.constantPool);
//			this.classGen = new ClassGen(this.javaClass);
//
//			//remove all existing attributes
//
//			//Remove class attributes
//			Attribute[] attributes = this.classGen.getAttributes();
//			if (attributes != null) {
//				for (int i = 0; i < attributes.length; i++) {
//					classGen.removeAttribute(attributes[i]);
//				}
//			}
//
//			//Remove method attributes
//			Method[] classfileMethod = this.classGen.getMethods();
//			for (int i = 0; i < classfileMethod.length; i++) {
//				if (classfileMethod[i].getAttributes().length != 0) {
//					MethodGen methodGen =
//						new MethodGen(
//							classfileMethod[i],
//							this.javaClass.getClassName(),
//							this.constantPoolGen);
//					methodGen.removeAttributes();
//					Method newMethod = methodGen.getMethod();
//					this.classGen.replaceMethod(classfileMethod[i], newMethod);
//				}
//			}
//
//			//Remove field atttributes
//			Field[] classfileField = this.classGen.getFields();
//			for (int i = 0; i < classfileField.length; i++) {
//				if (classfileField[i].getAttributes().length != 0) {
//					FieldGen fieldGen =
//						new FieldGen(classfileField[i], this.constantPoolGen);
//					fieldGen.removeAttributes();
//					Field newField = fieldGen.getField();
//					this.classGen.replaceField(classfileField[i], newField);
//				}
//			}
//
//		} catch (IOException e) {
//			throw new AttributeException(
//				"Could not create an instance of BCELClassAnnotator",
//				e);
//		}
//
//	}

}
