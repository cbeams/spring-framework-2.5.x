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

package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;

/**
 * Implementation of AspectInstanceFactory that wraps a singleton instance.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class SingletonMetadataAwareAspectInstanceFactory extends SingletonAspectInstanceFactory
		implements MetadataAwareAspectInstanceFactory {
	
	private final AspectMetadata metadata;


	/**
	 * Create a new SingletonMetadataAwareAspectInstanceFactory for the given aspect.
	 * @param aspectInstance the singleton aspect instance
	 * @param aspectName the name of the aspect
	 */
	public SingletonMetadataAwareAspectInstanceFactory(Object aspectInstance, String aspectName) {
		super(aspectInstance);
		this.metadata = new AspectMetadata(aspectInstance.getClass(), aspectName);
	}


	public AspectMetadata getAspectMetadata() {
		return this.metadata;
	}

}
