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

package org.springframework.beans.factory.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple implementation of the {@link ProblemReporter} that exhibits fail-fast
 * behaviour when errors are encountered. The first error encountered results in
 * a {@link BeanDefinitionParsingException} being thrown.
 *
 * <p>Warnings are written to the log for this class.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class FailFastProblemReporter implements ProblemReporter {

	/**
	 * {@link Log} instance for this class.
	 */
	private static final Log logger = LogFactory.getLog(FailFastProblemReporter.class);


	/**
	 * Throws a {@link BeanDefinitionParsingException} detailing the error that occured.
	 */
	public void error(Problem problem) {
		throw new BeanDefinitionParsingException(problem.getResourceDescription(),
						problem.getParseState(),
						problem.getMessage(),
						problem.getRootCause());
	}

	/**
	 * Writes the supplied {@link Problem} to the {@link Log} at <code>WARN</code> level.
	 */
	public void warning(Problem problem) {
		if (logger.isWarnEnabled()) {
			logger.warn(problem, problem.getRootCause());
		}
	}

}
