/*
 * Copyright 2002-2005 the original author or authors.
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

/**
 * Decorator to cause a MetadataAwareAspectInstanceFactory to instantiate
 * only once.
 * @author Rod Johnson
 * @since 2.0
 */
public class LazySingletonMetadataAwareAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory {
	
	private final MetadataAwareAspectInstanceFactory maaif;
	private Object materialized;
	
	public LazySingletonMetadataAwareAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory aif) {
		this.maaif = aif;
	}

	public synchronized Object getAspectInstance() {
		if (materialized == null) {
			materialized = maaif.getAspectInstance();
		}
		return materialized;
	}
	
	public AspectMetadata getAspectMetadata() {
		return maaif.getAspectMetadata();
	}
	
	public int getInstantiationCount() {
		return (materialized != null) ? 1 : 0;
	}
	
	@Override
	public String toString() {
		return "LazySingletonMetadataAwareAspectInstanceFactory: delegate=" + maaif;
	}
	
}