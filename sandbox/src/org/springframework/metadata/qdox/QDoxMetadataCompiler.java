/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.qdox;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeParser;
import org.springframework.metadata.bcel.BcelAttributeWriter;
import org.springframework.metadata.support.AbstractMetadataCompiler;
import org.springframework.metadata.support.AttributeCreator;
import org.springframework.metadata.support.DotNetAttributeParser;
import org.springframework.metadata.support.JavaDocTags;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * The QDox based implementation of the MetadataCompiler
 * 
 * 
 * @author Mark Pollack
 * @since Oct 13, 2003
 */
public class QDoxMetadataCompiler extends AbstractMetadataCompiler {

	/*
	public final static String DESTDIR = "destdir";

	public final static String CLASSPATH = "classpath";

	public final static String METADATAPACKAGES = "metadatapackages";

	public final static String SOURCEDIR = "sourcedir";

	private Map configMap = new HashMap();
	*/
	
	/**
	 * The classloader used to resolve .class files.
	 */
	private URLClassLoader classLoader;

	/**
	 * The logging instance.
	 */
	private final Log log = LogFactory.getLog(getClass());
	
	
	//private AttributeWriter writer;
	
	//private String srcDir;
	
	/**
	 * 
	 * @see org.springframework.metadata.MetadataCompiler#initialize(java.util.Map)
	 */
	/*
	public void initialize(Map config) throws AttributeException {
		this.configMap = config;
		writer = new BcelAttributeWriter();
		writer.setAttributePackages((String[]) configMap.get(METADATAPACKAGES));
		writer.setClasspath((String) configMap.get(CLASSPATH));
		srcDir = (String) configMap.get(SOURCEDIR);
		//TODO validate values.
	}
	*/

	/* (non-Javadoc)
	 * @see org.springframework.metadata.MetadataCompiler#compile()
	 */
	public void compile() throws AttributeException {
		validateConfiguration();
		//pass on configuration information to writer
		getAttributeWriter().setAttributePackages(getAttributePackages());
		getAttributeWriter().setClassPath(getClassPath());
		
		JavaDocBuilder builder = new JavaDocBuilder();
		//AttributeWriter 

		//TODO this mechanism is not working to write only those files that
		//were modified.
		boolean modifiedClassfile = false;
		AttributeParser parser = new DotNetAttributeParser();
		
			builder.addSourceTree(new File(getSourceDirectory()));
			JavaSource[] jSource = builder.getSources();
			for (int i = 0; i < jSource.length; i++) {
				JavaClass[] jClass = jSource[i].getClasses();
				modifiedClassfile = false;
				for (int j = 0; j < jClass.length; j++) {

					//class level tags
					DocletTag[] jTags = jClass[j].getTags();
					Set jdocTags = JavaDocTags.getJavadocClassTags();
					for (int k = 0; k < jTags.length; k++) {
						String classAttribute = jTags[k].getName() + " " + jTags[k].getValue();
						//Tell the writer to add the class attribute.
						if (!jdocTags.contains(jTags[k].getName())) {
							if (modifiedClassfile == false) {
								//Tell the writer what class we are going to modify
								getAttributeWriter().initializeClass(jClass[j].getFullyQualifiedName());
								modifiedClassfile = true;
							}
							getAttributeWriter().addClassAttribute(classAttribute);
						}
					}
					//Get an instance of the java.lang.Class so we can
					//ask it for its methods.
					Class targetClass = null;
					try {
						targetClass =
							getClass(jClass[j].getFullyQualifiedName());
					} catch (ClassNotFoundException e) {
						throw new AttributeException(
							"Could not resolve the class "
								+ jClass[j].getFullyQualifiedName());
					} catch (NoClassDefFoundError e) {
						throw new AttributeException(
							"Could not create the class "
								+ jClass[j].getFullyQualifiedName(),e);
					}
					//method level tags
					JavaMethod[] jMethod = jClass[j].getMethods();
					for (int a = 0; a < jMethod.length; a++) {
						Class[] parameterTypes = convertParams(jMethod[a]);

						DocletTag[] methodTags = jMethod[a].getTags();
						Set jdocMTags = JavaDocTags.getJavadocMethodTags();
						for (int b = 0; b < methodTags.length; b++) {
							if (!jdocMTags.contains(methodTags[b].getName())) {
								try {
									Method targetMethod =
										targetClass.getMethod(
											jMethod[a].getName(),
											parameterTypes);
									if (modifiedClassfile == false) {
										//Tell the writer what class we are going to modify
										log.debug("calling writer.initializeclass");
										getAttributeWriter().initializeClass(jClass[j].getFullyQualifiedName());
										modifiedClassfile = true;
									}											
									getAttributeWriter().addMethodAttribute(
										targetMethod,
										methodTags[b].getName()
											+ " "
											+ methodTags[b].getValue());
								} catch (NoSuchMethodException e) {
									throw new AttributeException(
										"Could not resolve method from qdox name = "
											+ jMethod[a].getName(),
										e);
								}
								
							}
						}
					}
					
					//TODO Fields...
				}
				if (modifiedClassfile == true) {
					getAttributeWriter().finishClass(getDestinationDirectory());
				}
			}


	}


	/**
	 * TODO switch to later version of qdox.
	 * @param method
	 * @return
	 */
	private Class[] convertParams(JavaMethod method) {
		Class[] returnClass = null;
		List clazzList = new ArrayList();
		JavaParameter[] params = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			Class clazz = convertToClass(params[i].getType().getValue());
			clazzList.add(clazz);
			/*
			log.debug(
				"Method "
					+ method.getName()
					+ " parameter class = "
					+ clazz.getName());
					*/
		}

		return (Class[]) clazzList.toArray(new Class[0]);
	}

	/**
	 * Silly utility method to convert primitive type to their class equivalents
	 * @param qdoxValue
	 * @return
	 */
	private Class convertToClass(String qdoxValue) {
		Class rClazz = null;
		if (primToClazzMap.containsKey(qdoxValue)) {
			rClazz = (Class) primToClazzMap.get(qdoxValue);
		} else {
			try {
				rClazz = getClass(qdoxValue);
			} catch (ClassNotFoundException e) {
				throw new AttributeException(
					"could not create class from qdox value = " + qdoxValue,
					e);
			}
		}
		return rClazz;
	}



	private Class getClass(String className) throws ClassNotFoundException {
		System.out.println("QDoxMetadataCompiler creating classname = [" + className + "]");
		if (this.classLoader == null) {
			String cp = getClassPath();
			if (cp == null) {
				cp = System.getProperty("java.class.path");
			} 
			this.classLoader =
				new URLClassLoader(
					AttributeCreator.pathArrayToURLArray(cp),
					QDoxMetadataCompiler.class.getClassLoader());
		}
		return classLoader.loadClass(className);
	}

	/**
	 * Check that the source and destination directories are
	 * not null.  Check that the attribute writer is not null.
	 *
	 */
	private void validateConfiguration() {
		if (getSourceDirectory() == null) {
			throw new AttributeException("QDoxMetadataCompiler source directory can not be null");
		}
		if (getDestinationDirectory() == null) {
			throw new AttributeException("QDoxMetadataCompiler destination directory can not be null");
		}
		if (getAttributeWriter() == null) {
			log.info("No AttributeWriter specified, using BcelAttributeWriter");
			throw new UnsupportedOperationException("Fix up a concrete attributewriter");
			//setAttributeWriter(new BcelAttributeWriter());	
		}
	}

	private static Map primToClazzMap = new HashMap();
	static {
		primToClazzMap.put("boolean", boolean.class);
		primToClazzMap.put("char", char.class);
		primToClazzMap.put("byte", byte.class);
		primToClazzMap.put("short", short.class);
		primToClazzMap.put("int", int.class);
		primToClazzMap.put("long", long.class);
		primToClazzMap.put("float", float.class);
		primToClazzMap.put("double", double.class);

	}
}
