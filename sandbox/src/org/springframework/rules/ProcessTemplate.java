/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules;

/**
 * A interface to be implemented by objects which encapsulate a workflow process
 * template. For example, a template process might produce elements from a data
 * source for processing.
 * <p>
 * Process templates encapsulate the work and logic common to a specific
 * workflow. A user-provided closure call back is passed in on process execution
 * and is called to insert custom processing within the template.
 * <p>
 * For example, the following code snippet demonstrates a generator that
 * produces parsed csv records from an underlying file resource. In this case,
 * this <code>recordGenerator</code> encapsulates required resource management
 * (opening/closing resources) and the algorithm to parse / iterate over
 * records. The results (a single parsed record) are passed to the callback for
 * processing.
 * 
 * <code>
 * 
 * ProcessTemplate recordGenerator = new CsvRecordGenerator(new FileSystemResource(file));
 * 
 * recordGenerator.run(new Block() {
 *     protected void handle(Object csvRecord) {
 *         // process each record
 *     }
 * });
 * </code>
 * 
 * This is a generic equivalent to approaches used throughout The Spring
 * Framework, including Spring's JDBC Template for processing DB query result
 * sets.
 * 
 * @author Keith Donald
 */
public interface ProcessTemplate {

    /**
     * Execute the template with the specific closure callback for the insertion
     * of custom processing code.
     * 
     * @param templateCallback
     *            The procedure callback.
     */
    public void run(Closure templateCallback);
}