/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.springframework.metadata.MetadataCompiler;
import org.springframework.metadata.bcel.BcelAttributeWriter;
import org.springframework.metadata.qdox.QDoxMetadataCompiler;

/**
 * A custom Ant task to run the metadata compiler using the following syntax.
 * <metadataCompiler sourcepath="${src}/main"
 * 			 classpath="${build}/classes"
 * 			 destdir="${dist}"
 *           attributepackages="examples.attributes">
 * 
 * It also is a helper class to the MetadataCompilerAdapter
 * 
 * @since Oct 15, 2003
 * @author Mark.Pollack
 */
public class MetadataCompilerTask extends Task {

	/**
	 * This helper class is used to manage the source files to be processed.
	 */
	public static class SourceFile {
		/** The source file */
		private File file;

		public SourceFile() {
		}
		public SourceFile(File file) {
			this.file = file;
		}

		/**
		 * Set the source file.
		 *
		 * @param file the source file.
		 */
		public void setFile(File file) {
			this.file = file;
		}

		/**
		 * Get the source file.
		 *
		 * @return the source file.
		 */
		public File getFile() {
			return file;
		}

		public String toString() {
			return file.toString();
		}
	}

	private Path sourcePath = null;

	/**
	 * The packages that contain attribute.  Used to prepend to
	 * the classname given as an attribute so as not to require
	 * using the fully qualified classname.
	 */
	private String attributePackages = null;

	/**
	 * Logging level if using internal logging package...
	 */
	private String loglevel = "INFO";

	private File destDir = null;
	private Vector sourceFiles = new Vector();
	private Path classpath = null;
	private Vector fileSets = new Vector();
	
	/**
	 * Specify where to find source file
	 *
	 * @param src a Path instance containing the various source directories.
	 */
	public void setSourcepath(Path src) {
		if (sourcePath == null) {
			sourcePath = src;
		} else {
			sourcePath.append(src);
		}
	}

	/**
	 * A comma delimited list of attribute packages name that will be
	 * prepended in order to create a fully qualified classname.
	 * @param packages
	 */
	public void setAttributePackages(String packages) {
		attributePackages = packages;
	}


	/**
	 * Create a path to be configured with the locations of the source
	 * files.
	 *
	 * @return a new Path instance to be configured by the Ant core.
	 */
	public Path createSourcepath() {
		if (sourcePath == null) {
			sourcePath = new Path(project);
		}
		return sourcePath.createPath();
	}

	/**
	 * Adds a reference to a CLASSPATH defined elsewhere.
	 *
	 * @param r the reference containing the source path definition.
	 */
	public void setSourcepathRef(Reference r) {
		createSourcepath().setRefid(r);
	}

	/** 
	 * Set the directory where the Javadoc output will be generated.
	 *
	 * @param dir the destination directory.
	 */
	public void setDestdir(File dir) {
		destDir = dir;
	}

	/**
	 * Adds a fileset.
	 *
	 * <p>All included files will be added as sourcefiles.  The task
	 * will automatically add
	 * <code>includes=&quot;**&#47;*.java&quot;</code> to the
	 * fileset.</p>
	 *
	 * @since 1.5
	 */
	public void addFileset(FileSet fs) {
		fileSets.addElement(fs);
	}

	/**
	 * Set the classpath to be used for this javadoc run.
	 * 
	 * @param path an Ant Path object containing the compilation 
	 *        classpath.
	 */
	public void setClasspath(Path path) {
		if (classpath == null) {
			classpath = path;
		} else {
			classpath.append(path);
		}
	}

	/**
	 * Create a Path to be configured with the classpath to use
	 *
	 * @return a new Path instance to be configured with the classpath.
	 */
	public Path createClasspath() {
		if (classpath == null) {
			classpath = new Path(project);
		}
		return classpath.createPath();
	}

	/**
	 * Adds a reference to a CLASSPATH defined elsewhere.
	 *
	 * @param r the reference to an instance defining the classpath.
	 */
	public void setClasspathRef(Reference r) {
		createClasspath().setRefid(r);
	}

	public void execute() throws BuildException {

		log("***********************", Project.MSG_DEBUG);
		log("*Compiling Attributes *", Project.MSG_DEBUG);

		log("* sourcePath = " + sourcePath, Project.MSG_DEBUG);
		log("* destDir = " + destDir, Project.MSG_DEBUG);
		log("* attributePackages = " + attributePackages, Project.MSG_DEBUG);

		if (sourcePath == null) {
			throw new BuildException("sourcepath must not be null");
		}

		if (classpath == null) {
			classpath = Path.systemClasspath;
		} else {
			//Why doesn't this work with "ignore" as in the original ant task from which
			//this was copied?
			//classpath = classpath.concatSystemClasspath("ignore");
			classpath = classpath.concatSystemClasspath();
		}
		log("*Classpath = " + classpath, Project.MSG_DEBUG);
		log("*                                   *", Project.MSG_DEBUG);
		log("*************************************", Project.MSG_DEBUG);
		//When running from Ant, if attrib4j.jar and bcel.jar are in ANT_HOME/lib
		//then these jars will be present on the Path.systemClasspath.
		//Set the doclet classpath equal to the same thing.

		MetadataCompiler compiler = new QDoxMetadataCompiler();
		Map config = new HashMap();

		String[] paths = sourcePath.list();
		//TODO only using first in the path...
		compiler.setSourceDirectory(paths[0]);
		//config.put(QDoxMetadataCompiler.SOURCEDIR, paths[0]);

		String[] attribPackages = convertToArray(attributePackages);
		compiler.setAttributePackages(attribPackages);
		//config.put(QDoxMetadataCompiler.METADATAPACKAGES, attribPackages);

		compiler.setDestinationDirectory(destDir.getName());	
		//config.put(QDoxMetadataCompiler.DESTDIR, destDir.getName());

		compiler.setClassPath(classpath.toString());
		//config.put(QDoxMetadataCompiler.CLASSPATH, classpath.toString());

		throw new UnsupportedOperationException("Fix up an AttributeWriter implementation");
		//compiler.setAttributeWriter(new BcelAttributeWriter());
		
		//compiler.initialize(config);
		//compiler.compile();

	}


	/**
	 * Silly utility method to convert a comma delimited list to an 
	 * array.  If the input string is null the returned array is null.
	 * If no token can be generated from the string, null is returned.
	 * @param packages comma delimited list of package names.
	 * @return converted to string array.
	 */
	private String[] convertToArray(String packages) {
		if (packages == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(packages, ",");
		ArrayList list = new ArrayList();
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		if (list.size() > 0) {
			return (String[]) list.toArray(new String[list.size()]);
		} else {
			return null;
		}

	}

	/**
	 * Setup the MetadataCompilerTask when used as a compiler
	 * adapter.
	 * @param javac The Javac task
	 * @return null if everything is ok.
	 */
	public String initializeMetadataTask(Javac javac) {
		if (null == javac) {
			return "null javac"; 
		}
		setTaskName("javac-metadata");		
		setProject(javac.getProject());
		setLocation(javac.getLocation());

		//TODO investigat this setting more.
		if (javac.getSourcepath() == null) {
			setSourcepath(javac.getSrcdir()); 
		} else {
			setSourcepath(javac.getSourcepath());	
		}

		setClasspath(javac.getClasspath());
    	
		//TODO investigate, seems wierd stuff to set the classpath 
		Path destPath = new Path(javac.getProject());
		destPath.setLocation(javac.getDestdir());
		setClasspath(destPath);
		
		setDestdir(javac.getDestdir());
		
		setAttributePackages(javac.getProject().getProperty("attributepackages"));

    	 
		return null;
 
	}

}
