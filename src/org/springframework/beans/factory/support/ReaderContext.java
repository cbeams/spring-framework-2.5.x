/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.ParseState;
import org.springframework.core.io.Resource;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ReaderContext {

	private final Resource resource;

	private final ProblemReporter problemReporter;

	private final ReaderEventListener eventListener;

	private final BeanDefinitionReader reader;

	private final SourceExtractor sourceExtractor;


	public ReaderContext(
			BeanDefinitionReader reader, Resource resource, ProblemReporter problemReporter,
			ReaderEventListener eventListener, SourceExtractor sourceExtractor) {

		this.reader = reader;
		this.resource = resource;
		this.problemReporter = problemReporter;
		this.eventListener = eventListener;
		this.sourceExtractor = sourceExtractor;
	}

	public BeanDefinitionReader getReader() {
		return reader;
	}

	public Resource getResource() {
		return this.resource;
	}


	public void error(String message, Object source) {
		error(message, source, null, null);
	}

	public void error(String message, Object source, ParseState parseState) {
		error(message, source, parseState, null);
	}

	public void error(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.error(new Problem(message, parseState, cause, location));
	}

	public void warning(String message, Object source) {
		warning(message, source, null, null);
	}

	public void warning(String message, Object source, ParseState parseState) {
		warning(message, source, parseState, null);
	}

	public void warning(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.warning(new Problem(message, parseState, cause, location));
	}

	public void fireComponentRegistered(ComponentDefinition componentDefinition) {
		this.eventListener.componentRegistered(componentDefinition);
	}

	public void fireAliasRegistered(String targetBeanName, String alias) {
		this.eventListener.aliasRegistered(targetBeanName, alias);
	}

	public void fireImportProcessed(String importedResource) {
		this.eventListener.importProcessed(importedResource);
	}

	public SourceExtractor getSourceExtractor() {
		return sourceExtractor;
	}

}
