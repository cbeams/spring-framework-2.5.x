/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata;

/**
 * 
 * Different implementations of metadata compiler should implement
 * this interface.  There are setters for common configuration
 * data.  If in the course of implementing a new metadata compiler
 * additional configuration information is needed, this interface should
 * be modified accordingly.
 * 
 * @author Mark Pollack
 * @since Oct 13, 2003
 */
public interface MetadataCompiler {

	/**
	 * Return the destination directory for the metadata compiler.
	 * This is often the same as the output directory of the javac
	 * compiler. 
	 * @return The destination directoy for the metadata compiler.
	 */
	String getDestinationDirectory();
	
	/**
	 * Set the destination directory for metadata compilation
	 * @param destDir the destination directory for metadata compilation
	 */
	void setDestinationDirectory(String destDir);

	
	/**
	 * Return the classpath that will be used to resolve the classes
	 * specified in the metadata annotations.  The string is a path
	 * separated list of directories and jars.  It value is often the
	 * same as the classpath used by the javac compiler.  
	 * @return The classpath to resolve classes
	 */
	String getClassPath();
	
	/**
	 * Set the classpath.  A path separated list of locations used to
	 * resolve metadata classnames.
	 * @param cp The classpath
	 */
	void setClassPath(String cp);
	
	/**
	 * A list of package names that can be used to 
	 * prepend to the classes specified in the metadata annotations.
	 * This allow for a shortened naming scheme, @Foo() instead of
	 * @com.mycompany.Foo().  Some implementations might choose to
	 * use the import statements in the source code file to resolve the
	 * classname in which case this does not need to be set.
	 *
	 * @return The array of packages to prepend to classes specified in
	 * the metadata annotations.
	 */
	String[] getAttributePackages();
	
	
	/**
	 * Set the list of packages to be prepended to classnames specified
	 * in the metadata annotations.
	 * @param packages The list of packages.
	 */
	void setAttributePackages(String[] packages);

	/**
	 * The directory of source code that the metadata compiler should
	 * process.  This is often the same as the source directory for the
	 * javac compiler. 
	 * 
	 * @return The source directory to run the metadata compiler on.
	 */
	String getSourceDirectory();

	/**
	 * Set the source directory on which to run the metadata compiler 
	 * @param srcDir The source directory.
	 */
	void setSourceDirectory(String srcDir);
	
	/**
	 * The attribute writer that will be used to store the metadata
	 * information.
	 * @return The attribute writer.
	 */
	AttributeWriter getAttributeWriter();

	/**
	 * Set the AttributeWriter implementation that the compiler will
	 * use.
	 * @param writer The AttributeWriter implementation.
	 */
	void setAttributeWriter(AttributeWriter writer);
	
	/**
	 * Start the attribute compilation!
	 * @throws AttributeException if something went wrong, doh!
	 */
	void compile() throws AttributeException;

}
