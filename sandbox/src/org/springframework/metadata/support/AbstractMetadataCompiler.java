/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.support;

import org.springframework.metadata.AttributeWriter;
import org.springframework.metadata.MetadataCompiler;

/**
 * A convenient base class to use for creating a Metadata compiler.
 * It provides the implementation for the configuration information,
 * sourcedirectory, destination directory etc.
 * 
 * 
 * @author Mark Pollack
 * @since Nov 9, 2003
 */
public abstract class AbstractMetadataCompiler implements MetadataCompiler {

	/**
	 * The destination directory
	 */
	private String destinationDirectory;

	/**
	 * The classpath used to resolve metadata classnames.
	 */
	private String classPath;
	
	/**
	 * The packages to prepend to abbreviated classnames in metadata 
	 * statements.
	 */
	private String[] metadataPackages;
	
	/**
	 * The source directory to compile.
	 */
	private String sourceDirectory;
	
	/**
	 * The AttributeWriter instance used to store the metadata 
	 * information.
	 */
	private AttributeWriter attributeWriter;

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public String getDestinationDirectory() {
		return destinationDirectory;
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public String getClassPath() {
		return classPath;
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public String[] getAttributePackages() {
		return metadataPackages;
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public String getSourceDirectory() {
		return sourceDirectory;
	}
	
	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public AttributeWriter getAttributeWriter() {
		return attributeWriter;
	}
	
	/**
	 * {@inheritDoc}
	 * @param {@inheritDoc}
	 */
	public void setDestinationDirectory(String destDir) {
		destinationDirectory = destDir;
	}
	
	/**
	 * {@inheritDoc}
	 * @param {@inheritDoc}
	 */
	public void setClassPath(String cp) {
		classPath = cp;
	}

	/**
	 * Set the list of packages to be prepended to classnames specified
	 * in the metadata annotations.
	 * @param packages The list of packages.
	 */
	public void setAttributePackages(String[] packages) {
		metadataPackages = packages;
	}

	/**
	 *{@inheritDoc}
	 * @param {@inheritDoc}
	 */
	public void setSourceDirectory(String srcDir) {
		sourceDirectory = srcDir;
	}

	/**
	 * {@inheritDoc}
	 * @param {@inheritDoc}
	 */
	public void setAttributeWriter(AttributeWriter writer) {
		attributeWriter = writer;
	}

}
