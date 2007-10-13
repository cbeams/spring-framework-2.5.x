/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jmx.export.annotation;

import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;

/**
 * Convenient subclass of Spring's standard {@link MBeanExporter},
 * activating Java 5 annotation usage for JMX exposure of Spring beans:
 * {@link ManagedResource}, {@link ManagedAttribute}, {@link ManagedOperation}, etc.
 *
 * <p>Sets a {@link MetadataNamingStrategy} and a {@link MetadataMBeanInfoAssembler}
 * with an {@link AnnotationJmxAttributeSource}, and activates the
 * {@link #AUTODETECT_ASSEMBLER} mode by default (can be upgraded to
 * {@link #AUTODETECT_ALL} if standard MBeans should be detected and exposed as well).
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class AnnotationMBeanExporter extends MBeanExporter {

	public AnnotationMBeanExporter() {
		AnnotationJmxAttributeSource source = new AnnotationJmxAttributeSource();
		setNamingStrategy(new MetadataNamingStrategy(source));
		setAssembler(new MetadataMBeanInfoAssembler(source));
		setAutodetectMode(AUTODETECT_ALL);
	}

}
